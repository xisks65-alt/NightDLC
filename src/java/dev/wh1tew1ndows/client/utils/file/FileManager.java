package dev.wh1tew1ndows.client.utils.file;


import dev.wh1tew1ndows.client.api.client.Constants;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import lombok.extern.log4j.Log4j2;

import java.io.File;

@Log4j2
public class FileManager implements IMinecraft {
    public static File DIRECTORY;

    public FileManager() {
        DIRECTORY = new File(mc.gameDir, Constants.NAMESPACE);
        if (!DIRECTORY.exists()) {
            if (!DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", Constants.NAMESPACE);
                System.exit(0);
            }
        }
    }
}