package dev.wh1tew1ndows.client.managers.events.player;

import lombok.Getter;
import dev.wh1tew1ndows.client.api.events.Event;

public class PostUpdateEvent extends Event {
    @Getter
    private static final PostUpdateEvent instance = new PostUpdateEvent();
}