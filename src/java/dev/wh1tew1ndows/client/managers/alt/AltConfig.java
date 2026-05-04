package dev.wh1tew1ndows.client.managers.alt;


import com.google.gson.*;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import net.minecraft.util.Session;

import java.io.*;
import java.util.UUID;

public class AltConfig implements IMinecraft {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final File file = new File(mc.gameDir, "\\zetrix\\other\\alts.night");

    public void init() throws Exception {
        // Создаем директории если их нет
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("last", mc.session.getUsername());

        JsonArray altsArray = new JsonArray();
        for (Alt alt : Zetrix.inst().altWidget().alts) {
            altsArray.add(alt.name);
        }

        jsonObject.add("alts", altsArray);

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(gson.toJson(jsonObject));
            //  System.out.println("Аккаунты сохранены: " + Zetrix.inst().altWidget().alts.size() + " шт.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAlts() throws FileNotFoundException {
        try {
            JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(file)));

            if (jsonElement.isJsonNull()) return;

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("last")) {
                mc.session = new Session(jsonObject.get("last").getAsString(), UUID.randomUUID().toString(), "", "mojang");
            }

            if (jsonObject.has("alts")) {
                // Очищаем список перед загрузкой
                Zetrix.inst().altWidget().alts.clear();

                for (JsonElement element : jsonObject.get("alts").getAsJsonArray()) {
                    String name = element.getAsString();
                    Zetrix.inst().altWidget().alts.add(new Alt(name));
                }
                // System.out.println("Аккаунты загружены: " + Zetrix.inst().altWidget().alts.size() + " шт.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
