package ru.nocturneguard;

import lombok.Getter;
import ru.nocturneguard.J2C.J2t;
import ru.nocturneguard.J2C.Native;
import ru.nocturneguard.J2C.NotNative;

@J2t
@Native
public class UserProfile {

    private static UserProfile instance;

    @Getter
    private final String username;
    @Getter
    private final String uid;
    @Getter
    private final String hwid;
    private final String accessToken;
    private final String sessionKey;
    private final long loginTime;

    private UserProfile(String username, String uid, String hwid, String accessToken, String sessionKey) {
        this.username = username;
        this.uid = uid;
        this.hwid = hwid;
        this.accessToken = accessToken;
        this.sessionKey = sessionKey;
        this.loginTime = System.currentTimeMillis();
    }

    @NotNative
    public static void initProfile(String username, String uid, String hwid, String accessToken, String sessionKey) {
        if (instance != null)
            return;
        if (!NocturneGuard.dev) {
            if (instance == null) instance = new UserProfile(username, uid, hwid, accessToken, sessionKey);
        } else instance = new UserProfile("Wh1teW1ndows_", "-1", "hwid", "accessToken", "sessionKey");
        if (instance == null) UserProfile.get().clear();
    }

    public void clear() {
        deleteRegistryValue("username");
        deleteRegistryValue("uid");
        deleteRegistryValue("hwid");
        deleteRegistryValue("accessToken");
        deleteRegistryValue("sessionKey");
    }

    private static String readRegistryValue(String key) {
        try {
            Process process = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\NocturneGuard", "/v", key)
                    .redirectErrorStream(true)
                    .start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(key)) {
                    String[] parts = line.trim().split("\\s{4,}");
                    if (parts.length >= 3) {
                        return parts[2].trim();
                    }
                }
            }
            reader.close();
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void deleteRegistryValue(String valueName) {
        try {
            Process process = new ProcessBuilder(
                    "reg", "delete", "HKCU\\Software\\NocturneGuard",
                    "/v", valueName, "/f"
            ).redirectErrorStream(true).start();

            process.waitFor();
        } catch (Exception ignored) {
        }
    }


    public static void loadFromRegistry() {
        //if (instance != null)
        // throw new IllegalStateException("VSE TOP");

        String username = readRegistryValue("username");
        String uid = readRegistryValue("uid");
        String hwid = readRegistryValue("hwid");
        String accessToken = readRegistryValue("accessToken");
        String sessionKey = readRegistryValue("sessionKey");

        if (username == null || uid == null || hwid == null) {
            if (!NocturneGuard.dev)
                throw new IllegalStateException("NE KRAKAY");
        }

        UserProfile.initProfile(username, uid, hwid, accessToken, sessionKey);
    }

    public static UserProfile get() {
        if (instance == null)
            throw new IllegalStateException("NE POLUCHITSYA");
        return instance;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", uid='" + uid + '\'' +
                ", hwid='" + hwid + '\'' +
                '}';
    }
}
