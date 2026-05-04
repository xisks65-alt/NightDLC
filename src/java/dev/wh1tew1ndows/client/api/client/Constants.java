package dev.wh1tew1ndows.client.api.client;

import dev.wh1tew1ndows.client.Zetrix;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class Constants {
    public final String NAME = "Zetrix.cc";
    public final String NAMESPACE = "zetrix";
    public final String RELEASE = "Release";
    public final String VERSION = Zetrix.name;
    public final String TITLE = NAME + " » " + RELEASE + " » Build:" + VERSION;
    public final Path MAIN_DIR = Paths.get(Minecraft.getInstance().gameDir + NAMESPACE);
    public final String FILE_FORMAT = ".night";
}
