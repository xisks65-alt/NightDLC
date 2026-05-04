package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.alt.Scissor;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil.Rounded;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

public class MultiBooleanSettingComponent extends SettingComponent {
    private final MultiBooleanSetting value;

    public MultiBooleanSettingComponent(MultiBooleanSetting var1) {
        super(var1);
        this.value = var1;
    }

    public void resize(Minecraft var1, int var2, int var3) {
    }

    public void init() {
    }

    public void render(MatrixStack var1, int var2, int var3, float var4) {
        super.render(var1, var2, var3, var4);
        int total = 0;
        int active = 0;
        for (BooleanSetting setting : this.value.getValues()) {
            if (setting.getVisible().get()) {
                total++;
                if (setting.getValue()) active++;
            }
        }
        ///  Fonts.MONTSERRAT_MEDIUM.getWidth(active + " из " + total, fontSize)
        Fonts.MONTSERRAT_MEDIUM.drawRight(var1, active + " из " + total, position.x + size.x, position.y + margin, getWhite(), 7.5F);
        Scissor.push();
        Scissor.setFromComponentCoordinates((int) (position.x + margin / 2), (int) (position.y + margin ), (int) (size.x - margin - Fonts.MONTSERRAT_MEDIUM.getWidth(active + " из " + total, 7.5F)), 10);
        Fonts.MONTSERRAT_MEDIUM.draw(var1, value.getName(), position.x + margin / 2, position.y + margin, ColorUtil.replAlpha(getWhite(), alpha()), 7.5F);
        Scissor.unset();
        Scissor.pop();
        float var5 = 8;
        float var6 = 0.0F;
        float var7 = 0.0F;
        float var8 = 7;
        float var9 = 2.5F;

        for (BooleanSetting var11 : this.value.getValues()) {
            if (var11.getVisible().get()) {
                var11.getAnimation().update();
                var11.getAnimation().run(this.value.getValue(var11.getName()) ? (double) 1.0F : (double) 0.0F, 0.25F, Easings.LINEAR, true);
                float var12 = Fonts.MONTSERRAT_MEDIUM.getWidth(var11.getName(), 6.5F) + var8;
                if (var6 + var12 >= this.size.x - this.margin * 2.0F) {
                    var6 = 0.0F;
                    var7 += 7.0F + var8;
                }

                float pillX = this.position.x + this.margin + var6 - var9;
                float pillY = this.position.y + (this.margin / 2) + this.margin + var5 + var7 + this.margin / 2.0F;
                float pillW = Fonts.MONTSERRAT_MEDIUM.getWidth(var11.getName(), 6.5F) + var9 * 2.0F;
                float pillH = 7.0F + var9 * 2.0F;
                float selVal = (float) var11.getAnimation().get();
                if (selVal > 0.01f) {
                    RenderUtil.Shadow.drawShadow(var1, pillX, pillY, pillW, pillH, 4,
                            ColorUtil.replAlpha(this.accentColor(), (int)(this.alpha() * 0.25f * selVal)));
                }
                Rounded.smooth(var1, pillX, pillY, pillW, pillH, ColorUtil.overCol(this.backColor(), ColorUtil.multAlpha(this.accentColor(), 0.15F), selVal), Round.of(3.0F));
                Rounded.roundedOutline(var1, pillX, pillY, pillW, pillH, 1, ColorUtil.overCol(0, ColorUtil.multAlpha(this.accentColor(), 0.3F), selVal), Round.of(3.0F));
                int textCol = ColorUtil.overCol(this.getWhite(), this.accentColor(), selVal * 0.5f);
                Fonts.MONTSERRAT_MEDIUM.draw(var1, var11.getName(), this.position.x + this.margin + var6, pillY + var9 + 0.1F, textCol, 6.5F);
                var6 += var12;
            }
        }

        this.size.y = this.margin + var5 + this.margin + var7 + 7.5F + this.margin * 2.0F;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float offsetX = 0.0F;
        float offsetY = 0.0F;
        float spacingX = 7;
        float padding = 2.5F;
        float startY = 8; // такое же как var5 в render

        for (BooleanSetting setting : this.value.getValues()) {
            if (setting.getVisible().get()) {
                float elementWidth = Fonts.MONTSERRAT_MEDIUM.getWidth(setting.getName(), 6.5F) + spacingX;

                // переход на новую строку
                if (offsetX + elementWidth >= this.size.x - this.margin * 2.0F) {
                    offsetX = 0.0F;
                    offsetY += 7.0F + spacingX;
                }

                // те же координаты, что и в Rounded.smooth()
                float x = this.position.x + this.margin + offsetX - padding;
                float y = this.position.y + (this.margin / 2) + this.margin + startY + offsetY + this.margin / 2.0F;
                float w = Fonts.MONTSERRAT_MEDIUM.getWidth(setting.getName(), 6.5F) + padding * 2.0F;
                float h = 7.0F + padding * 2.0F;

                if (this.isLClick(button) && this.isHover(mouseX, mouseY, x, y, w, h)) {
                    this.value.get(setting.getName()).set(!setting.getValue());
                }

                offsetX += elementWidth;
            }
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

