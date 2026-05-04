/*import org.excellent.common.launch.Launcher;

public class Start {

    public static void main(String[] args) {
        Launcher.start();
    }
}*/

import net.minecraft.client.main.Main;

import java.util.Arrays;

public class Start {

    public static void main(String[] args) {
        //  // ШАГ 1: Инициализация антитампер защиты
        //  AntiTamper.initialize();
//
        //  if (AntiTamper.isTampered()) {
        //      LicenseValidator.emergencyShutdown("Client integrity compromised");
        //      return;
        //  }
//
        //  // ШАГ 2: Проверка запуска из лаунчера
        //  if (!LicenseValidator.isLaunchedFromLauncher()) {
        //      System.err.println("Client must be launched from Zetrix.cc Launcher!");
        //      System.exit(-1);
        //      return;
        //  }
//
        //  // ШАГ 3: Валидация через Discord API
        //  // ВАЖНО: hwid приходит ЗАШИФРОВАННЫМ из лоудера!
        //  String encryptedHwid = System.getProperty("zetrix.hwid");
//
        //  if (!LicenseValidator.validateLicense(encryptedHwid)) {
        //      System.err.println("License validation failed!");
        //      System.exit(-1);
        //      return;
        //  }
//
        //  // ШАГ 4: Запуск фонового мониторинга
        //  AntiTamper.startBackgroundMonitoring();

        // ШАГ 5: Запуск Minecraft
        String assets = System.getenv().containsKey("assetDirectory") ? System.getenv("assetDirectory") : "assets";
        Main.main(concat(new String[]{"--version", "mcp", "--accessToken", "0", "--assetsDir", assets, "--assetIndex", "1.16", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
