package ru.nocturneguard;

import ru.nocturneguard.J2C.Native;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

@Native
public final class JVMGuard {
    private JVMGuard() {}

    public static boolean basicSanityChecks() {
        try {
            if (isDebuggingEnabled()) {
                System.err.println("JVMGuard: JDWP detected");
                return false;
            }
            if (hasJavaAgent()) {
                System.err.println("JVMGuard: Java agent detected");
                return false;
            }
            if (suspiciousSystemProperties()) {
                System.err.println("JVMGuard: suspicious sysprops");
                return false;
            }
            if (isClassPathTampered()) {
                System.err.println("JVMGuard: classpath tamper");
                return false;
            }
            try {
                if (!NativeBridge.nativeEnvironmentCheck()) {
                    System.err.println("JVMGuard: native environment check failed");
                    return false;
                }
            } catch (Throwable ignored) {}
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isDebuggingEnabled() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String a : args) {
            if (a.contains("-agentlib:jdwp") || a.contains("-Xrunjdwp")) return true;
        }
        return false;
    }

    private static boolean hasJavaAgent() {
        Map<String, String> env = System.getenv();
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String a : args) {
            if (a.contains("-javaagent") || a.contains("-agentpath")) return true;
        }
        if (env.containsKey("JAVA_TOOL_OPTIONS")) {
            String v = env.get("JAVA_TOOL_OPTIONS");
            if (v != null && !v.isEmpty()) return true;
        }
        return false;
    }

    private static boolean suspiciousSystemProperties() {
        String javaHome = System.getProperty("java.home", "");
        String userDir = System.getProperty("user.dir", "");
        if (javaHome.toLowerCase().contains("debug") || userDir.toLowerCase().contains("temp")) return true;
        return false;
    }

    private static boolean isClassPathTampered() {
        String cp = System.getProperty("java.class.path", "");
        if (cp == null || cp.isEmpty()) return true;
        return false;
    }
}
