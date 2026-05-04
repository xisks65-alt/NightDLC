package dev.wh1tew1ndows.client.managers.events.other;

import dev.wh1tew1ndows.client.api.events.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SprintEvent extends CancellableEvent {

    private boolean sprint;

    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }

    public boolean getSpring() {
        return this.sprint;
    }


}

