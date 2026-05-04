package ru.nocturneguard;

import ru.nocturneguard.J2C.Native;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

@Native
public final class IntegrityChecker {
    private static final Map<String, String> KNOWN = new HashMap<>();
   // static {}

    public static boolean verifyBundle() {
        try {
            for (Map.Entry<String,String> e : KNOWN.entrySet()) {
                String resource = e.getKey();
                String expected = e.getValue();
                byte[] actualHash = sha256Resource(resource);
                String actualB64 = Base64.getEncoder().encodeToString(actualHash);
                if (!actualB64.equals(expected)) {
                    System.err.println("IntegrityChecker: mismatch " + resource);
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            System.err.println("IntegrityChecker error: " + t.getMessage());
            return false;
        }
    }

    private static byte[] sha256Resource(String resourcePath) throws Exception {
        try (InputStream in = IntegrityChecker.class.getResourceAsStream("/" + resourcePath)) {
            if (in == null) throw new IllegalStateException("Missing resource " + resourcePath);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) md.update(buf, 0, r);
            return md.digest();
        }
    }
}
