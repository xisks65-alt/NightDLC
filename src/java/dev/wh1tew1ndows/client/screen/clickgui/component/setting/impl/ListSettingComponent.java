//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wh1tew1ndows.client.screen.clickgui.component.setting.impl;

import dev.wh1tew1ndows.client.managers.module.settings.impl.ListSetting;
import dev.wh1tew1ndows.client.screen.clickgui.component.setting.SettingComponent;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil.Rounded;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

public class ListSettingComponent extends SettingComponent {
    private final ListSetting<?> value;

    public ListSettingComponent(ListSetting<?> var1) {
        super(var1);
        this.value = var1;
    }

    public void resize(Minecraft var1, int var2, int var3) {
    }

    public void init() {
    }

    public void render(MatrixStack var1, int var2, int var3, float var4) {
        super.render(var1, var2, var3, var4);
        float var5 = this.drawName(var1, var2, var3, this.size.x);
        float var6 = 0.0F;
        float var7 = 0.0F;
        float var8 = 3.0F;
        float var9 = 1.0F;
        Rounded.smooth(var1, this.position.x, this.position.y + this.margin + var5 + (this.margin / 2), this.size.x, this.size.y - (this.margin + var5 + this.margin) - this.margin - this.margin / 2.0F, this.backColor(), Round.of(4.0F));

        for (Object var11 : this.value.values) {
            float var12 = Fonts.MONTSERRAT_MEDIUM.getWidth(var11.toString(), fontSize) + var8;
            if (var6 + var12 >= this.size.x - this.margin * 2.0F) {
                var6 = 0.0F;
                var7 += fontSize + var8;
            }

            Fonts.MONTSERRAT_MEDIUM.draw(var1, var11.toString(), this.position.x + this.margin + var6, this.position.y + (this.margin / 2) + var5 + this.margin + var7 + this.margin / 2.0F + var9 - 0.6F, this.value.getValue().equals(var11) ? this.accentColor() : this.getWhite(), fontSize);
            var6 += var12;
        }

        this.size.y = this.margin + var5 + this.margin + var7 + fontSize + this.margin * 2.0F + this.margin;
    }

    public boolean mouseClicked(double var1, double var3, int var5) {
        float var6 = 0.0F;
        float var7 = 0.0F;
        float var8 = 3.0F;
        float var9 = 1.0F;

        for (Object var11 : this.value.values) {
            float var12 = Fonts.MONTSERRAT_MEDIUM.getWidth(var11.toString(), fontSize) + var8;
            if (var6 + var12 >= this.size.x - this.margin * 2.0F) {
                var6 = 0.0F;
                var7 += fontSize + var8;
            }

            if (this.isLClick(var5) && this.isHover(var1, var3, this.position.x + this.margin + var6 - var9, this.position.y + (this.margin / 2) + this.valueHeight() + this.margin + var7 + this.margin / 2.0F, Fonts.MONTSERRAT_MEDIUM.getWidth(var11.toString(), fontSize) + var9 * 2.0F, fontSize + var9 * 2.0F)) {
                this.value.setAsObject(var11);
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