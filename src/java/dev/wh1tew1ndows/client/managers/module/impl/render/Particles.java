package dev.wh1tew1ndows.client.managers.module.impl.render;


import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.TickEvent;
import dev.wh1tew1ndows.client.managers.events.player.AttackEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
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
import lombok.experimental.NonFinal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Particles", category = Category.RENDER, desc = "Кастомные частицы")
public class Particles extends Module {
    public static Particles getInstance() {
        return Instance.get(Particles.class);
    }


    private final MultiBooleanSetting events = new MultiBooleanSetting(this, "Спавнить при",
            BooleanSetting.of("Атаке", true),
            BooleanSetting.of("Бросок", true),
            BooleanSetting.of("Тотем", true),
            BooleanSetting.of("Движении", false),
            BooleanSetting.of("Бездействии", true)

    );


    private final ModeSetting visiksmode = new ModeSetting(this, "Тип партиклов", "1", "2", "3");

    private final ModeSetting particleMode = new ModeSetting(this, "Тип частиц", "Бубенец", "Звездачка", "Сердечко", "Доллар", "Снежок", "Звездачка 2", "Рандом");
    private final SliderSetting speed = new SliderSetting(this, "Скорость", 1.5F, 0.1F, 3F, 0.1F);
    private final SliderSetting size = new SliderSetting(this, "Размер", 0.2F, 0.0F, 1F, 0.1F);
    private final SliderSetting attackcos = new SliderSetting(this, "Кол-в атаки", 30, 5, 50, 1).setVisible(() -> events.getValue("Атаке"));
    private final SliderSetting totem = new SliderSetting(this, "Кол-в тотеме", 8, 2, 16, 1).setVisible(() -> events.getValue("Тотем"));
    private final SliderSetting move = new SliderSetting(this, "Кол-в движении", 2, 1, 6, 1).setVisible(() -> events.getValue("Движении"));
    private final SliderSetting brosok = new SliderSetting(this, "Кол-в броске", 6, 1, 16, 1).setVisible(() -> events.getValue("Бросок"));
    private final SliderSetting countAFK = new SliderSetting(this, "Кол-во при бездействии", 5, 1, 25, 1).setVisible(() -> events.getValue("Бездействии"));
    private final SliderSetting range = new SliderSetting(this, "Дистанция при бездействии ", 16, 4, 32, 1).setVisible(() -> events.getValue("Бездействии"));
    private final BooleanSetting rotation = new BooleanSetting(this, "Кручения", false);
    private final BooleanSetting glowEffect = new BooleanSetting(this, "Эффект свечения", true);
    private final BooleanSetting viefForStinka = new BooleanSetting(this, "Видить через стену", false);
    private final BooleanSetting strongYFactor = new BooleanSetting(this, "Сильный фактор по Y", false);

    private final List<Particle> targetParticles = new ArrayList<>();
    private final List<Particle> worldParticles = new ArrayList<>();
    private final List<Particle> flameParticles = new ArrayList<>();

    private void clear() {
        targetParticles.clear();
        worldParticles.clear();
        flameParticles.clear();
    }

    private long lastUpdateTime = System.nanoTime();


    @Override
    public void toggle() {
        super.toggle();
        clear();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        clear();
    }

    private void spawnParticle(List<Particle> particles, Vector3d position, Vector3d velocity) {
        float s = 0.05F + (this.size.getValue() * 0.2F);
        int col = ColorUtil.fade(particles.size() * 100);


        ParticleType type = switch (this.particleMode.getValue()) {
            case "Доллар" -> ParticleType.DOLLAR;
            case "Сердечко" -> ParticleType.HEART;
            case "Звездачка 2" -> ParticleType.STARNEW;
            case "Звездачка" -> ParticleType.STAR;
            case "Бубенец" -> ParticleType.BLOOM;
            case "Снежок" -> ParticleType.SNOOW;
            case "Рандом" -> ParticleType.getRandom();
            default -> ParticleType.getRandom();
        };

        particles.add(new Particle(type,
                position.add(0, s, 0),
                velocity,
                particles.size(),
                col,
                s,
                speed.getValue())
        );
    }

    private void spawnParticleTotem(List<Particle> particles, Vector3d position, Vector3d velocity, int col) {
        float s = 0.05F + (this.size.getValue() * 0.2F);

        ParticleType type = switch (this.particleMode.getValue()) {
            case "Доллар" -> ParticleType.DOLLAR;
            case "Сердечко" -> ParticleType.HEART;
            case "Звездачка" -> ParticleType.STAR;
            case "Звездачка 2" -> ParticleType.STARNEW;
            case "Бубенец" -> ParticleType.BLOOM;
            case "Снежок" -> ParticleType.SNOOW;
            case "Рандом" -> ParticleType.getRandom();
            default -> ParticleType.getRandom();
        };

        particles.add(new Particle(type,
                position.add(0, s, 0),
                velocity,
                particles.size(),
                col,
                s,
                2.0F)
        );
    }


    @EventHandler
    public void onEvent(AttackEvent event) {
        Entity target = event.getTarget();
        float motion = 1.2F;
        if (events.getValue("Атаке")) {
            for (int i = 0; i < attackcos.getValue(); i++) {
                if (getInstance().visiksmode.is("2")) {
                    motion = 0F;
                    spawnParticle(targetParticles,
                            new Vector3d(target.getPosX(),
                                    target.getPosY() + Mathf.randomValue(0, target.getHeight() / 1.1F),
                                    target.getPosZ()),
                            new Vector3d(Mathf.randomValue(-0.6F, 0.6F), Mathf.randomValue(-0.1F, 0.1F),
                                    Mathf.randomValue(-0.6F, 0.6F)));
                } else if (getInstance().visiksmode.is("3")) {
                    motion = 0.8F;
                    spawnParticle(targetParticles,
                            new Vector3d(target.getPosX(),
                                    target.getPosY() + Mathf.randomValue(0, target.getHeight()),
                                    target.getPosZ()),
                            new Vector3d(Mathf.randomValue(-motion, motion), Mathf.randomValue(-1, 0.4F),
                                    Mathf.randomValue(-motion, motion)));
                } else {
                    motion = 1.35F;
                    spawnParticle(targetParticles,
                            new Vector3d(target.getPosX() + Mathf.randomValue(-0.4F, 0.4F),
                                    target.getPosY() + Mathf.randomValue(0, target.getHeight()),
                                    target.getPosZ() + Mathf.randomValue(-0.4F, 0.4F)),
                            new Vector3d(Mathf.randomValue(-motion, motion), Mathf.randomValue(-1.25F, 1.25F),
                                    Mathf.randomValue(-motion, motion)));
                }
            }
        }
    }

    @EventHandler
    public void onEvent(MotionEvent event) {

        if (events.getValue("Бездействии")) {
            int r = range.getValue().intValue();
            for (int i = 0; i < countAFK.getValue().intValue(); i++) {
                Vector3d additional = mc.player.getPositionVec().add(
                        Mathf.randomValue(-r, r), 0, Mathf.randomValue(-r, r)
                );
                BlockPos pos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(additional));

                double x = pos.getX() + Mathf.randomValue(0, 1);
                double z = pos.getZ() + Mathf.randomValue(0, 1);
                double y = mc.player.getPosY() + Mathf.randomValue(mc.player.getHeight(), r);

                Vector3d spawnPos = new Vector3d(x, y, z);

// Проверяем, чтобы блок был воздухом
                while (!mc.world.isAirBlock(new BlockPos(spawnPos)) && spawnPos.y < mc.world.getHeight()) {
                    spawnPos = spawnPos.add(0, 1, 0); // поднимаем вверх, пока не найдём воздух
                }

                if (getInstance().visiksmode.is("2")) {
                    spawnParticle(worldParticles,
                            spawnPos,
                            new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.3, 0.3), Mathf.randomValue(-0.9, 0.2F), mc.player.motion.z + Mathf.randomValue(-0.3, 0.3)).mul(1));
                } else if (getInstance().visiksmode.is("1")) {
                    spawnParticle(worldParticles,
                            spawnPos,
                            new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.5, 0.5),
                                    Mathf.randomValue(-0.06, 0.06),
                                    mc.player.motion.z + Mathf.randomValue(-0.5, 0.5)));
                } else {
                    spawnParticle(worldParticles,
                            spawnPos,
                            new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.5, 0.5),
                                    Mathf.randomValue(-0.1, 0.06),
                                    mc.player.motion.z + Mathf.randomValue(-0.5, 0.5)));
                }
            }
        }
        if (events.getValue("Движении") && hasPlayerMoved()) {
            for (int i = 0; i < move.getValue(); i++) {
                if (getInstance().visiksmode.is("2")) {
                    spawnParticle(flameParticles,
                            new Vector3d(mc.player.getPosX() + Mathf.randomValue(-0.3, 0.3),
                                    mc.player.getPosY() + Mathf.randomValue(0, mc.player.getHeight() / 1.7F),
                                    mc.player.getPosZ() + Mathf.randomValue(-0.3, 0.3)),
                            new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.3, 0.3),
                                    Mathf.randomValue(-0.1, 0.1),
                                    mc.player.motion.z + Mathf.randomValue(-0.3, 0.3)).mul(1));
                } else if (getInstance().visiksmode.is("3")) {
                    spawnParticle(flameParticles,
                            new Vector3d(mc.player.getPosX() + Mathf.randomValue(-0.45, 0.45),
                                    mc.player.getPosY() + Mathf.randomValue(0, mc.player.getHeight() / 3),
                                    mc.player.getPosZ() + Mathf.randomValue(-0.45, 0.45)),
                            new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.3, 0.1),
                                    Mathf.randomValue(-0.3, 0.3),
                                    mc.player.motion.z + Mathf.randomValue(-0.3, 0.3)).mul(1));
                } else {
                    spawnParticle(flameParticles,
                            new Vector3d(mc.player.getPosX() + Mathf.randomValue(-0.45, 0.45),
                                    mc.player.getPosY() + Mathf.randomValue(0, mc.player.getHeight()),
                                    mc.player.getPosZ() + Mathf.randomValue(-0.45, 0.45)),
                            new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.1, 0.1),
                                    Mathf.randomValue(-0.1, 0.1),
                                    mc.player.motion.z + Mathf.randomValue(-0.1, 0.1)).mul(0.2F));
                }
            }
        }
        if (events.getValue("Бросок")) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof net.minecraft.entity.item.EnderPearlEntity ||
                        entity instanceof net.minecraft.entity.projectile.ArrowEntity ||
                        entity instanceof net.minecraft.entity.projectile.TridentEntity) {

                    if (entity instanceof TridentEntity trident) {
                        if (trident.func_234616_v_() != null && trident.dealtDamage) {
                            continue;
                        }
                    }

                    boolean isMoving = entity.prevPosX != entity.getPosX() ||
                            entity.prevPosY != entity.getPosY() ||
                            entity.prevPosZ != entity.getPosZ();
                    if (!isMoving) {
                        continue;
                    }
                    Vector3d pos = entity.getPositionVec();
                    for (int i = 0; i < brosok.getValue(); i++) {
                        if (getInstance().visiksmode.is("2")) {
                            spawnParticle(flameParticles,
                                    new Vector3d(
                                            pos.x + Mathf.randomValue(-0.1, 0.1),
                                            pos.y + Mathf.randomValue(-0.1, 0.1),
                                            pos.z + Mathf.randomValue(-0.1, 0.1)
                                    ),
                                    new Vector3d(
                                            entity.getMotion().x * 0.1 + Mathf.randomValue(-0.4, 0.4),
                                            entity.getMotion().y * 0.1,
                                            entity.getMotion().z * 0.1 + Mathf.randomValue(-0.4, 0.4)
                                    )
                            );
                        } else if (getInstance().visiksmode.is("3")) {
                            spawnParticle(flameParticles,
                                    new Vector3d(
                                            pos.x + Mathf.randomValue(-0.2, 0.2),
                                            pos.y + Mathf.randomValue(-0.2, 0.2),
                                            pos.z + Mathf.randomValue(-0.2, 0.2)
                                    ),
                                    new Vector3d(
                                            Mathf.randomValue(-0.12, 0.12),
                                            Mathf.randomValue(-0.6, 0.06),
                                            Mathf.randomValue(-0.12, 0.12)
                                    )
                            );
                        } else {
                            spawnParticle(flameParticles,
                                    new Vector3d(
                                            pos.x + Mathf.randomValue(-0.5, 0.5),
                                            pos.y + Mathf.randomValue(-0.5, 0.5),
                                            pos.z + Mathf.randomValue(-0.5, 0.5)
                                    ),
                                    new Vector3d(
                                            Mathf.randomValue(-0.06, 0.06),
                                            Mathf.randomValue(-0.06, 0.06),
                                            Mathf.randomValue(-0.06, 0.06)
                                    )
                            );
                        }
                    }
                }
            }
        }

     if(getInstance().visiksmode.is("3")) {
         removeExpiredParticles(targetParticles, 2000);
     } else{
         removeExpiredParticles(targetParticles, 1000);
     }
        removeExpiredParticlesWorld(worldParticles, 2000);
        if (visiksmode.is("2")) {
            removeExpiredParticles(flameParticles, 1200);
        } else {
            removeExpiredParticles(flameParticles, 2000);
        }
    }

    private long totemPartictTime = 0;
    private final long totemSpawnDuration = 2500;
    private boolean spawningTotemParticles = false;
    private Entity currentTotemTarget = null;

    public void spawnTotemParticles(Entity target) {
        if (!events.getValue("Тотем")) return;
        spawningTotemParticles = true;
        totemPartictTime = System.currentTimeMillis();
        currentTotemTarget = target;
    }

    public void updateTotemParticles() {
        if (!spawningTotemParticles || currentTotemTarget == null) return;

        long elapsed = System.currentTimeMillis() - totemPartictTime;
        if (elapsed > totemSpawnDuration) {
            spawningTotemParticles = false;
            currentTotemTarget = null;
            return;
        }

        float motion = 1;
        int[] colors = new int[]{
                ColorUtil.getColor(221, 218, 127),
                ColorUtil.getColor(127, 221, 144)
        };

        for (int i = 0; i < totem.getValue(); i++) {
            int col = colors[Mathf.randomInt(0, colors.length - 1)];
            if (getInstance().visiksmode.is("2")) {
                spawnParticleTotem(flameParticles,
                        new Vector3d(currentTotemTarget.getPosX(),
                                currentTotemTarget.getPosY() + Mathf.random(0, 1.5F),
                                currentTotemTarget.getPosZ()),
                        new Vector3d(Mathf.random(-0.6F, 0.6F), Mathf.random(0.1F, 0.1F), Mathf.random(-0.6F, 0.6F)), col);
            } else if (getInstance().visiksmode.is("3")) {
                spawnParticleTotem(flameParticles,
                        new Vector3d(currentTotemTarget.getPosX() + Mathf.random(-0.1F, -0.1F),
                                currentTotemTarget.getPosY() + Mathf.random(0, 2),
                                currentTotemTarget.getPosZ() + Mathf.random(-0.1F, -0.1F)),
                        new Vector3d(Mathf.random(-0.8F, 0.8F), Mathf.random(-0.7F, 0.7F), Mathf.random(-0.8F, 0.8F)), col);
            } else {
                spawnParticleTotem(flameParticles,
                        new Vector3d(currentTotemTarget.getPosX() + Mathf.random(-0.4F, 0.4F),
                                currentTotemTarget.getPosY() + Mathf.random(0, 2),
                                currentTotemTarget.getPosZ() + Mathf.random(-0.4F, 0.4F)),
                        new Vector3d(Mathf.random(-0.8F, 0.8F), Mathf.random(-0.6F, 0.1F), Mathf.random(-0.8F, 0.8F)), col);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        updateTotemParticles();
    }

    @EventHandler
    public void onEvent(Render3DPosedEvent event) {
        MatrixStack matrix = event.getMatrix();

        long now = System.nanoTime();
        double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = now;

        setupRenderState();
        if (visiksmode.is("2")) {
            renderParticles(matrix, targetParticles, 200, 400, deltaTime);
        } else if(getInstance().visiksmode.is("3")) {
            renderParticles(matrix, targetParticles, 1000, 1500, deltaTime);
        }
            else {
            renderParticles(matrix, targetParticles, 400, 600, deltaTime);
        }
        if (visiksmode.is("2") || getInstance().visiksmode.is("3")) {
            renderParticles(matrix, worldParticles, 500, 800, deltaTime);
        } else {
            renderParticles(matrix, worldParticles, 800, 1500, deltaTime);
        }
        if (visiksmode.is("2") || getInstance().visiksmode.is("3")) {
            renderParticles(matrix, flameParticles, 300, 500, deltaTime);
        } else {
            renderParticles(matrix, flameParticles, 700, 1200, deltaTime);
        }
        resetRenderState();
    }

    private boolean hasPlayerMoved() {
        return mc.player.lastTickPosX != mc.player.getPosX()
                || mc.player.lastTickPosY != mc.player.getPosY()
                || mc.player.lastTickPosZ != mc.player.getPosZ();
    }

    private void removeExpiredParticles(List<Particle> particles, long lifespan) {
        particles.removeIf(particle -> particle.time().finished(lifespan));
    }

    private void removeExpiredParticlesWorld(List<Particle> particles, long lifespan) {
        particles.removeIf(particle -> !PlayerUtil.isInView(particle.box));
        particles.removeIf(particle -> particle.time().finished(lifespan));
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

    // ===================== РЕНДЕР КВАДОВ БЕЗ RectUtil =====================

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

    private static void drawCentered(MatrixStack stack, float halfSize, int color) {
        drawTexturedQuad(stack, -halfSize, -halfSize, halfSize * 2f, halfSize * 2f, color);
    }

    @SuppressWarnings("SameParameterValue")
    private void renderParticles(MatrixStack matrix, List<Particle> particles, long fadeInTime, long fadeOutTime, double deltaTime) {
        if (particles.isEmpty()) return;

        matrix.push();
        for (Particle p : particles) {
            p.update(true, deltaTime);
            p.animation.update();

            if (p.animation().getValue() != 1 && !p.time().finished(fadeInTime)) {
                p.animation().run(1, 0.5, Easings.CUBIC_OUT, true);
            }
            if (p.animation().getValue() != 0 && p.time().finished(fadeOutTime)) {
                p.animation().run(0, 0.5, Easings.CUBIC_OUT, true);
            }

            int col = ColorUtil.replAlpha(p.color(), p.animation.get());
            Vector3d v = p.position();
            renderParticle(matrix, p, (float) v.x, (float) v.y, (float) v.z, p.size, col);

        }
        matrix.pop();
    }

    private void renderParticle(MatrixStack matrix, Particle particle, float x, float y, float z, float pos, int color) {
        matrix.push();
        RenderUtil3D.setupOrientationMatrix(matrix, x, y, z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());


        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));
        // Применяем ротацию через Quaternion только если включена
        if (rotation.getValue()) {
            matrix.rotate(new Quaternion(new Vector3f(0, 0, 1), particle.rotation, false));
        }

        if (viefForStinka.getValue()) GL11.glDisable(GL11.GL_DEPTH_TEST);

        // основная текстура
        if (glowEffect.getValue()) {
            RenderUtil.bindTexture(ParticleType.BLOOM.texture());
            drawCentered(matrix, pos * 2, ColorUtil.multAlpha(color, 0.1F));
        }

        RenderUtil.bindTexture(particle.type().texture());


        drawCentered(matrix, pos, color);
        if (viefForStinka.getValue()) GL11.glEnable(GL11.GL_DEPTH_TEST);

        // ядро для BLOOM


        matrix.pop();
        matrix.pop();
    }

    // ===================== ДАННЫЕ =====================

    @Getter
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum ParticleType {
        DOLLAR("dollar"),
        HEART("heart"),
        STAR("star"),
        BLOOM("glow"),
        SNOOW("snowflake"),
        STARNEW("starnew"),
        ;

        ResourceLocation texture;

        ParticleType(String name) {
            texture = new Namespaced("particle/" + name + ".png");
        }

        public static ParticleType getRandom() {
            ParticleType[] values = ParticleType.values();
            return values[new FastRandom().nextInt(values.length)];
        }
    }

    @Getter
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Particle {
        AxisAlignedBB box;
        ParticleType type;
        @NonFinal
        Vector3d position;
        @NonFinal
        Vector3d velocity;
        int index;
        int color;
        float size;
        private static final double BASE_VELOCITY = 0.05;
        double speedMultiplier;

        StopWatch time = new StopWatch();
        Animation animation = new Animation();

        // Система ротации
        @NonFinal
        private float rotate = 0;
        @NonFinal
        private float rotation = 0;

        public Particle(ParticleType type, final Vector3d position, final Vector3d velocity,
                        final int index, int color, float size, float speedMultiplier) {
            this.box = new AxisAlignedBB(position, position).grow(size);
            this.type = type;
            this.position = position;
            this.velocity = velocity.mul(BASE_VELOCITY);
            this.index = index;
            this.color = color;
            this.size = size;
            this.speedMultiplier = speedMultiplier;
            this.time.reset();
        }

        public void update(boolean physic, double deltaTime) {

            if (Particles.getInstance().rotation.getValue()) {
                this.rotation = (this.rotate % 1000f) / 150f;
                this.rotate += 1;
            }

            if (physic) {
                if (getInstance().visiksmode.is("2")) {
                    // Более мягкая физика для режима 2 - партиклы проходят сквозь блоки с замедлением
                    if (PlayerUtil.isBlockSolid(this.position.x, this.position.y, this.position.z + this.velocity.z)) {
                        this.velocity = this.velocity.mul(0.8F, 0.8F, -0.3F);
                    }
                    if (PlayerUtil.isBlockSolid(this.position.x, this.position.y + this.velocity.y, this.position.z)) {
                        this.velocity = this.velocity.mul(0.8F, -0.4F, 0.8F);
                    }
                    if (PlayerUtil.isBlockSolid(this.position.x + this.velocity.x, this.position.y, this.position.z)) {
                        this.velocity = this.velocity.mul(-0.3F, 0.8F, 0.8F);
                    }
                } else if (getInstance().visiksmode.is("3")) {
                    if (PlayerUtil.isBlockSolid(this.position.x, this.position.y, this.position.z + this.velocity.z)) {
                        this.velocity = this.velocity.mul(0.8F, 0.8F, -0.8F);
                    }
                    if (PlayerUtil.isBlockSolid(this.position.x, this.position.y + this.velocity.y, this.position.z)) {
                        this.velocity = this.velocity.mul(0.8F, -1, 0.8F);
                    }
                    if (PlayerUtil.isBlockSolid(this.position.x + this.velocity.x, this.position.y, this.position.z)) {
                        this.velocity = this.velocity.mul(-0.8F, 0.8F, 0.8F);
                    }
                } else {
                    if (PlayerUtil.isBlockSolid(this.position.x, this.position.y, this.position.z + this.velocity.z)) {
                        this.velocity = this.velocity.mul(1, 1, -1);
                    }
                    if (PlayerUtil.isBlockSolid(this.position.x, this.position.y + this.velocity.y, this.position.z)) {
                        this.velocity = this.velocity.mul(1, -1, 1);
                    }
                    if (PlayerUtil.isBlockSolid(this.position.x + this.velocity.x, this.position.y, this.position.z)) {
                        this.velocity = this.velocity.mul(-1, 1, 1);
                    }
                }
                if (getInstance().visiksmode.is("2")) {
                    this.velocity = this.velocity
                            .mul(Math.pow(0.999, deltaTime * 60))
                            .subtract(new Vector3d(0, (0.000003) * deltaTime * 60, 0));
                } else if (getInstance().visiksmode.is("3")) {
                    this.velocity = this.velocity
                            .mul(Math.pow(0.999, deltaTime * 60))
                            .subtract(new Vector3d(0, (0.0003) * deltaTime * 60, 0));
                } else {
                    this.velocity = this.velocity
                            .mul(Math.pow(0.999, deltaTime * 60))
                            .subtract(new Vector3d(0, 0, 0));
                }
            }
            this.position = this.position.add(this.velocity.mul(deltaTime * 60 * speedMultiplier));
        }
    }
}
