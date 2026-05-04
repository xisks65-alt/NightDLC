package dev.wh1tew1ndows.client.managers.events.other;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import dev.wh1tew1ndows.client.api.events.Event;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScreenResizeEvent extends Event {
    @Getter
    private static final ScreenResizeEvent instance = new ScreenResizeEvent();
    private int width;
    private int height;

    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
