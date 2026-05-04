package dev.wh1tew1ndows.client.managers.events.other;

import dev.wh1tew1ndows.client.api.events.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.IPacket;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PacketEvent extends CancellableEvent {
    private final Action action;
    private final IPacket<?> packet;

    public boolean isSend() {
        return this.getAction().equals(Action.SEND);
    }

    public boolean isReceive() {
        return this.getAction().equals(Action.RECEIVE);
    }

    public enum Action {
        SEND, RECEIVE
    }
}