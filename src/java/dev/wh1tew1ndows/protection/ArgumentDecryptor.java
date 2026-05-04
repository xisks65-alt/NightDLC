package dev.wh1tew1ndows.protection;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Класс для расшифровки аргументов, переданных из C++ лоудера
 * Использует AES-256 + XOR шифрование (в обратном порядке)
 */
public class ArgumentDecryptor {

    // Ключи шифрования (должны совпадать с C++ лоудером!)
    private static final String AES_KEY = "gl6cPdIBrdR46ffgsAwD3n80UZ88C4r8ykBBsDoI0";
    private static final String XOR_KEY = "4KicRC9OyjNfF6H1rh402SG0GIVcyuReWUbUFA2K";

    // Логирование в файл для отладки
    private static void logDecryption(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter("decryption_debug.log", true))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            out.println("[" + sdf.format(new Date()) + "] " + message);
        } catch (Exception e) {
            // Игнорируем ошибки логирования
        }
    }

    /**
     * Расшифровывает строку, зашифрованную в C++ лоудере
     * Порядок: Base64 decode -> AES decrypt -> XOR decrypt
     * FALLBACK: если расшифровка не работает, возвращает оригинальную строку
     *
     * @param encryptedBase64 Зашифрованная строка в Base64 (или незашифрованная при fallback)
     * @return Расшифрованная строка или оригинальная при ошибке
     */
    public static String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            logDecryption("decrypt: Null or empty input");
            return null;
        }

        logDecryption("decrypt: Starting decryption for string of length " + encryptedBase64.length());
        logDecryption("decrypt: Input (first 50 chars): " + encryptedBase64.substring(0, Math.min(50, encryptedBase64.length())));

        try {
            // Шаг 1: Декодируем из Base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            logDecryption("decrypt: Base64 decoded, byte array size: " + encryptedBytes.length);

            // Шаг 2: Расшифровываем AES
            byte[] aesDecrypted = decryptAES(encryptedBytes);
            if (aesDecrypted == null) {
                logDecryption("decrypt: AES decryption FAILED! Using fallback (original string)");
                // FALLBACK: возвращаем оригинальную строку
                return encryptedBase64;
            }
            logDecryption("decrypt: AES decrypted, byte array size: " + aesDecrypted.length);

            // Шаг 3: Расшифровываем XOR
            byte[] xorDecrypted = decryptXOR(aesDecrypted);
            logDecryption("decrypt: XOR decrypted, byte array size: " + xorDecrypted.length);

            // Конвертируем в строку
            String result = new String(xorDecrypted, StandardCharsets.UTF_8);
            logDecryption("decrypt: Result length: " + result.length());
            logDecryption("decrypt: Result (first 20 chars): " + result.substring(0, Math.min(20, result.length())));
            logDecryption("decrypt: SUCCESS!");

            return result;

        } catch (Exception e) {
            logDecryption("decrypt: Exception occurred: " + e.getClass().getName() + " - " + e.getMessage());
            logDecryption("decrypt: Using fallback (original string)");
            // FALLBACK: если расшифровка не работает (например, строка не в Base64),
            // возвращаем оригинальную строку (незашифрованную)
            return encryptedBase64;
        }
    }

    /**
     * AES-256 расшифровка
     */
    private static byte[] decryptAES(byte[] encrypted) {
        try {
            logDecryption("decryptAES: Starting AES decryption, input size: " + encrypted.length);

            // Создаем SHA-256 хэш ключа (как в C++ через CryptHashData)
            // ВАЖНО: используем US-ASCII как в C++ (strlen работает с ASCII)
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(AES_KEY.getBytes("US-ASCII"));
            logDecryption("decryptAES: SHA-256 key hash created, key size: " + keyBytes.length);

            // Создаем AES ключ
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            logDecryption("decryptAES: SecretKeySpec created");

            // Инициализируем Cipher для расшифровки
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            logDecryption("decryptAES: Cipher initialized");

            // Расшифровываем
            byte[] result = cipher.doFinal(encrypted);
            logDecryption("decryptAES: Decryption successful, output size: " + result.length);
            return result;

        } catch (Exception e) {
            logDecryption("decryptAES: ERROR - " + e.getClass().getName() + ": " + e.getMessage());
            System.err.println("AES decryption error: " + e.getMessage());
            return null;
        }
    }

    /**
     * XOR расшифровка (симметричная операция)
     */
    private static byte[] decryptXOR(byte[] data) {
        logDecryption("decryptXOR: Starting XOR decryption, input size: " + data.length);

        // ВАЖНО: используем US-ASCII как в C++ (strlen работает с ASCII)
        byte[] keyBytes = XOR_KEY.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        logDecryption("decryptXOR: XOR key size: " + keyBytes.length);

        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }

        logDecryption("decryptXOR: XOR decryption complete, output size: " + result.length);
        return result;
    }

    /**
     * Безопасное получение и расшифровка System Property
     */
    public static String getDecryptedProperty(String propertyName) {
        String encrypted = System.getProperty(propertyName);
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }
        return decrypt(encrypted);
    }

    /**
     * Безопасное получение и расшифровка Environment Variable
     */
    public static String getDecryptedEnv(String envName) {
        String encrypted = System.getenv(envName);
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }
        return decrypt(encrypted);
    }

    /**
     * Тестовый метод для проверки шифрования/расшифровки
     */
    public static boolean testDecryption() {
        try {
            // Тестовая строка
            String test = "test123";

            // В реальности шифрование происходит в C++, но для теста можем проверить XOR
            byte[] xorEncrypted = encryptXOR(test.getBytes(StandardCharsets.UTF_8));
            byte[] xorDecrypted = decryptXOR(xorEncrypted);

            String result = new String(xorDecrypted, StandardCharsets.UTF_8);
            return test.equals(result);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * XOR шифрование (для тестирования)
     */
    private static byte[] encryptXOR(byte[] data) {
        byte[] keyBytes = XOR_KEY.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ keyBytes[i % keyBytes.length]);
        }

        return result;
    }
}

