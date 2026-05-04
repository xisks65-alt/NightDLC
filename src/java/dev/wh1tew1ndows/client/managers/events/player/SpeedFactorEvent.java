package dev.wh1tew1ndows.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class SpeedFactorEvent extends Event {
    private float speed;
}