package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DLastEvent;
import dev.wh1tew1ndows.client.managers.events.render.WorldColorEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ColorSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(
        name = "CustomWorld",
        category = Category.RENDER,
        desc = "Кастомизация отображения мира"
)
public class CustomWorld extends Module {
    private final BooleanSetting customfog = new BooleanSetting(this, "Цветной туман", false);
    public final ModeSetting colorfog = new ModeSetting(this, "Цвет", "Тема", "Свой").setVisible(() -> this.customfog.getValue());
    private final ColorSetting color = (new ColorSetting(this, "Цвет тумана")).setVisible(() -> this.customfog.getValue() && colorfog.is("Свой"));
    private final SliderSetting distance = new SliderSetting(this, "Дистанция тумана", 1.0F, 0.0F, 1.0F, 0.01F);
    private final SliderSetting time = new SliderSetting(this, "Время суток", 16000.0F, 0.0F, 24000.0F, 500.0F);
    private final BooleanSetting customskin = new BooleanSetting(this, "Кастомный скин", false);
    private final ModeSetting typeskin = (new ModeSetting(this, "Текстурка скина", "Client", "1", "2")).setVisible(() -> this.customskin.getValue());

    public static CustomWorld getInstance() {
        return Instance.get(CustomWorld.class);
    }

    public void toggle() {
        super.toggle();
        mc.world.setRainStrength(0.0F);
        mc.world.getWorldInfo().setRaining(false);
    }

    public static ResourceLocation updateSkin(ResourceLocation var0, Entity var1) {
        if (var1 == mc.player && getInstance().customskin.getValue() && getInstance().isEnabled()) {
            var0 = new ResourceLocation("zetrix/texture/" + getInstance().typeskin.getValue().toLowerCase() + ".png");
        }

        return var0;
    }

    @EventHandler
    public void onEvent(WorldColorEvent var1) {
        float[] color = ColorUtil.getRGBf(this.color.getValue());
        if (colorfog.is("Тема")) {
            color = ColorUtil.getRGBf(ColorUtil.fade(0));
        }
        if (this.customfog.getValue()) {
            var1.setRed(color[0]);
            var1.setGreen(color[1]);
            var1.setBlue(color[2]);
        }

    }


    @EventHandler
    public void onEvent(MotionEvent var1) {
        if (mc.player.ticksExisted % 20 == 0) {
            mc.world.setRainStrength(0.0F);
            mc.world.getWorldInfo().setRaining(false);
        }

    }

    @EventHandler
    public void onEvent(Render3DLastEvent event) {
        mc.world.setDayTime(time.getValue().intValue());
    }

    @EventHandler
    public void onEvent(PacketEvent e) {
        if (e.isReceive()) {
            if (e.getPacket() instanceof SUpdateTimePacket) {
                e.setCancelled(true);
            } else if (e.getPacket() instanceof SChangeGameStatePacket wrapper) {
                if (wrapper.getState() == SChangeGameStatePacket.field_241765_b_ || wrapper.getState() == SChangeGameStatePacket.field_241766_c_) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Generated
    public BooleanSetting customfog() {
        return this.customfog;
    }

    @Generated
    public ColorSetting color() {
        return this.color;
    }

    @Generated
    public SliderSetting distance() {
        return this.distance;
    }

    @Generated
    public SliderSetting time() {
        return this.time;
    }

    @Generated
    public BooleanSetting customskin() {
        return this.customskin;
    }

    @Generated
    public ModeSetting typeskin() {
        return this.typeskin;
    }
}
