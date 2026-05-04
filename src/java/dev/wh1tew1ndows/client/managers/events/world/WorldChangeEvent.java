package dev.wh1tew1ndows.client.managers.events.world;

import lombok.Getter;
import dev.wh1tew1ndows.client.api.events.Event;

public final class WorldChangeEvent extends Event {
    @Getter
    private static final WorldChangeEvent instance = new WorldChangeEvent();
}