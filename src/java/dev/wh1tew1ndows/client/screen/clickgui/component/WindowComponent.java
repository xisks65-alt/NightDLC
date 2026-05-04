package dev.wh1tew1ndows.client.screen.clickgui.component;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IScreen;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.screen.clickgui.ClickGuiScreen;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.joml.Vector2f;

@Data
@Accessors(fluent = true)
public abstract class WindowComponent implements IScreen {
    public Vector2f position = new Vector2f();
    public Vector2f size = new Vector2f();
    public final Animation hoverAnimation = new Animation();
    public final Animation hoverAnimation2 = new Animation();
    public final Animation expandAnimation = new Animation();

    public final float moduleFontSize = 7;
    public final float categoryFontSize = 8;

    public float outline = 2F;
    public float round = 4F;

    public boolean isHover(double mouseX, double mouseY) {
        return isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);
    }

    public int alpha() {
        return Math.round(alphaPC() * 255F);
    }

    public float alphaPC() {
        clickgui();
        return Mathf.clamp01(ClickGuiScreen.alpha.get());
    }

    public int backgroundColor() {
        return ColorUtil.getColor(24, 24, 24, alphaPC() / 3F);
    }

    public int backgroundColorS() {
        return ColorUtil.getColor(255, alphaPC() * 0.02F);
    }

    public int getWhite() {
        return ColorUtil.getColor(200, alpha());
    }

    public int backColor() {
        return ColorUtil.getColor(12, 12, 12, this.alphaPC() / 3.0F);
    }


    public int frontColor() {
        return ColorUtil.overCol(backColor(), accentColor(), 0.075F);
    }

    public int accentColor() {
        InterFace theme = InterFace.getInstance();
        return ColorUtil.multAlpha(theme.textColor(), alphaPC());
    }

    public boolean isHover(int mouseX, int mouseY) {
        return isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);
    }

    public ClickGuiScreen clickgui() {
        return Zetrix.inst().clickGui();
    }

    public Panel panel() {
        return clickgui().panel();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }
}
