package dev.wh1tew1ndows.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import dev.wh1tew1ndows.client.api.events.Event;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActionEvent extends Event {
    private boolean sprintState;
}