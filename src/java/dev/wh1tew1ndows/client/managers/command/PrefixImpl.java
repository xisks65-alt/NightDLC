package dev.wh1tew1ndows.client.managers.command;

import dev.wh1tew1ndows.client.managers.command.api.Prefix;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrefixImpl implements Prefix {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final File file = new File(mc.gameDir, "zetrix/other/prefix.night");
    String prefix = ".";

    public PrefixImpl() {
        loadPrefix();
    }

    @Override
    public void set(String prefix) {
        this.prefix = prefix;
        savePrefix();
    }

    @Override
    public String get() {
        return prefix;
    }

    private void savePrefix() {
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            try (FileWriter writer = new FileWriter(file)) {
                Properties props = new Properties();
                props.setProperty("prefix", prefix);
                props.store(writer, "Command Prefix Settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPrefix() {
        try {
            if (!file.exists()) {
                savePrefix(); // Создаём файл с дефолтным префиксом
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                Properties props = new Properties();
                props.load(reader);
                prefix = props.getProperty("prefix", ".");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
