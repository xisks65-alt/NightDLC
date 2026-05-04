package dev.wh1tew1ndows.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public final class SwingAnimationEvent extends Event {
    private int animation;
}