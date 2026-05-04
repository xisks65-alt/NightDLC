package dev.wh1tew1ndows.client.api.events;

import dev.wh1tew1ndows.client.Zetrix;

public class Event {
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public void hook() {
        Zetrix.eventHandler().post(this);
    }
}
