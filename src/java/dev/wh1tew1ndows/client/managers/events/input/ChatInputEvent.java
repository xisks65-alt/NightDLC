package dev.wh1tew1ndows.client.managers.events.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.wh1tew1ndows.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public final class ChatInputEvent extends CancellableEvent {
    private String message;
}