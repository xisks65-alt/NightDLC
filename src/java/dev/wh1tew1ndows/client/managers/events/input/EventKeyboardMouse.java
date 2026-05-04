package dev.wh1tew1ndows.client.managers.events.input;

import dev.wh1tew1ndows.client.api.events.Event;

public class EventKeyboardMouse extends Event {
    private int key;

    public EventKeyboardMouse(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }

    public boolean isKeyDown(int key) {
        return this.key == key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
