package dev.wh1tew1ndows.client.managers.events.world;

import lombok.Getter;
import dev.wh1tew1ndows.client.api.events.Event;

public class WorldLoadEvent extends Event {
    @Getter
    private static final WorldLoadEvent instance = new WorldLoadEvent();
}