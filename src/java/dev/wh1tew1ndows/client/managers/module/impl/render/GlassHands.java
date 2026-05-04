package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.draw.RenderFactory;
import dev.wh1tew1ndows.client.utils.render.framebuffer.CustomFramebuffer;
import dev.wh1tew1ndows.client.utils.render.shader.impl.BloomShader;
import dev.wh1tew1ndows.client.utils.render.shader.impl.util.BlurShader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "GlassHands", category = Category.RENDER, desc = "Прозрачные руки")
public class GlassHands extends Module {
    public static GlassHands getInstance() {
        return Instance.get(GlassHands.class);
    }

    public final BooleanSetting glow = new BooleanSetting(this, "Глов", true);

    public final SliderSetting glowSize = new SliderSetting(this, "Сила глова", 4, 1, 5, 1).setVisible(() -> glow.getValue());

    public final SliderSetting saturationGlow = new SliderSetting(this, "Яркость глова", 1.0F, 0.0F, 4, 0.05F).setVisible(() -> glow.getValue());

    public final BooleanSetting blur = new BooleanSetting(this, "Блюр", true);

    public final SliderSetting saturationBlur = new SliderSetting(this, "Яркость блюра", 1.0F, 0.0F, 2.0F, 0.05F).setVisible(() -> blur.getValue());


    public final SliderSetting blurSIZE = new SliderSetting(this, "Сила блюра", 4, 1, 5, 1).setVisible(() -> blur.getValue());


    public CustomFramebuffer hands = new CustomFramebuffer(false).setLinear();
    public CustomFramebuffer mask = new CustomFramebuffer(false).setLinear();

    public final BloomShader BLOOM_SHADER = new BloomShader();
    public final BlurShader BLUR_SHADER = new BlurShader();

    @EventHandler
    public void event(Render3DEvent.PostWorld event) {

        if (glow.getValue()) {
            BLOOM_SHADER.addTask3D(() ->
                    mc.gameRenderer.renderHand(event.getMatrix(), event.getActiveRenderInfo(), event.getPartialTicks(), true, false, false));
            BLOOM_SHADER.draw(glowSize.getValue().intValue(), glowSize.getValue(), BloomShader.RenderType.CAMERA, saturationGlow.getValue());
        }
        if (blur.getValue()) {
            BLUR_SHADER.addTask3D(() ->
                    mc.gameRenderer.renderHand(event.getMatrix(), event.getActiveRenderInfo(), event.getPartialTicks(), true, false, false));
            BLUR_SHADER.draw(blurSIZE.getValue().intValue(), blurSIZE.getValue(), BlurShader.RenderType.CAMERA, saturationBlur.getValue());

        }
    }

    @EventHandler
    public void event(Render2DEvent event) {
        if (glow.getValue()) {
            RenderFactory.addTask(() ->
                    BLOOM_SHADER.draw(glowSize.getValue().intValue(), glowSize.getValue(), BloomShader.RenderType.DISPLAY, saturationGlow.getValue()));
        }
        if (blur.getValue()) {
            RenderFactory.addTask(() ->
                    BLUR_SHADER.draw(blurSIZE.getValue().intValue(), blurSIZE.getValue(), BlurShader.RenderType.DISPLAY, saturationBlur.getValue()));
        }
    }


}
