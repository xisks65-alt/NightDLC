package dev.wh1tew1ndows.client.managers.events.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class AspectRatioEvent extends Event {
    private float aspectRatio;
}
