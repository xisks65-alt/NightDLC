package dev.wh1tew1ndows.client.managers.events.player;

import dev.wh1tew1ndows.client.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SprintLockEvent extends Event {
    private boolean unlock;

    public void unlockSprint() {
        this.unlock = true;
    }
}

