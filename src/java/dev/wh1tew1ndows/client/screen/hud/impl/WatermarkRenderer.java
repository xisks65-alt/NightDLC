package dev.wh1tew1ndows.client.screen.hud.impl;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.client.Constants;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.screen.hud.IRenderer;
import dev.wh1tew1ndows.client.utils.animation.NumberTransition;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.ПенисУтилита;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
public class WatermarkRenderer implements IRenderer {

    float title_width = 50;
    float username_width = 100;
    float server_width = 100;
    float fps_width = 50;
    float ping_width = 50;

    float tempFps = 0;
    float tempPing = 0;
    private float smoothY = -1.0F;
    private long lastTimeNs = 0L;
    private static final float SMOOTH_SPEED = 20.0F;

    @EventHandler
    public void render(Render2DEvent e) {
        MatrixStack ms = e.getMatrix();



            ms.push();

            float pos = 8;

            tempFps = NumberTransition.result(tempFps, Minecraft.getDebugFPS());
            String fps = ((int) tempFps) + ColorFormatting.getColor(ColorUtil.getColor(100)) + "fps";

            ПенисУтилита.ЕбатьПенка(ms, pos + 1, pos, 60 + 10 + 5 + Fonts.MONTSERRAT_BOLD.getWidth(fps, 5.5F), 14, 1, 5);

            RenderUtil.Rounded.smooth(ms, pos + 1, pos, 14, 14, ColorUtil.getColor(20, 0.2F), Round.of(5, 5, 0, 0));

            RectUtil.drawRect(ms, pos + 14.5F, pos, 0.8F, 14, ColorUtil.getColor(255, 0.02F));


            Fonts.MONTSERRAT_BOLD.draw(ms, "Z", pos + 4, pos + 2.5F, ColorUtil.fade(), 9F);

            Fonts.MONTSERRAT_BOLD.draw(ms, "Zetrix Client", pos + 20, pos + 3.5F, ColorUtil.getColor(180), 5.5F);

            RectUtil.drawRect(ms, pos + 14.5F + Fonts.MONTSERRAT_BOLD.getWidth("Zetrix Client", 5.5F) + 8.5F, pos, 1, 14, ColorUtil.getColor(255, 0.02F));


            Fonts.ICON_ESSENS.draw(ms, "m", pos + 14.5F + Fonts.MONTSERRAT_BOLD.getWidth("Zetrix Client", 5.5F) + 8.5F + 5, pos + 4.5F, ColorUtil.fade(), 5.5F);

            Fonts.MONTSERRAT_BOLD.draw(ms, fps,
                    pos + 14.5F + Fonts.MONTSERRAT_BOLD.getWidth("Zetrix Client", 5.5F)
                            + 8.5F + 5 + 8.5F, pos + 3.5F, ColorUtil.getColor(180), 5.5F);


            String pingString = PlayerUtil.getPing(mc.player) + " ms";

            double playerX = Mathf.round(mc.player.getPosX(), 1);
            double playerY = Mathf.round(mc.player.getPosY(), 1);
            double playerZ = Mathf.round(mc.player.getPosZ(), 1);
            String posString = playerX + ", " + playerY + ", " + playerZ;

            float fontSize = 8;
            float startX = 7;

            boolean isChatOpen = mc.currentScreen instanceof ChatScreen;

            float targetY = mc.getMainWindow().getScaledHeight() - fontSize - 15;
            if (isChatOpen) targetY -= 10;

            float dt = this.computeDtSeconds();
            if (this.smoothY < 0) this.smoothY = targetY;
            this.smoothY = this.smoothTowards(this.smoothY, targetY, dt, 20.0F);

            float y = this.smoothY;

            int mainColor = ColorUtil.getColor(200);
            int whiteColor = ColorUtil.getColor(150);
            int grayColor = ColorUtil.getColor(90, 90, 90);

            Fonts.MONTSERRAT_BOLD.draw(ms, "Ping » ", startX, y, mainColor, fontSize);
            float x = startX + Fonts.MONTSERRAT_BOLD.getWidth("Ping » ", fontSize);
            Fonts.MONTSERRAT_BOLD.draw(ms, pingString, x, y, whiteColor, fontSize);

            float y2 = y + fontSize + 2;

            Fonts.MONTSERRAT_BOLD.draw(ms, "Pos » ", startX, y2, mainColor, fontSize);
            x = startX + Fonts.MONTSERRAT_BOLD.getWidth("Pos » ", fontSize);
            Fonts.MONTSERRAT_BOLD.draw(ms, posString, x, y2, whiteColor, fontSize);

            ms.pop();


    }

    private float smoothTowards(float var1, float ms, float var3, float var4) {
        if (Float.isFinite(var3) && !(var3 <= 0.0F)) {
            float var5 = 1.0F - (float) Math.exp(-var4 * var3);
            return var1 + (ms - var1) * var5;
        } else {
            return ms;
        }
    }

    private float computeDtSeconds() {
        long var1 = System.nanoTime();
        if (this.lastTimeNs == 0L) {
            this.lastTimeNs = var1;
            return 0.016666668F;
        } else {
            long var3 = var1 - this.lastTimeNs;
            this.lastTimeNs = var1;
            double var5 = Math.min(Math.max((double) var3 / (double) 1.0E9F, 0.0F), 0.1);
            return (float) var5;
        }
    }

    private void renderPart(String icon, String text, float pos, float x, float y, float width, float height, MatrixStack ms) {
        int alpha = 255;
        float posx = 45;

        InterFace.getInstance().drawClientRect(ms, posx + x, pos + y, width, height, 1, 5);

        Fonts.ICON_ESSENS.draw(ms, icon, posx + x + 4, pos + y + 2 + 1.7F, InterFace.getInstance().iconColor(), 9);
        Fonts.MONTSERRAT_MEDIUM.draw(ms, text, posx + x + 15, pos + y + 2 + 1.5F, ColorUtil.getColor(235), 7);
    }

}
