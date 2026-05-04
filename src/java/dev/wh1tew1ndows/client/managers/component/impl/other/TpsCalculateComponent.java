package dev.wh1tew1ndows.client.managers.component.impl.other;


import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;

@Getter
public class TpsCalculateComponent extends Component implements IMinecraft {
    public static TpsCalculateComponent getInstance() {
        return Instance.getComponent(TpsCalculateComponent.class);
    }

    private float TPS = 20;
    private float adjustTicks = 0;
    private long timestamp;

    @EventHandler
    private void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof SUpdateTimePacket) {
            long delay = System.nanoTime() - timestamp;

            float maxTPS = 20;
            float rawTPS = maxTPS * (1e9f / delay);

            float boundedTPS = MathHelper.clamp(rawTPS, 0, maxTPS);

            TPS = (float) limitDecimals(boundedTPS, 2);

            adjustTicks = boundedTPS - maxTPS;

            timestamp = System.nanoTime();

        }
    }

    public double limitDecimals(double value, int decimalPlaces) {
        return Math.round(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
    }

}
