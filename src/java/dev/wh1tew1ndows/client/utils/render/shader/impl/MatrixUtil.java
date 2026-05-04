package dev.wh1tew1ndows.client.utils.render.shader.impl;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;

@UtilityClass
public class MatrixUtil {
    public void rotate(MatrixStack matrix, float x, float y, float z, Quaternion rotation) {
        matrix.translate(x, y, z);
        matrix.rotate(rotation);
        matrix.translate(-x, -y, -z);
    }

    public static MatrixStack matrixFrom(MatrixStack matrices, ActiveRenderInfo camera) {

        matrices.rotate(Vector3f.XP.rotationDegrees(0));
        matrices.rotate(Vector3f.YP.rotationDegrees(0));

        return matrices;
    }

    public float lerp(float var1, float var2, float var3) {
        return var1 + var3 * (var2 - var1);
    }

    public void rotate2D(MatrixStack matrix, float x, float y, float rotation) {
        rotate(matrix, x, y, 0, Vector3f.XP.rotationDegrees(rotation));
    }

    private void scale(MatrixStack matrix, float x, float y, float z, Vector3f scale) {
        matrix.translate(x, y, z);
        matrix.scale(scale.getX(), scale.getY(), scale.getZ());
        matrix.translate(-x, -y, -z);
    }

    public void scale2D(MatrixStack matrix, float x, float y, float scale) {
        scale(matrix, x, y, 0, new Vector3f(scale, scale, 0));
    }

    public void startScale(float x, float y, float scale) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(-x, -y, 0);
    }

    public void endScale() {
        RenderSystem.popMatrix();
    }
}
