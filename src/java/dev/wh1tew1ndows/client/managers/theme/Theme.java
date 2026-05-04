package dev.wh1tew1ndows.client.managers.theme;

import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;

import java.awt.*;

public enum Theme {
    ICY_LILAC("Ледяная сирень", new Color(160, 176, 239)),
    TWILIGHT_VIOLET("Лиловый сумрак", new Color(156, 138, 223)),
    CLEAR_SKY("Чистое небо", new Color(127, 186, 221)),
    DUNES("Песчаное тепло", new Color(214, 212, 168)),
    POWDER_ROSE("Пудровая роза", new Color(221, 149, 177)),
    SUNSET_CORAL("Закатный коралл", new Color(221, 127, 129)),
    CUSTOM("Кастомная", new Color(160, 176, 239), new Color(200, 100, 200));


    private final String name;
    private Color color1;
    private Color color2;

    public Animation animation = new Animation();

    Theme(String name, Color color1, Color color2) {
        this.name = name;
        this.color1 = color1;
        this.color2 = color2;
    }

    Theme(String name, Color color1) {
        this.name = name;
        this.color1 = color1;
        this.color2 = color1;
    }

    public String getName() {
        return name;
    }

    public Color getColor1() {

        return color1;
    }

    public Color getColor2() {
        
        return color2;
    }


    public void setColor1(Color color1) {
        if (this == CUSTOM) {
            // For CUSTOM theme, we should not call CustomThemeManager here
            // as it would cause recursion. The colors are managed by CustomThemeManager.
            // This method should only be called from CustomThemeManager.updateThemeColors()
            this.color1 = color1;
        } else {
            this.color1 = color1;
        }
    }

    public void setColor2(Color color2) {
        if (this == CUSTOM) {
            // For CUSTOM theme, we should not call CustomThemeManager here
            // as it would cause recursion. The colors are managed by CustomThemeManager.
            // This method should only be called from CustomThemeManager.updateThemeColors()
            this.color2 = color2;
        } else {
            this.color2 = color2;
        }
    }


    public Color getColor(float offset) {
        return RenderUtil.Colors.TwoColoreffect(getColor1(), getColor2(), (Math.abs(System.currentTimeMillis() / 10L) / 100.0 + 6.68F * (offset * 0.1) / 60));
    }
}
