package dev.wh1tew1ndows.client.utils;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import java.util.HashSet;
import java.util.Set;


public class MovementManager implements IMinecraft {
    private static final MovementManager INSTANCE = new MovementManager();
    public final Set<String> lockRequests = new HashSet<>();

    private MovementManager() {
    }

    public static MovementManager getInstance() {
        return INSTANCE;
    }

    public void lockMovement(String moduleName) {
        if (mc.player == null || !mc.player.isAlive() || mc.world == null) {
            return;
        }
        lockRequests.add(moduleName);
        setMovementKeys(false);
    }

    public void unlockMovement(String moduleName) {
        if (mc.player == null || !mc.player.isAlive() || mc.world == null) {
            return;
        }
        lockRequests.remove(moduleName);
        if (lockRequests.isEmpty() && mc.currentScreen == null) {
            setMovementKeys(true);
        }
    }

    public boolean isMovementLocked() {
        return !lockRequests.isEmpty();
    }

    /**
     * Добавляет задачу для выполнения с блокировкой движения, аналогично InvComponent.addTask
     */


    /**
     * Добавляет задачу для выполнения с блокировкой движения
     */

    private void setMovementKeys(boolean state) {
        KeyBinding[] movementKeys = {
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint
        };
        for (KeyBinding keyBinding : movementKeys) {
            keyBinding.setPressed(state && InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode()));
        }
    }
}
