package dev.wh1tew1ndows.protection;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * Антитампер защита для предотвращения модификации и отладки клиента
 */
public class AntiTamper {

    private static boolean debuggerDetected = false;
    private static boolean tamperDetected = false;
    private static final long startTime = System.currentTimeMillis();

    /**
     * Инициализация антитампер защиты
     */
    public static void initialize() {
        // Проверяем отладчик
        if (isDebuggerAttached()) {
            debuggerDetected = true;
        }

        // Проверяем подозрительные JVM аргументы
        if (hasSuspiciousJVMArgs()) {
            tamperDetected = true;
        }

        // Проверяем целостность JAR
        if (!verifyJarIntegrity()) {
            tamperDetected = true;
        }
    }

    /**
     * Проверяет, подключен ли отладчик
     */
    public static boolean isDebuggerAttached() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();

        for (String arg : arguments) {
            // Проверяем наличие debug аргументов
            if (arg.contains("-agentlib:jdwp") ||
                    arg.contains("-Xdebug") ||
                    arg.contains("-Xrunjdwp")) {
                return true;
            }
        }

        // Дополнительная проверка через ManagementFactory
        try {
            String jvmName = runtimeMXBean.getName();
            // Если JVM запущена с отладчиком, имя может содержать специфичные паттерны
            return jvmName.contains("debug");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверяет наличие подозрительных JVM аргументов
     */
    public static boolean hasSuspiciousJVMArgs() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();

        String[] suspiciousArgs = {
                "-javaagent",
                "-agentpath",
                "frida",
                "xposed"
        };

        for (String arg : arguments) {
            for (String suspicious : suspiciousArgs) {
                if (arg.toLowerCase().contains(suspicious.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Проверяет целостность JAR файла
     */
    public static boolean verifyJarIntegrity() {
        try {
            // Получаем путь к текущему JAR
            String jarPath = AntiTamper.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            File jarFile = new File(jarPath);

            // Если запущено не из JAR (например, из IDE), пропускаем проверку
            if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
                return true;
            }

            // Проверяем размер файла (базовая проверка)
            long fileSize = jarFile.length();
            if (fileSize < 1024 * 100) { // Меньше 100KB - подозрительно
                return false;
            }

            // Проверяем, что файл не был изменён недавно
            long lastModified = jarFile.lastModified();
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastModified;

            // Если файл был изменён в последние 5 секунд - подозрительно
            return timeDiff >= 5000;
        } catch (Exception e) {
            return true; // Не блокируем запуск при ошибке проверки
        }
    }

    /**
     * Проверяет, запущен ли клиент в виртуальной машине
     */
    public static boolean isRunningInVM() {
        try {
            String[] vmIndicators = {
                    "VirtualBox",
                    "VMware",
                    "QEMU",
                    "Xen",
                    "Hyper-V"
            };

            String osName = System.getProperty("os.name", "").toLowerCase();
            String osArch = System.getProperty("os.arch", "").toLowerCase();

            for (String indicator : vmIndicators) {
                if (osName.contains(indicator.toLowerCase())) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Периодическая проверка защиты
     * Должна вызываться каждые N секунд
     */
    public static boolean performRuntimeCheck() {
        // Проверяем, не подключился ли отладчик во время работы
        if (!debuggerDetected && isDebuggerAttached()) {
            debuggerDetected = true;
            return false;
        }

        // Проверяем время работы (защита от time-based attacks)
        long currentTime = System.currentTimeMillis();
        long runTime = currentTime - startTime;

        // Если время работы отрицательное или слишком большое - подозрительно
        // Больше 24 часов
        return runTime >= 0 && runTime <= 1000L * 60 * 60 * 24;
    }

    /**
     * Проверяет, были ли обнаружены попытки взлома
     */
    public static boolean isTampered() {
        return tamperDetected || debuggerDetected;
    }

    /**
     * Получает статус защиты
     */
    public static String getProtectionStatus() {
        if (tamperDetected) {
            return "TAMPERED";
        }
        if (debuggerDetected) {
            return "DEBUGGER_DETECTED";
        }
        return "OK";
    }

    /**
     * Проверяет наличие известных инструментов для взлома
     */
    public static boolean detectHackingTools() {
        try {
            String[] hackingTools = {
                    "cheatengine",
                    "ida",
                    "ollydbg",
                    "x64dbg",
                    "frida",
                    "xposed"
            };

            // Проверяем загруженные библиотеки
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String classPath = runtimeMXBean.getClassPath();

            for (String tool : hackingTools) {
                if (classPath.toLowerCase().contains(tool)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Запускает фоновый поток для постоянной проверки
     */
    public static void startBackgroundMonitoring() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Проверка каждые 30 секунд

                    if (!performRuntimeCheck()) {
                        LicenseValidator.emergencyShutdown("Runtime protection check failed");
                        break;
                    }

                    if (detectHackingTools()) {
                        LicenseValidator.emergencyShutdown("Hacking tools detected");
                        break;
                    }

                    if (!LicenseValidator.recheckLicense()) {
                        LicenseValidator.emergencyShutdown("License recheck failed");
                        break;
                    }

                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    // Тихо игнорируем ошибки
                }
            }
        }, "Zetrix.cc-Protection-Monitor");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }
}

