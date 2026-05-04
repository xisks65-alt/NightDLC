package dev.wh1tew1ndows.protection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Валидатор лицензии через Discord API
 * Проверяет связку DISCORDID:HWID в канале Discord
 */
public class DiscordValidator {

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";

    private String botToken;
    private String channelId;
    private String discordUserId;
    private String hwid;

    /**
     * Конструктор валидатора
     * @param botToken Токен Discord бота
     * @param channelId ID канала с HWID записями
     * @param discordUserId Discord ID пользователя
     * @param hwid HWID пользователя
     */
    public DiscordValidator(String botToken, String channelId, String discordUserId, String hwid) {
        this.botToken = botToken;
        this.channelId = channelId;
        this.discordUserId = discordUserId;
        this.hwid = hwid;
    }

    /**
     * Проверяет валидность лицензии через Discord API
     * @return true если найдена запись DISCORDID:HWID в канале
     */
    public boolean validateLicense() {
        try {
            JsonArray messages = fetchAllChannelMessages();

            if (messages == null || messages.size() == 0) {
                return false;
            }

            // Ищем запись с нашим Discord ID и HWID
            for (JsonElement messageElement : messages) {
                JsonObject message = messageElement.getAsJsonObject();

                if (!message.has("content")) {
                    continue;
                }

                String content = message.get("content").getAsString();

                // Формат: DISCORDID:HWID
                if (content.contains(":")) {
                    String[] parts = content.split(":");

                    if (parts.length >= 2) {
                        String msgDiscordId = parts[0].trim();
                        String msgHwid = parts[1].trim();

                        // Проверяем совпадение
                        boolean discordIdMatch = msgDiscordId.equals(discordUserId);
                        boolean hwidMatch = msgHwid.equals(hwid);

                        if (discordIdMatch && hwidMatch) {
                            return true;
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получает ВСЕ сообщения из Discord канала (с пагинацией)
     * @return JsonArray со всеми сообщениями
     */
    private JsonArray fetchAllChannelMessages() throws Exception {
        JsonArray allMessages = new JsonArray();
        String lastMessageId = null;

        while (true) {
            // Формируем URL с пагинацией
            String urlString = DISCORD_API_BASE + "/channels/" + channelId + "/messages?limit=100";

            if (lastMessageId != null) {
                urlString += "&before=" + lastMessageId;
            }

            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            try {
                // Настраиваем запрос
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bot " + botToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Zetrix.cc-Client/1.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Проверяем код ответа
                int responseCode = connection.getResponseCode();

                if (responseCode != 200) {
                    break;
                }

                // Читаем успешный ответ
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Парсим JSON (старый API для Gson 2.8.0)
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(response.toString());

                if (!element.isJsonArray()) {
                    break;
                }

                JsonArray messages = element.getAsJsonArray();

                // Если сообщений нет - выходим
                if (messages.size() == 0) {
                    break;
                }

                // Добавляем сообщения в общий массив
                for (JsonElement msg : messages) {
                    allMessages.add(msg);
                }

                // Если меньше 100 сообщений - это последняя страница
                if (messages.size() < 100) {
                    break;
                }

                // Получаем ID последнего сообщения для следующей итерации
                JsonObject lastMessage = messages.get(messages.size() - 1).getAsJsonObject();
                if (lastMessage.has("id")) {
                    lastMessageId = lastMessage.get("id").getAsString();
                } else {
                    break;
                }

            } finally {
                connection.disconnect();
            }
        }

        return allMessages;
    }
    
    /**
     * Проверяет доступность Discord API
     * @return true если API доступен
     */
    public boolean testConnection() {
        try {
            String urlString = DISCORD_API_BASE + "/channels/" + channelId;

            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bot " + botToken);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;

        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Получает информацию о канале
     * @return Название канала или null при ошибке
     */
    public String getChannelInfo() {
        try {
            String urlString = DISCORD_API_BASE + "/channels/" + channelId;

            URL url = new URL(urlString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bot " + botToken);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Парсим JSON (старый API для Gson 2.8.0)
                JsonParser parser = new JsonParser();
                JsonObject channel = parser.parse(response.toString()).getAsJsonObject();
                String channelName = channel.has("name") ? channel.get("name").getAsString() : "Unknown";

                connection.disconnect();
                return channelName;
            }

            connection.disconnect();
            return null;

        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Форматирует информацию о валидаторе
     */
    public String getValidatorInfo() {
        return String.format(
            "Discord Validator:\n" +
            "  Channel ID: %s\n" +
            "  Discord ID: %s\n" +
            "  HWID: %s...",
            channelId,
            discordUserId,
            hwid.substring(0, Math.min(8, hwid.length()))
        );
    }
}

