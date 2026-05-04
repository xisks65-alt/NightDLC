package dev.wh1tew1ndows.client.utils.other;

import java.util.HashSet;
import java.util.Set;

public class LockManager {
    private static final Set<String> activeLocks = new HashSet<>();

    public static boolean tryLock(String moduleName) {
        if (isLocked()) return false;
        activeLocks.add(moduleName);
        return true;
    }

    public static void unlock(String moduleName) {
        activeLocks.remove(moduleName);
    }

    public static boolean isLocked() {
        return !activeLocks.isEmpty();
    }

    public static boolean isLockedBy(String moduleName) {
        return activeLocks.contains(moduleName);
    }
}