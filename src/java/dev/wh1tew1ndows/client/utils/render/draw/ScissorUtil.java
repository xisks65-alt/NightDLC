package dev.wh1tew1ndows.client.utils.render.draw;


import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.opengl.GL11;

import static dev.wh1tew1ndows.client.api.interfaces.IMinecraft.mw;

@UtilityClass
public class ScissorUtil {
    //   public void enable() {
//     GL11.glEnable(GL11.GL_SCISSOR_TEST);
// }

// public void disable() {
//     GL11.glDisable(GL11.GL_SCISSOR_TEST);
// }

    public void scissor(MainWindow window, double x, double y, double width, double height) {
        if (x + width == x || y + height == y || x < 0 || y + height < 0) return;
        final double scaleFactor = window.getScaleFactor();
        GL11.glScissor((int) Math.round(x * scaleFactor), (int) Math.round((window.getScaledHeight() - (y + height)) * scaleFactor), Math.max(1, (int) Math.round(width * scaleFactor)), Math.max(1, (int) Math.round(height * scaleFactor)));
    }

    private static final float MIN_SIZE = 1.0F;

    public void enable() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public void disable() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void scissor(float x, float y, float width, float height) {
        if (width <= 0 || height <= 0 || x + width == x || y + height == y || x < 0 || y < 0) {
            return;
        }

        final double scaleFactor = mw.getScaleFactor();
        int scaledWidth = (int) Math.max(MIN_SIZE, Math.round(width * scaleFactor));
        int scaledHeight = (int) Math.max(MIN_SIZE, Math.round(height * scaleFactor));
        int scaledX = (int) Math.round(x * scaleFactor);
        int scaledY = (int) Math.round((mw.getScaledHeight() - (y + height)) * scaleFactor);

        GL11.glScissor(scaledX, scaledY, scaledWidth, scaledHeight);
    }

    public void scissor(MatrixStack matrix, float x, float y, float width, float height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        Matrix4f matrix4f = matrix.getLast().getMatrix();

        float[][] corners = {
                {x, y},                   // Левый верхний
                {x + width, y},           // Правый верхний
                {x, y + height},          // Левый нижний
                {x + width, y + height}   // Правый нижний
        };

        float[][] transformedCorners = new float[4][2];
        for (int i = 0; i < 4; i++) {
            float cornerX = corners[i][0];
            float cornerY = corners[i][1];
            float transformedX = matrix4f.m00 * cornerX + matrix4f.m01 * cornerY + matrix4f.m03;
            float transformedY = matrix4f.m10 * cornerX + matrix4f.m11 * cornerY + matrix4f.m13;
            transformedCorners[i] = new float[]{transformedX, transformedY};
        }

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (float[] corner : transformedCorners) {
            minX = Math.min(minX, corner[0]);
            maxX = Math.max(maxX, corner[0]);
            minY = Math.min(minY, corner[1]);
            maxY = Math.max(maxY, corner[1]);
        }

        double scaleFactor = mw.getScaleFactor();
        int scissorX = (int) Math.round(minX * scaleFactor);
        int scissorWidth = (int) Math.max(MIN_SIZE, Math.round((maxX - minX) * scaleFactor));
        int scissorY = (int) Math.round((mw.getScaledHeight() - maxY) * scaleFactor);
        int scissorHeight = (int) Math.max(MIN_SIZE, Math.round((maxY - minY) * scaleFactor));

        if (scissorWidth <= 0 || scissorHeight <= 0) {
            return;
        }

        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }


}