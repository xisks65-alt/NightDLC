package dev.wh1tew1ndows.client.managers.module.impl.combat;

import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.AxisAlignedBB;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Resolver", desc = "Предсказывает прошлые позиции игроков для повышения точности атаки", category = Category.COMBAT)

public class Resolver extends Module {
    public static Resolver getInstance() {
        return Instance.get(Resolver.class);
    }


    public final SliderSetting backTick = new SliderSetting(this, "Бектики", 2F, 1F, 50F, 1F);

    public AxisAlignedBB resolvedBox;

    public static class Position {
        private final double x;
        private final double y;
        private final double z;
        public int ticks;
        final Resolver kill = Resolver.getInstance();

        public Position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean shouldRemove() {
            return ticks++ > kill.backTick.getValue();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }
    }
}
