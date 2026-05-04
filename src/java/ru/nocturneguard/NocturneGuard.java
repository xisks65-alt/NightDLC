package ru.nocturneguard;

import ru.nocturneguard.J2C.Native;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Native
public final class NocturneGuard {


    public static boolean dev = true;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "NocturneSelfChecks");
        t.setDaemon(true);
        return t;
    });

    @Native
    public static void start() {
        UserProfile.loadFromRegistry();
        if (!dev) {
            NativeBridge.loadNative();

            if (!JVMGuard.basicSanityChecks()) {
                shutdown("NU TI I LOX");
                return;
            }

            if (!IntegrityChecker.verifyBundle()) {
                shutdown("SOSI XYI ");
                return;
            }
            AntiTamperClassLoader.hook();
            SelfProtectionScheduler.startPeriodicChecks(scheduler);
            try {
                NativeBridge.onLoaderStarted();
            } catch (Throwable ignored) {
            }
        }
    }

    private static void shutdown(String reason) {
        try {
            System.err.println("SASI XYI" + reason);
            NativeBridge.onTamperDetected(reason);
        } catch (Throwable ignored) {
        }
        Runtime.getRuntime().halt(1);
    }
}
