package dev.wh1tew1ndows.client.utils.render.scroll;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import lombok.Getter;
import lombok.Setter;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;

@Getter
@Setter
public class ScrollUtil implements IMinecraft {
    private float target, max, wheel;
    private boolean enabled;
    private boolean autoReset;
    private float speed = 5F;
    private final Animation animation = new Animation();

    public ScrollUtil() {
        enabled = true;
        autoReset = true;
    }

    public void update() {
        if (enabled) {
            double wheel = mc.mouseHelper.getWheel() * (speed * 10F);
            double stretch = 0;
            target = (float) Math.min(Math.max(target + wheel / 2, max - (wheel == 0 ? 0 : stretch)), (wheel == 0 ? 0 : stretch));
            resetWheel();
        }
        if (autoReset) resetWheel();
        animation.update();
        animation.run(target, 0.25F, Easings.QUAD_OUT, true);
        wheel = (float) animation.getValue();
        
    }

    public void renderV(MatrixStack matrixStack, Vector2f position, float maxHeight, float alpha) {
        float percentage = (getWheel() / getMax());
        float barHeight = maxHeight - ((getMax() / (getMax() - maxHeight)) * maxHeight);
        boolean allowed = (barHeight < maxHeight);
        if (!allowed) return;
        float scrollX = position.x;
        float scrollY = position.y + (maxHeight * percentage) - (barHeight * percentage);
        RectUtil.drawRect(matrixStack, scrollX, scrollY, 0.5F, barHeight, ColorUtil.getColor(255, alpha));

    }

    public void renderGradienV(MatrixStack matrixStack, Vector2f position, float maxHeight, float alpha) {
        float percentage = (getWheel() / getMax());
        float barHeight = maxHeight - ((getMax() / (getMax() - maxHeight)) * maxHeight);
        boolean allowed = (barHeight < maxHeight);
        if (!allowed) return;
        float scrollX = position.x;
        float scrollY = position.y + (maxHeight * percentage) - (barHeight * percentage);
        // RectUtil.drawRect(matrixStack, scrollX, scrollY, 0.5F, barHeight, ColorUtil.getColor(255, alpha));

        RenderUtil.Rounded.smooth(matrixStack, scrollX, scrollY, 2, barHeight, ColorUtil.replAlpha(InterFace.getInstance().clientColor(), alpha), Round.of(1));
        RenderUtil.Shadow.drawShadow(matrixStack, scrollX, scrollY - 1, 2, barHeight + 2, 7, 3, ColorUtil.replAlpha(InterFace.getInstance().clientColor(), alpha));
    }

    public void renderH(MatrixStack matrixStack, Vector2f position, float maxWidth, float alpha) {
        float percentage = (getWheel() / getMax());
        float barWidth = maxWidth - ((getMax() / (getMax() - maxWidth)) * maxWidth);
        boolean allowed = (barWidth < maxWidth);
        if (!allowed) return;
        float scrollX = position.x + (maxWidth * percentage) - (barWidth * percentage);
        float scrollY = position.y;
        RectUtil.drawRect(matrixStack, scrollX, scrollY, barWidth, 0.5F, ColorUtil.getColor(255, alpha));
    }

    public void reset() {
        this.animation.set(this.wheel = this.target = 0F);
    }

    private void resetWheel() {
        mc.mouseHelper.setWheel(0F);
    }

    public void setMax(float max, float value) {
        this.max = -max + value;
    }
}