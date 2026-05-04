package dev.wh1tew1ndows.client.managers.neuro;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * MLP для предсказания ротации + сброса спринта.
 *
 * v1 (legacy): вход [7], выход [2] — только ротация
 * v2 (current): вход [8], выход [3] — ротация + спринт
 *
 * Вход v2: [deltaYaw, deltaPitch, targetDX, targetDY, targetDZ, distance, cooldown, sprintState]
 * Выход v2: [yawOffset, pitchOffset, sprintProb]  (sprintProb: 0=не сбрасывать, 1=сбросить)
 */
public class NeuroModel {

    public static final int VERSION = 2;

    // v2 размеры
    private static final int INPUT_SIZE  = 8;
    private static final int HIDDEN_SIZE = 48;
    private static final int OUTPUT_SIZE = 3;

    private double[][] w1 = new double[HIDDEN_SIZE][INPUT_SIZE];
    private double[]   b1 = new double[HIDDEN_SIZE];
    private double[][] w2 = new double[OUTPUT_SIZE][HIDDEN_SIZE];
    private double[]   b2 = new double[OUTPUT_SIZE];

    private final String name;
    private final int version;

    public NeuroModel(String name) {
        this.name    = name;
        this.version = VERSION;
        initRandom();
    }

    /** Конструктор для загрузки legacy v1 модели (7 входов, 2 выхода) */
    private NeuroModel(String name, int version, double[][] w1, double[] b1, double[][] w2, double[] b2) {
        this.name    = name;
        this.version = version;
        this.w1 = w1;
        this.b1 = b1;
        this.w2 = w2;
        this.b2 = b2;
    }

    private void initRandom() {
        Random rng = new Random(42);
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            for (int j = 0; j < INPUT_SIZE; j++)
                w1[i][j] = rng.nextGaussian() * 0.1;
            b1[i] = 0;
        }
        for (int i = 0; i < OUTPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++)
                w2[i][j] = rng.nextGaussian() * 0.1;
            b2[i] = 0;
        }
    }

    /** Прямой проход. Для v1 — возвращает [yaw, pitch, 0.0] */
    public double[] forward(double[] input) {
        int inSize  = w1[0].length;
        int hidSize = w1.length;
        int outSize = w2.length;

        double[] hidden = new double[hidSize];
        for (int i = 0; i < hidSize; i++) {
            double sum = b1[i];
            for (int j = 0; j < inSize; j++) sum += w1[i][j] * input[j];
            hidden[i] = relu(sum);
        }
        double[] output = new double[outSize];
        for (int i = 0; i < outSize; i++) {
            double sum = b2[i];
            for (int j = 0; j < hidSize; j++) sum += w2[i][j] * hidden[j];
            output[i] = sum;
        }

        // v1 не имеет sprintProb — добавляем 0
        if (outSize == 2) {
            return new double[]{ output[0], output[1], 0.0 };
        }
        // v2: применяем sigmoid к sprintProb
        output[2] = sigmoid(output[2]);
        return output;
    }

    /**
     * Обучение SGD с backprop.
     * samples: {input[8], target[3]}  (target[2] = 0.0 или 1.0 для спринта)
     */
    public void train(List<double[][]> samples, int epochs, double lr) {
        int inSize  = w1[0].length;
        int hidSize = w1.length;
        int outSize = w2.length;

        // Мини-батч: если данных много — берём случайные подвыборки
        int batchSize = Math.min(samples.size(), 2048);
        Random rng = new Random();

        for (int epoch = 0; epoch < epochs; epoch++) {
            // Формируем мини-батч
            List<double[][]> batch;
            if (samples.size() <= batchSize) {
                batch = samples;
            } else {
                batch = new ArrayList<>(batchSize);
                for (int b = 0; b < batchSize; b++) {
                    batch.add(samples.get(rng.nextInt(samples.size())));
                }
            }

            for (double[][] sample : batch) {
                double[] input  = sample[0];
                double[] target = sample[1];

                // Forward
                double[] hiddenRaw = new double[hidSize];
                double[] hidden    = new double[hidSize];
                for (int i = 0; i < hidSize; i++) {
                    double sum = b1[i];
                    for (int j = 0; j < inSize; j++) sum += w1[i][j] * input[j];
                    hiddenRaw[i] = sum;
                    hidden[i]    = relu(sum);
                }
                double[] rawOut = new double[outSize];
                double[] output = new double[outSize];
                for (int i = 0; i < outSize; i++) {
                    double sum = b2[i];
                    for (int j = 0; j < hidSize; j++) sum += w2[i][j] * hidden[j];
                    rawOut[i] = sum;
                    output[i] = (i == 2) ? sigmoid(sum) : sum; // sigmoid только для спринта
                }

                // Градиент выхода
                double[] dOut = new double[outSize];
                for (int i = 0; i < outSize; i++) {
                    if (i == 2) {
                        // BCE loss для спринта: d/dz sigmoid(z) * BCE = output - target
                        dOut[i] = output[i] - target[i];
                    } else {
                        // MSE для ротации
                        dOut[i] = 2.0 * (output[i] - target[i]);
                    }
                }

                // Backprop to hidden
                double[] dHidden = new double[hidSize];
                for (int j = 0; j < hidSize; j++) {
                    double grad = 0;
                    for (int i = 0; i < outSize; i++) grad += w2[i][j] * dOut[i];
                    dHidden[j] = grad * reluDeriv(hiddenRaw[j]);
                }

                // Update w2, b2
                for (int i = 0; i < outSize; i++) {
                    for (int j = 0; j < hidSize; j++) w2[i][j] -= lr * dOut[i] * hidden[j];
                    b2[i] -= lr * dOut[i];
                }
                // Update w1, b1
                for (int i = 0; i < hidSize; i++) {
                    for (int j = 0; j < inSize; j++) w1[i][j] -= lr * dHidden[i] * input[j];
                    b1[i] -= lr * dHidden[i];
                }
            }
        }
    }

    private double relu(double x)      { return Math.max(0, x); }
    private double reluDeriv(double x) { return x > 0 ? 1.0 : 0.0; }
    private double sigmoid(double x)   { return 1.0 / (1.0 + Math.exp(-x)); }

    public String getName()   { return name; }
    public int    getVersion(){ return version; }

    // ---- Сериализация ----

    public void save(Path dir) throws IOException {
        Files.createDirectories(dir);
        JsonObject root = new JsonObject();
        root.addProperty("name",    name);
        root.addProperty("version", version);
        root.add("w1", matrix2Json(w1));
        root.add("b1", array2Json(b1));
        root.add("w2", matrix2Json(w2));
        root.add("b2", array2Json(b2));
        Path file = dir.resolve(name + ".json");
        Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root));
    }

    public static NeuroModel load(Path file) throws IOException {
        String     json = Files.readString(file);
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        String     name = root.get("name").getAsString();
        int        ver  = root.has("version") ? root.get("version").getAsInt() : 1;

        double[][] w1 = json2Matrix(root.getAsJsonArray("w1"));
        double[]   b1 = json2Array(root.getAsJsonArray("b1"));
        double[][] w2 = json2Matrix(root.getAsJsonArray("w2"));
        double[]   b2 = json2Array(root.getAsJsonArray("b2"));

        return new NeuroModel(name, ver, w1, b1, w2, b2);
    }

    private JsonArray matrix2Json(double[][] mat) {
        JsonArray rows = new JsonArray();
        for (double[] row : mat) rows.add(array2Json(row));
        return rows;
    }

    private JsonArray array2Json(double[] arr) {
        JsonArray a = new JsonArray();
        for (double v : arr) a.add(v);
        return a;
    }

    private static double[][] json2Matrix(JsonArray arr) {
        double[][] mat = new double[arr.size()][];
        for (int i = 0; i < arr.size(); i++) mat[i] = json2Array(arr.get(i).getAsJsonArray());
        return mat;
    }

    private static double[] json2Array(JsonArray arr) {
        double[] a = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++) a[i] = arr.get(i).getAsDouble();
        return a;
    }
}
