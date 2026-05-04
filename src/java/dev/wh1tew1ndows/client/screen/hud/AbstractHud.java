package dev.wh1tew1ndows.client.screen.hud;

import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;

public abstract class AbstractHud {
    public Animation animation = new Animation();

    public void update(float value) {
        animation.update();
        animation.run(value, 0.6F, Easings.EXPO_OUT, true);
    }

    public float animValue() {
        return animation.get();
    }

    public int clientColor() {
        return ColorUtil.multAlpha(theme().clientColor(), animValue());
    }

    public float round() {
        return 4;
    }

    public int textColor() {
        return ColorUtil.multAlpha(theme().textColor(), animValue());
    }

    public int textWhite() {
        return ColorUtil.multAlpha(InterFace.getInstance().ThemeTextHud(), animValue());
    }

    public int textAccentColor() {
        return ColorUtil.multAlpha(theme().textAccentColor(), animValue());
    }

    public int iconColor() {
        return ColorUtil.multAlpha(theme().iconColor(), animValue());
    }

    public InterFace theme() {
        return InterFace.getInstance();
    }


}
