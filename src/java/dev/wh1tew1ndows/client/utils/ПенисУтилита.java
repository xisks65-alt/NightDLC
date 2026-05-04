package dev.wh1tew1ndows.client.utils;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.shader.impl.BlurShader;
import lombok.experimental.UtilityClass;
import net.mojang.blaze3d.matrix.MatrixStack;

@UtilityClass
public class ПенисУтилита implements IMinecraft {


    public void КрутойПенаРект(MatrixStack матрицахуятица, float залупаХ, float залупаY, float залупаШирина, float залупаДлина, float ебанаяАнимкаХУйлитка) {

        float round = 5;

        if (InterFace.getInstance().blur.getValue()) {
            if(InterFace.getInstance().shadow.getValue())
            RenderUtil.Shadow.drawShadow(матрицахуятица, залупаХ - round / 2, залупаY - round / 2, залупаШирина + round, залупаДлина + round, round * 2, ебанаяАнимкаХУйлитка,
                    ColorUtil.replAlpha(ColorUtil.getColor(0), (int) (255 * ебанаяАнимкаХУйлитка)), Round.of(round));

            RenderUtil.bindTexture(BlurShader.INSTANCE.getBuffer().framebufferTexture);
            RenderUtil.Texture.customRound(матрицахуятица, RenderUtil.Texture.ShaderType.BLUR, залупаХ, залупаY, залупаШирина, залупаДлина, ебанаяАнимкаХУйлитка, 0, 0, 0, 0, Round.of(round));
        }


        RenderUtil.Rounded.smooth(матрицахуятица, залупаХ, залупаY, залупаШирина, залупаДлина, ColorUtil.getColor(15, (int) (100 * ебанаяАнимкаХУйлитка)), Round.of(round));

        RenderUtil.Rounded.roundedOutline(матрицахуятица, залупаХ, залупаY, залупаШирина, залупаДлина, 1,
                ColorUtil.replAlpha(ColorUtil.fade(255), (int) (25 * ебанаяАнимкаХУйлитка)), Round.of(round));

    }


    public void ЕбатьПенка(MatrixStack матрицахуятица, float залупаХ, float залупаY, float залупаШирина, float залупаДлина, float ебанаяАнимкаХУйлитка, float ебанаоезакругления) {

        float round = ебанаоезакругления;

        if (InterFace.getInstance().blur.getValue()) {
            if(InterFace.getInstance().shadow.getValue())
            RenderUtil.Shadow.drawShadow(матрицахуятица, залупаХ - round / 2, залупаY - round / 2, залупаШирина + round, залупаДлина + round, round * 2, ебанаяАнимкаХУйлитка,
                    ColorUtil.replAlpha(ColorUtil.getColor(0), (int) (255 * ебанаяАнимкаХУйлитка)), Round.of(round));

            RenderUtil.bindTexture(BlurShader.INSTANCE.getBuffer().framebufferTexture);
            RenderUtil.Texture.customRound(матрицахуятица, RenderUtil.Texture.ShaderType.BLUR, залупаХ, залупаY, залупаШирина, залупаДлина, ебанаяАнимкаХУйлитка, 0, 0, 0, 0, Round.of(round));
        }


        RenderUtil.Rounded.smooth(матрицахуятица, залупаХ, залупаY, залупаШирина, залупаДлина, ColorUtil.getColor(15, (int) (100 * ебанаяАнимкаХУйлитка)), Round.of(round));

        RenderUtil.Rounded.roundedOutline(матрицахуятица, залупаХ, залупаY, залупаШирина, залупаДлина, 1,
                ColorUtil.replAlpha(ColorUtil.fade(255), (int) (25 * ебанаяАнимкаХУйлитка)), Round.of(round));


    }


}
