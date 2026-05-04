package net.minecraft.util;

import dev.wh1tew1ndows.client.managers.events.input.MovementInputKeysEvent;
import dev.wh1tew1ndows.client.managers.events.player.MoveInputEvent;
import net.minecraft.client.GameSettings;

public class MovementInputFromOptions extends MovementInput {
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn) {
        this.gameSettings = gameSettingsIn;
    }

    public void tickMovement(boolean isSneak) {
        final MovementInputKeysEvent moveInputKeysEvent = new MovementInputKeysEvent(this.gameSettings.keyBindForward.isKeyDown(), this.gameSettings.keyBindLeft.isKeyDown(), this.gameSettings.keyBindBack.isKeyDown(), this.gameSettings.keyBindRight.isKeyDown(), this.gameSettings.keyBindJump.isKeyDown(), this.gameSettings.keyBindSneak.isKeyDown());
        moveInputKeysEvent.hook();
        boolean keyBindForward = moveInputKeysEvent.isW();
        boolean keyBindLeft = moveInputKeysEvent.isA();
        boolean keyBindBack = moveInputKeysEvent.isS();
        boolean keyBindRight = moveInputKeysEvent.isD();
        boolean keyBindJump = moveInputKeysEvent.isSpace();
        boolean keyBindSneak = moveInputKeysEvent.isShift();

        this.forwardKeyDown = keyBindForward;
        this.backKeyDown = keyBindBack;
        this.leftKeyDown = keyBindLeft;
        this.rightKeyDown = keyBindRight;
        this.moveForward = this.forwardKeyDown == this.backKeyDown ? 0.0F : (this.forwardKeyDown ? 1.0F : -1.0F);
        this.moveStrafe = this.leftKeyDown == this.rightKeyDown ? 0.0F : (this.leftKeyDown ? 1.0F : -1.0F);
        this.jump = keyBindJump;
        this.sneaking = keyBindSneak;

        MoveInputEvent event = new MoveInputEvent(moveForward, moveStrafe, jump, sneaking, 0.3);
        event.hook();

        if (event.isCancelled()) return;

        final double sneakMultiplier = event.getSneakSlow();
        this.moveForward = event.getForward();
        this.moveStrafe = event.getStrafe();
        this.jump = event.isJump();
        this.sneaking = event.isSneaking();

        if (isSneak) {
            this.moveStrafe = (float) (this.moveStrafe * sneakMultiplier);
            this.moveForward = (float) (this.moveForward * sneakMultiplier);
        }
    }
}
