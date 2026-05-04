package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ColorSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil3D;
import dev.wh1tew1ndows.common.impl.fastrandom.FastRandom;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "WorldParticles", category = Category.RENDER, desc = "Частицы в мире")
public class FireFly extends Module {
    public static FireFly getInstance() {
        return Instance.get(FireFly.class);
    }

    private final SliderSetting count = new SliderSetting(this, "Кол-во", 5, 1, 25, 1);
    private final SliderSetting size = new SliderSetting(this, "Размер", 0.5F, 0.0F, 1F, 0.1F);
    private final SliderSetting range = new SliderSetting(this, "Дистанция", 16, 4, 32, 1);
    private final SliderSetting duration = new SliderSetting(this, "Время жизни", 3500, 500, 5000, 250);
    private final SliderSetting strength = new SliderSetting(this, "Сила движения", 1.0F, 0.1F, 2.0F, 0.1F);
    private final SliderSetting speed = new SliderSetting(this, "Скорость", 1.0F, 0.1F, 3.0F, 0.1F);
    private final SliderSetting opacity = new SliderSetting(this, "Прозрачность", 1.0F, 0.1F, 1.0F, 0.1F);
    private final BooleanSetting glowing = new BooleanSetting(this, "Свечение", true);
    private final BooleanSetting onlyMove = new BooleanSetting(this, "Только в движении", false);
    private final BooleanSetting ground = new BooleanSetting(this, "Спавнить на земле", false);
    private final BooleanSetting opti = new BooleanSetting(this, "Спавнить только если видиш", true);
    private final BooleanSetting physic = new BooleanSetting(this, "Физика", false);
    private final ModeSetting colorMode = new ModeSetting(this, "Режим цвета", "Клиентский", "Свой");
    private final ColorSetting color = new ColorSetting(this, "Цвет", new Color(125, 125, 200).getRGB()).setVisible(() -> colorMode.is("Свой"));
    private final ModeSetting particleMode = new ModeSetting(this, "Тип частиц", "Бубенец", "Звездачка", "Сердечко", "Доллар", "Снежок", "Рандом");

    private final List<Particle> particles = new ArrayList<>();

    private void clear() {
        particles.clear();
    }

    @Override
    public void toggle() {
        super.toggle();
        clear();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        clear();
    }

    @EventHandler
    public void onEvent(MotionEvent event) {
        double distance = Mathf.getRandom(5, 50);
        double yawRad = Math.toRadians(Mathf.getRandom(0, 360));
        double xOffset = -Math.sin(yawRad) * distance;
        double zOffset = Math.cos(yawRad) * distance; // не используется, но оставляю как у тебя

        int range = this.range.getValue().intValue();
        if (onlyMove.getValue() && !hasPlayerMoved()) return;

        for (int i = 0; i < count.getValue().intValue(); i++) {
            Vector3d additional = mc.player.getPositionVec()
                    .add(Mathf.randomValue(-range, range), 0, Mathf.randomValue(-range, range));
            BlockPos pos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(additional));
            spawnParticle(
                    new Vector3d(
                            pos.getX() + Mathf.randomValue(0, 1),
                            ground.getValue()
                                    ? pos.getY()
                                    : mc.player.getPosY() + Mathf.randomValue(mc.player.getHeight(), range),
                            pos.getZ() + Mathf.randomValue(0, 1)
                    ),
                    new Vector3d(
                            0 + Mathf.getRandom(-1, 1),
                            Mathf.randomValue(0.0, strength.getValue()) * (ground.getValue() ? 1 : -1),
                            0 + Mathf.getRandom(-1, 1)
                    )
            );
        }
    }

    private long lastUpdateTime = System.nanoTime();

    @EventHandler
    public void onEvent(Render3DPosedEvent event) {
        MatrixStack matrix = event.getMatrix();

        setupRenderState();
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // секунды
        lastUpdateTime = currentTime;

        renderParticles(matrix, particles, duration.getValue().doubleValue(), duration.min, deltaTime);
        resetRenderState();
    }

    private void renderParticles(MatrixStack matrix, List<Particle> particles, double lifetime, double fadeDuration, double deltaTime) {
        removeExpiredParticles(particles, lifetime + fadeDuration);
        if (particles.isEmpty()) return;

        matrix.push();
        for (Particle particle : particles) {
            particle.update(physic.getValue(), deltaTime);

            Animation animation = particle.animation();
            animation.update();
            float alpha = animation.get();

            if (alpha != opacity.getValue() && !particle.time().finished(fadeDuration)) {
                animation.run(opacity.getValue(), (fadeDuration / 1000), Easings.CUBIC_OUT, true);
            }
            if (alpha != 0.0F && particle.time().finished(lifetime)) {
                animation.run(0.0F, (fadeDuration / 1000), Easings.CUBIC_OUT, true);
            }

            int c = ColorUtil.multAlpha(
                    ColorUtil.replAlpha(particle.color(), alpha),
                    (float) ((Math.sin((System.currentTimeMillis() - particle.spawnTime()) / 200D) + 1F) / 2F)
            );
            Vector3d vec = particle.position();
            renderParticle(matrix, particle, (float) vec.x, (float) vec.y, (float) vec.z, c);
        }
        matrix.pop();
    }

    private void removeExpiredParticles(List<Particle> particles, double lifespan) {
        if (opti.getValue()) {
            particles.removeIf(particle -> !PlayerUtil.isInView(particle.box));
        }
        particles.removeIf(particle -> particle.time().finished(lifespan));
    }

    // ==== РЕНДЕР БЕЗ RectUtil ====

    // ARGB -> [0..1]
    private static float af(int c) {
        return ((c >>> 24) & 0xFF) / 255f;
    }

    private static float rf(int c) {
        return ((c >>> 16) & 0xFF) / 255f;
    }

    private static float gf(int c) {
        return ((c >>> 8) & 0xFF) / 255f;
    }

    private static float bf(int c) {
        return (c & 0xFF) / 255f;
    }

    private static void drawTexturedQuad(MatrixStack stack, float x, float y, float w, float h, int color) {
        Matrix4f m = stack.getLast().getMatrix();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);

        float r = rf(color), g = gf(color), b = bf(color), a = af(color);

        bb.pos(m, x, y, 0).color(r, g, b, a).tex(0f, 0f).endVertex();
        bb.pos(m, x + w, y, 0).color(r, g, b, a).tex(1f, 0f).endVertex();
        bb.pos(m, x + w, y + h, 0).color(r, g, b, a).tex(1f, 1f).endVertex();
        bb.pos(m, x, y + h, 0).color(r, g, b, a).tex(0f, 1f).endVertex();

        Tessellator.getInstance().draw();
    }

    private static void drawCentered(MatrixStack stack, float size, int color) {
        float half = size;
        drawTexturedQuad(stack, -half, -half, half * 2f, half * 2f, color);
    }

    private void renderParticle(MatrixStack matrix, Particle particle, float x, float y, float z, int color) {
        float pos = particle.size;

        matrix.push();
        // позиционирование
        RenderUtil3D.setupOrientationMatrix(matrix, x, y, z);
        // поворот к камере (биллборд)
        matrix.rotate(mc.getRenderManager().getCameraOrientation());

        matrix.push();
        // как в исходнике — разворот по Z на 180
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));
        if (particle.type().rotatable()) {
            matrix.rotate(Vector3f.ZP.rotationDegrees(particle.rotate()));
        }

        matrix.push();

        // свечение
        if (glowing.getValue()) {
            RenderUtil.bindTexture(ParticleType.BLOOM.texture());
            int glowCol = ColorUtil.multAlpha(color, 0.10F);
            drawCentered(matrix, pos * 4f, glowCol);
        }

        // основная текстура частицы
        RenderUtil.bindTexture(particle.type().texture());
        drawCentered(matrix, pos, color);

        // ядро для BLOOM
        if (particle.type() == ParticleType.BLOOM && glowing.getValue()) {
            drawCentered(matrix, pos * 0.5f, color);
        }

        matrix.pop();
        matrix.pop();
        matrix.pop();
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
    }

    private void resetRenderState() {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.clearCurrentColor();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
    }

    private void spawnParticle(Vector3d position, Vector3d velocity) {
        float sz = 0.05F + (this.size.getValue() * 0.2F);
        int col = switch (this.colorMode.getValue()) {
            case "Клиентский" -> ColorUtil.fade(particles.size() * 100);
            case "Свой" -> ColorUtil.fade(
                    10,
                    particles.size() * 100,
                    this.color.getValue(),
                    ColorUtil.multDark(this.color.getValue(), 0.5F)
            );
            default -> -1;
        };

        ParticleType type = switch (this.particleMode.getValue()) {
            case "Доллар" -> ParticleType.DOLLAR;
            case "Сердечко" -> ParticleType.HEART;
            case "Звездачка" -> ParticleType.STAR;
            case "Бубенец" -> ParticleType.BLOOM;
            case "Снежок" -> ParticleType.SNOOW;
            case "Рандом" -> ParticleType.getRandom();
            default -> ParticleType.getRandom();
        };

        particles.add(new Particle(
                type,
                position.add(0, sz, 0),
                velocity,
                particles.size(),
                col,
                sz,
                (int) Mathf.step(Mathf.randomValue(0, 360), 15),
                speed.getValue()
        ));
    }

    private boolean hasPlayerMoved() {
        return mc.player.lastTickPosX != mc.player.getPosX()
                || mc.player.lastTickPosY != mc.player.getPosY()
                || mc.player.lastTickPosZ != mc.player.getPosZ();
    }

    @Getter
    @Accessors(fluent = true)
    public enum ParticleType {
        DOLLAR("dollar", false),
        HEART("heart", false),
        STAR("star", false),
        BLOOM("glow", false),
        SNOOW("snowflake", false);

        private final ResourceLocation texture;
        private final boolean rotatable;

        ParticleType(String name, boolean rotatable) {
            texture = new Namespaced("particle/" + name + ".png");
            this.rotatable = rotatable;
        }

        public static ParticleType getRandom() {
            ParticleType[] values = ParticleType.values();
            return values[new FastRandom().nextInt(values.length)];
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static class Particle {
        private final long spawnTime = System.currentTimeMillis();
        private final ParticleType type;
        private final AxisAlignedBB box;
        private Vector3d position;
        private Vector3d velocity;
        private final int rotate;
        private final int index;
        private final int color;
        private final float size;
        private static final double BASE_VELOCITY = 0.05; // базовая скорость в тиках/с
        private final double speedMultiplier;

        private final StopWatch time = new StopWatch();
        private final Animation animation = new Animation();

        public Particle(ParticleType type, final Vector3d position, final Vector3d velocity,
                        final int index, int color, float size, int rotate, float speedMultiplier) {
            this.type = type;
            this.rotate = rotate;
            this.box = new AxisAlignedBB(position, position).grow(size);
            this.position = position;
            this.velocity = velocity.mul(BASE_VELOCITY);
            this.index = index;
            this.color = color;
            this.size = size;
            this.speedMultiplier = speedMultiplier;
            this.time.reset();
        }

        public void update(boolean physic, double deltaTime) {
            if (physic) {
                if (PlayerUtil.isBlockSolid(this.position.x, this.position.y, this.position.z + this.velocity.z)) {
                    this.velocity = this.velocity.mul(1, 1, -0.8);
                }
                if (PlayerUtil.isBlockSolid(this.position.x, this.position.y + this.velocity.y, this.position.z)) {
                    this.velocity = this.velocity.mul(0.999, -0.6, 0.999);
                }
                if (PlayerUtil.isBlockSolid(this.position.x + this.velocity.x, this.position.y, this.position.z)) {
                    this.velocity = this.velocity.mul(-0.8, 1, 1);
                }
                this.velocity = this.velocity
                        .mul(Math.pow(0.999999, deltaTime * 60))
                        .subtract(new Vector3d(0, 0.00005 * deltaTime * 60, 0));
            }
            this.position = this.position.add(this.velocity.mul(deltaTime * 60 * speedMultiplier));
        }
    }
}
