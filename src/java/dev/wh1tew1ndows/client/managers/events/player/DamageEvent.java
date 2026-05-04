package dev.wh1tew1ndows.client.managers.events.player;

import dev.wh1tew1ndows.client.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DamageEvent extends Event {
    private final DamageType damageType;

    public enum DamageType {
        ENDER_PEARL, ARROW, FALL
    }
}

