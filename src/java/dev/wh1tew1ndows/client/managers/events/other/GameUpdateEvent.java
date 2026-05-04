package dev.wh1tew1ndows.client.managers.events.other;

import lombok.Getter;
import dev.wh1tew1ndows.client.api.events.Event;

public class GameUpdateEvent extends Event {
    @Getter
    private static final GameUpdateEvent instance = new GameUpdateEvent();
}
