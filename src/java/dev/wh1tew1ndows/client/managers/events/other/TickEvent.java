package dev.wh1tew1ndows.client.managers.events.other;

import lombok.Getter;
import dev.wh1tew1ndows.client.api.events.Event;

public class TickEvent extends Event {
    @Getter
    private static final TickEvent instance = new TickEvent();
}
