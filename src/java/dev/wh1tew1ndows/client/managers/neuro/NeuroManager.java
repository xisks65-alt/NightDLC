package dev.wh1tew1ndows.client.managers.neuro;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер нейро-ауры.
 * Записывает ротацию + спринт, обучает и предсказывает.
 */
public class NeuroManager {

    private static NeuroManager INSTANCE;
    public static NeuroManager getInstance() {
        if (INSTANCE == null) INSTANCE = new NeuroManager();
        return INSTANCE;
    }

    public static final Path NEURO_DIR = Paths.get("neuro");

    private static Path getNeuroDir() {
        try {
            return Minecraft.getInstance().gameDir.toPath().resolve("neuro");
        } catch (Exception e) {
            return NEURO_DIR;
        }
    }

    // ---- Состояние записи ----
    private boolean recording        = false;
    private String  pendingModelName = null;

    // Максимум сэмплов — скользящее окно, старые вытесняются
    private static final int MAX_SAMPLES = 50_000;

    /** Каждый сэмпл: {input[8], target[3]} */
    private final ArrayDeque<double[][]> recordedSamples = new ArrayDeque<>(MAX_SAMPLES + 1);

    // ---- Загруженные модели ----
    private final Map<String, NeuroModel> loadedModels = new LinkedHashMap<>();
    private NeuroModel activeModel = null;

    // ---- Предыдущий кадр ----
    private float   prevYaw     = 0;
    private float   prevPitch   = 0;
    private boolean prevSprint  = false;
    private float   prevForward = 0f;

    private final Minecraft mc = Minecraft.getInstance();

    private NeuroManager() { loadAllModels(); }

    // ==================== Запись ====================

    public void startRecording(String modelName) {
        this.pendingModelName = modelName;
        this.recordedSamples.clear();
        this.recording = true;
        chat(TextFormatting.GREEN + "[Нейро] Запись начата: " + TextFormatting.YELLOW + modelName);
        chat(TextFormatting.GRAY + "Включи AttackAura -> Нейро, модель 'Нет', бей врагов.");
    }

    public void stopRecording() {
        if (!recording) { chat(TextFormatting.RED + "[Нейро] Запись не активна."); return; }
        recording = false;
        int count = recordedSamples.size();
        chat(TextFormatting.YELLOW + "[Нейро] Запись остановлена. Сэмплов: " + count);
        if (count < 10) {
            chat(TextFormatting.RED + "[Нейро] Слишком мало данных. Отменено.");
            pendingModelName = null;
            return;
        }
        trainAndSave();
    }

    private void trainAndSave() {
        String name = pendingModelName;
        // Копируем в List для обучения
        List<double[][]> data = new ArrayList<>(recordedSamples);
        int sampleCount = data.size();
        chat(TextFormatting.AQUA + "[Нейро] Обучение '" + name + "' (" + sampleCount + " сэмплов)...");

        new Thread(() -> {
            NeuroModel model = new NeuroModel(name);
            // Эпохи масштабируем: меньше данных — больше эпох, больше данных — меньше
            int epochs = Math.max(50, Math.min(300, 300 * 1000 / Math.max(sampleCount, 1)));
            model.train(data, epochs, 0.001);
            try {
                model.save(getNeuroDir());
                loadedModels.put(name, model);
                mc.execute(() -> {
                    chat(TextFormatting.GREEN + "[Нейро] Модель '" + name + "' сохранена (v" + NeuroModel.VERSION + ", " + epochs + " эпох).");
                    chat(TextFormatting.GRAY + "Выбери в AttackAura -> Нейро модель.");
                });
            } catch (IOException e) {
                mc.execute(() -> chat(TextFormatting.RED + "[Нейро] Ошибка: " + e.getMessage()));
            }
        }, "NeuroTrain").start();

        pendingModelName = null;
    }

    // ==================== Сэмплирование ====================

    /**
     * Записывает сэмпл: ротация + когда игрок отпускает W перед ударом.
     */
    public void recordSample(float targetDX, float targetDY, float targetDZ, float distance, float cooldown) {
        if (!recording || mc.player == null) return;

        float   yaw     = mc.player.rotationYaw;
        float   pitch   = mc.player.rotationPitch;
        float   forward = mc.player.movementInput != null ? mc.player.movementInput.moveForward : 0f;
        boolean sprint  = mc.player.isSprinting();

        double deltaYaw   = yaw   - prevYaw;
        double deltaPitch = pitch - prevPitch;

        // stopMove = 1.0 когда игрок отпустил W (был forward > 0, стал <= 0)
        // или сбросил спринт — оба случая означают "остановил движение перед ударом"
        double stopMove = ((prevForward > 0f && forward <= 0f) || (!sprint && prevSprint)) ? 1.0 : 0.0;

        double[] input = {
            deltaYaw, deltaPitch,
            targetDX, targetDY, targetDZ,
            distance, cooldown,
            forward > 0f ? 1.0 : 0.0  // нажат ли W сейчас
        };
        double[] target = { deltaYaw, deltaPitch, stopMove };

        recordedSamples.add(new double[][]{ input, target });
        // Скользящее окно — вытесняем старые сэмплы
        if (recordedSamples.size() > MAX_SAMPLES) {
            recordedSamples.pollFirst();
        }

        prevYaw     = yaw;
        prevPitch   = pitch;
        prevSprint  = sprint;
        prevForward = forward;
    }

    // ==================== Инференс ====================

    /**
     * Предсказывает [yawOffset, pitchOffset, stopMoveProb].
     * stopMoveProb > 0.5 → нужно остановить W (setForward(0)).
     */
    public float[] predict(float targetDX, float targetDY, float targetDZ, float distance, float cooldown) {
        if (activeModel == null || mc.player == null) return new float[]{ 0, 0, 0 };

        float yaw     = mc.player.rotationYaw;
        float pitch   = mc.player.rotationPitch;
        float forward = mc.player.movementInput != null ? mc.player.movementInput.moveForward : 0f;

        double[] input = {
            yaw - prevYaw, pitch - prevPitch,
            targetDX, targetDY, targetDZ,
            distance, cooldown,
            forward > 0f ? 1.0 : 0.0
        };

        prevYaw     = yaw;
        prevPitch   = pitch;
        prevSprint  = mc.player.isSprinting();
        prevForward = forward;

        double[] out = activeModel.forward(input);
        return new float[]{ (float) out[0], (float) out[1], (float) out[2] };
    }

    /** Предсказывает только вероятность остановки W (без обновления prev-состояния) */
    public float predictStopMove(float targetDX, float targetDY, float targetDZ, float distance, float cooldown) {
        if (activeModel == null || mc.player == null) return 0f;
        float forward = mc.player.movementInput != null ? mc.player.movementInput.moveForward : 0f;
        double[] input = {
            mc.player.rotationYaw - prevYaw,
            mc.player.rotationPitch - prevPitch,
            targetDX, targetDY, targetDZ,
            distance, cooldown,
            forward > 0f ? 1.0 : 0.0
        };
        double[] out = activeModel.forward(input);
        return (float) out[2];
    }

    // ==================== Управление моделями ====================

    public void setActiveModel(String name) {
        if (name == null || name.equals("Нет")) { activeModel = null; return; }
        activeModel = loadedModels.get(name);
    }

    public NeuroModel getActiveModel() { return activeModel; }
    public boolean    isRecording()    { return recording; }

    public List<String> getModelNames() {
        List<String> names = new ArrayList<>();
        names.add("Нет");
        names.addAll(loadedModels.keySet());
        return names;
    }

    private void loadAllModels() {
        try {
            Path dir = getNeuroDir();
            Files.createDirectories(dir);
            List<Path> files = Files.list(dir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            for (Path f : files) {
                try {
                    NeuroModel m = NeuroModel.load(f);
                    loadedModels.put(m.getName(), m);
                } catch (Exception e) {
                    // пропускаем битые файлы
                }
            }
        } catch (IOException ignored) {}
    }

    public void reloadModels() {
        loadedModels.clear();
        loadAllModels();
        chat(TextFormatting.GREEN + "[Нейро] Загружено: " + loadedModels.size() + " | " + getNeuroDir().toAbsolutePath());
    }

    private void chat(String msg) {
        if (mc.player != null) ChatUtil.addText(msg);
    }
}
