package dev.wh1tew1ndows.protection;

/**
 * Класс для валидации лицензии через Discord API
 * Проверяет запись DISCORDID:HWID в Discord канале
 */
public class LicenseValidator {

    private static boolean isValidated = false;
    private static DiscordValidator discordValidator = null;

    /**
     * Проверяет лицензию через Discord API
     *
     * @param encryptedHwid ЗАШИФРОВАННЫЙ HWID пользователя (из лоудера)
     * @return true если найдена запись DISCORDID:HWID в Discord канале
     */
    public static boolean validateLicense(String encryptedHwid) {
        // Проверка наличия зашифрованного HWID
        if (encryptedHwid == null || encryptedHwid.isEmpty()) {
            return false;
        }

        // ===== РАСШИФРОВЫВАЕМ АРГУМЕНТЫ =====
        String hwid = ArgumentDecryptor.decrypt(encryptedHwid);
        if (hwid == null || hwid.isEmpty()) {
            return false;
        }

        // Читаем зашифрованные данные из System Properties
        String encryptedBotToken = System.getProperty("zetrix.bot_token");
        String encryptedChannelId = System.getProperty("zetrix.channel_id");
        String encryptedDiscordId = System.getProperty("zetrix.discord_id");

        // Расшифровываем
        String botToken = ArgumentDecryptor.decrypt(encryptedBotToken);
        String channelId = ArgumentDecryptor.decrypt(encryptedChannelId);
        String discordId = ArgumentDecryptor.decrypt(encryptedDiscordId);

        // Discord проверка ОБЯЗАТЕЛЬНА!
        if (botToken == null || botToken.isEmpty()) {
            return false;
        }

        if (channelId == null || channelId.isEmpty()) {
            return false;
        }

        if (discordId == null || discordId.isEmpty()) {
            return false;
        }

        try {
            discordValidator = new DiscordValidator(botToken, channelId, discordId, hwid);

            // Тестируем соединение
            if (!discordValidator.testConnection()) {
                return false;
            }

            // Проверяем лицензию через Discord
            if (!discordValidator.validateLicense()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        isValidated = true;
        return true;
    }

    /**
     * Проверяет, была ли лицензия валидирована
     */
    public static boolean isLicenseValid() {
        return isValidated;
    }

    /**
     * Периодическая проверка лицензии во время работы
     * Вызывается каждые N секунд для защиты от патчинга
     */
    public static boolean recheckLicense() {
        if (!isValidated) {
            return false;
        }

        // Проверяем через Discord API снова
        if (discordValidator != null) {
            return discordValidator.validateLicense();
        }

        return true;
    }

    /**
     * Деактивирует лицензию (при выходе или ошибке)
     */
    public static void deactivateLicense() {
        isValidated = false;
    }

    /**
     * Проверяет, запущен ли клиент из лаунчера
     * Проверяет наличие специальных JVM свойств
     */
    public static boolean isLaunchedFromLauncher() {
        String launcherMarker = System.getProperty("zetrix.launcher");
        String launcherVersion = System.getProperty("zetrix.version");

        return "true".equals(launcherMarker) && launcherVersion != null;
    }

    /**
     * Получает информацию о лаунчере
     */
    public static String getLauncherInfo() {
        if (!isLaunchedFromLauncher()) {
            return "Not launched from Zetrix.cc Launcher";
        }

        String version = System.getProperty("zetrix.version", "unknown");
        String encryptedHwid = System.getProperty("zetrix.hwid", "unknown");

        // Расшифровываем HWID для отображения
        String hwid = ArgumentDecryptor.decrypt(encryptedHwid);
        if (hwid == null) {
            hwid = "encrypted";
        }

        return String.format("Zetrix.cc Launcher v%s (HWID: %s)", version,
                hwid.length() > 8 ? hwid.substring(0, 8) + "..." : hwid);
    }

    /**
     * Экстренная остановка клиента при обнаружении взлома
     */
    public static void emergencyShutdown(String reason) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Игнорируем
        }

        System.exit(-1);
    }
}

