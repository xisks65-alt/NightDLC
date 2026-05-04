package ru.nocturneguard;

import ru.nocturneguard.J2C.Native;

import java.util.concurrent.*;

@Native
public final class SelfProtectionScheduler {
    private SelfProtectionScheduler() {}

    public static void startPeriodicChecks(ScheduledExecutorService scheduler) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!JVMGuard.basicSanityChecks()) {
                    System.err.println("SelfProtectionScheduler: environment changed -> exit");
                    Runtime.getRuntime().halt(2);
                }
            } catch (Throwable t) {
            }
        }, 3, 3, TimeUnit.SECONDS);
    }
}
