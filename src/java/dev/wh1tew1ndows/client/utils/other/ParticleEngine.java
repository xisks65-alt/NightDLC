package dev.wh1tew1ndows.client.utils.other;


import dev.wh1tew1ndows.client.managers.module.impl.render.Particles;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Vector2f;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector4i;

import java.util.ArrayList;

import static dev.wh1tew1ndows.client.api.interfaces.IMinecraft.mc;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR;


public class ParticleEngine {
    static class Particle {
        Vector2f pos;
        Vector2f mot;
        public int maxTime;
        public int time;

        public Particle(Vector2f pos, Vector2f mot, int maxtime) {
            this.pos = pos;
            this.mot = mot;
            this.maxTime = maxtime;
            this.time = 0;
        }

        public void update() {
            mot.x /= 1.01F;
            mot.y /= 1.01F;

            pos.x += mot.x;
            pos.y += mot.y;

            time++;
        }
    }

    static ArrayList<Particle> particles = new ArrayList<>();

    public static void render() {
        particles.removeIf(p -> p.time > p.maxTime);

        mc.gameRenderer.setupOverlayRendering(2);

        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 1, 0, 1);

        for (Particle p : particles) {
            float factor = (p.time) / 2F;
            int alpha = (int) (255 / (factor + 1));

            p.update();

            mc.getTextureManager().bindTexture(Particles.ParticleType.BLOOM.texture());
            quadsBeginC(p.pos.x, p.pos.y, 16, 16, 7, new Vector4i(
                    ColorUtil.replAlpha(ColorUtil.fade(), alpha)
            ));
        }

//        RenderSystem.enableLighting();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.popMatrix();

        mc.gameRenderer.setupOverlayRendering();
    }

    public static void quadsBeginC(float x, float y, float width, float height, int glQuads, Vector4i color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(glQuads, POSITION_TEX_COLOR);
        {


            buffer.pos(x, y, 0).tex(0, 0).color(color.get(0)).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).color(color.get(1)).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).color(color.get(2)).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).color(color.get(3)).endVertex();
        }
        tessellator.draw();
    }

    public static void addParticles(Vector2f pos, int amount) {
        for (int i = 0; i < amount; i++) {
            particles.add(new Particle(pos, new Vector2f(Mathf.random(-1, 1), Mathf.random(-1, 1)), 100));
        }
    }

    public static void addParticles(Vector2f pos, Vector2f mot, int amount) {
        for (int i = 0; i < amount; i++) {
            particles.add(new Particle(pos, mot, 100));
        }
    }
}
