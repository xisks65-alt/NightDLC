package dev.wh1tew1ndows.client.managers.events.other;

import com.sheluvparis.authenticator.lib.packet.type.ServerPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@RequiredArgsConstructor
public final class BackendPacketEvent extends Event {
    private final ServerPacket packet;
}