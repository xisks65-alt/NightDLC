package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.alt.Scissor;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil.Rounded;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.util.HashMap;
import java.util.Map;

public class ModeSettingComponent extends SettingComponent {
    private final ModeSetting value;
    private final Map<String, Animation> animations = new HashMap();
    Animation openanim = new Animation();

    public ModeSettingComponent(ModeSetting var1) {
        super(var1);
        this.value = var1;

        for (String var3 : var1.getValues()) {
            this.animations.put(var3, new Animation());
        }

    }

    public void resize(Minecraft var1, int var2, int var3) {
    }

    public void init() {
    }

    public void render(MatrixStack var1, int var2, int var3, float var4) {
        super.render(var1, var2, var3, var4);
        float var5 = 8;
        float var6 = 0.0F;
        float var7 = 0.0F;
        float var8 = 7;
        float var9 = 2.5F;
        this.openanim.update();
        Scissor.push();
        Scissor.setFromComponentCoordinates((int) (position.x + margin / 2), (int) (position.y + margin), (int) (size.x - margin), 10);
        Fonts.MONTSERRAT_MEDIUM.draw(var1, value.getName(), position.x + margin / 2, position.y + margin, ColorUtil.replAlpha(getWhite(), alpha()), 7.5F);
        Scissor.unset();
        Scissor.pop();
        for (String var11 : this.value.getValues()) {
            Animation var12 = this.animations.computeIfAbsent(var11, k -> new Animation());
            var12.update();
            var12.run(var11.equals(this.value.getValue()) ? (double) 1.0F : (double) 0.0F, 0.25F, Easings.LINEAR);
            float var13 = Fonts.MONTSERRAT_MEDIUM.getWidth(var11, fontSize) + var8;
            if (var6 + var13 >= this.size.x - this.margin * 2.0F) {
                var6 = 0.0F;
                var7 += 7F + var8;
            }

            Rounded.smooth(var1, this.position.x + this.margin + var6 - var9, this.position.y + this.margin + (this.margin / 2) + var5 + var7 + this.margin / 2.0F, Fonts.MONTSERRAT_MEDIUM.getWidth(var11, fontSize) + var9 * 2.0F, 7F + var9 * 2.0F, ColorUtil.overCol(this.backColor(), ColorUtil.multAlpha(this.accentColor(), 0.1F), var12.get()), Round.of(3.0F));
            Rounded.roundedOutline(var1, this.position.x + this.margin + var6 - var9, this.position.y + this.margin + (this.margin / 2) + var5 + var7 + this.margin / 2.0F, Fonts.MONTSERRAT_MEDIUM.getWidth(var11, fontSize) + var9 * 2.0F, 7F + var9 * 2.0F, 1, ColorUtil.overCol(0, ColorUtil.multAlpha(this.accentColor(), 0.2F), var12.get()), Round.of(3.0F));
            Fonts.MONTSERRAT_MEDIUM.draw(var1, var11, this.position.x + this.margin + var6, this.position.y + this.margin + (this.margin / 2) + var5 + var7 + this.margin / 2.0F + var9  + 0.1F, this.getWhite(), fontSize);
            var6 += var13;
        }

        this.size.y = this.margin + var5 + this.margin + var7 + 7.5F + this.margin * 2.0F;
    }

    public boolean mouseClicked(double var1, double var3, int var5) {
        float var6 = 0.0F;
        float var7 = 0.0F;
        float var8 = 7;
        float var9 = 2.5F;
        float offsetY = 8; // Соответствует var5 в render

        for (String var11 : this.value.getValues()) {
            float var12 = Fonts.MONTSERRAT_MEDIUM.getWidth(var11, fontSize) + var8;
            if (var6 + var12 >= this.size.x - this.margin * 2.0F) {
                var6 = 0.0F;
                var7 += 7F + var8;
            }

            if (this.isLClick(var5) && this.isHover(var1, var3, this.position.x + this.margin + var6 - var9, this.position.y + this.margin + (this.margin / 2) + offsetY + var7 + this.margin / 2.0F, Fonts.MONTSERRAT_MEDIUM.getWidth(var11, fontSize) + var9 * 2.0F, 7F + var9 * 2.0F)) {
                this.value.set(var11);
            }

            var6 += var12;
        }

        return false;
    }

    public boolean mouseReleased(double var1, double var3, int var5) {
        return false;
    }

    public boolean keyPressed(int var1, int var2, int var3) {
        return false;
    }

    public boolean keyReleased(int var1, int var2, int var3) {
        return false;
    }

    public boolean charTyped(char var1, int var2) {
        return false;
    }

    public void onClose() {
    }
}