package dev.wh1tew1ndows.client.utils.server;

import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;
import dev.wh1tew1ndows.client.api.events.Handler;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.utils.math.Mathf;

@Getter
public class ServerTPS extends Handler {
    private float TPS = 20;
    private float adjustTicks = 0;
    private long timestamp;

    private void update() {
        long delay = System.nanoTime() - timestamp;

        float maxTPS = 20;
        float rawTPS = maxTPS * (1e9f / delay);

        float boundedTPS = MathHelper.clamp(rawTPS, 0, maxTPS);

        TPS = (float) Mathf.round(boundedTPS);

        adjustTicks = boundedTPS - maxTPS;

        timestamp = System.nanoTime();
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (event.getPacket() instanceof SUpdateTimePacket) {
            update();
        }
    }
}