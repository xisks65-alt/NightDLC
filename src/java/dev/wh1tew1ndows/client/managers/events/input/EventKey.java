package dev.wh1tew1ndows.client.managers.events.input;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventKey {
    int key;

    public boolean isKeyDown(int key) {
        return this.key == key;
    }
}

