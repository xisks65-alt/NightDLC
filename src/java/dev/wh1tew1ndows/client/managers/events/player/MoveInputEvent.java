package dev.wh1tew1ndows.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.wh1tew1ndows.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class MoveInputEvent extends CancellableEvent {
    private float forward, strafe;
    private boolean jump, sneaking;
    private double sneakSlow;

    public boolean isMoving() {
        return getForward() != 0 || getStrafe() != 0;
    }
}