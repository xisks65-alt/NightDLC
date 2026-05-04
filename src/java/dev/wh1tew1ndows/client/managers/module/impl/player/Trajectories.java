package dev.wh1tew1ndows.client.managers.module.impl.player;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.other.TpsCalculateComponent;
import dev.wh1tew1ndows.client.managers.events.player.RightEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DLastEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.events.render.RenderPre2DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DelimiterSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.other.Project;
import dev.wh1tew1ndows.client.utils.player.InvUtil;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.GLUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil3D;
import dev.wh1tew1ndows.client.utils.render.font.Font;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dev.wh1tew1ndows.client.api.interfaces.IBuffer.TESSELLATOR;
import static dev.wh1tew1ndows.client.api.interfaces.IRender.BUFFER;
import dev.wh1tew1ndows.client.screen.hud.IRenderer;

@Getter
@Accessors(fluent = true)
@ModuleInfo(name = "Trajectories", desc = "Отрисовывает траекторию полета предметов, таких как жемчуг эндера, стрелы и т.д.", category = Category.PLAYER)
public class Trajectories extends Module {
    private final DelimiterSetting desc1 = new DelimiterSetting(this, "Настройка визуала");
    private final MultiBooleanSetting itemsToPredict = new MultiBooleanSetting(this, "Предметы для предсказания",
            BooleanSetting.of("Жемчуг эндера", true),
            BooleanSetting.of("Стрелы", true),
            BooleanSetting.of("Трезубец", true)
    );

    private final BooleanSetting renderName = new BooleanSetting(this, "Показывать владельца", false);
    private final BooleanSetting fromHand = new BooleanSetting(this, "Предсказание из рук", false);
    private final SliderSetting lineWidth = new SliderSetting(this, "Толщина линии", 2.5F, 1F, 5F, 0.5F);
    private final SliderSetting indicatorSize = new SliderSetting(this, "Размер индикатора", 0.5F, 0.1F, 2F, 0.1F);

    private final DelimiterSetting desc2 = new DelimiterSetting(this, "Настройка помощика");

    private final BooleanSetting autoFire = new BooleanSetting(this, "Авто огонь при наведении", false);
    private final BooleanSetting autoReload = new BooleanSetting(this, "Не отпускать при перезарядке", false);

    private final List<LandingData> landingData = new ArrayList<>();
    private final List<LineData> linesToRender = new ArrayList<>();

    private static final Font FONT = Fonts.SFP_SEMIBOLD;
    private static final float FONT_SIZE = 6.5F;

    private boolean state;


    @Override
    protected void onDisable() {
        state = false;
        super.onDisable();
    }

    @Override
    protected void onEnable() {
        landingData.clear();
        linesToRender.clear();
        super.onEnable();
    }

    @EventHandler
    public void right(RightEvent event) {
        if (autoReload.getValue() && state) event.setCancelled(true);
    }

    @EventHandler
    public void update(UpdateEvent event) {
        ItemStack currentItem = mc.player.getHeldItemMainhand();
        if (currentItem.getItem() instanceof CrossbowItem crossbowItem) {
            if (InvUtil.findArrows() == -1) return;

            boolean isValid = CrossbowItem.isCharged(currentItem);
            if (isValid) {
                if (autoFire.getValue()) {
                    for (LandingData data : landingData) {
                        if (data.hitEntity instanceof LivingEntity) {
                            mc.gameSettings.keyBindUseItem.setPressed(true);
                        }
                    }
                }
                state = false;
                return;
            }

            if (!autoReload.getValue()) return;

            int usedTime = crossbowItem.getUseDuration(currentItem) - mc.player.getItemInUseCount();
            float timeElapsed = CrossbowItem.getCharge(usedTime, currentItem);

            if (timeElapsed >= 1.0F) {
                mc.playerController.onStoppedUsingItem(mc.player);
                state = false;
            }

            if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                state = true;
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }

        } else if (currentItem.getItem() instanceof TridentItem tridentItem) {
            if (mc.gameSettings.keyBindUseItem.isKeyDown() && autoReload.getValue()) {
                state = true;
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }

            int useDuration = currentItem.getItem().getUseDuration(currentItem);
            int usedTime = useDuration - mc.player.getItemInUseCount();
            float timeElapsed = usedTime + TpsCalculateComponent.getInstance().getAdjustTicks();

            if (state && !autoFire.getValue()) {
                if (timeElapsed >= 10) {
                    mc.gameSettings.keyBindUseItem.setPressed(false);
                    state = false;
                    return;
                }
            }

            if (autoFire.getValue()) {
                for (LandingData data : landingData) {
                    if (data.hitEntity instanceof LivingEntity) {

                        if (timeElapsed >= 10) {
                            mc.gameSettings.keyBindUseItem.setPressed(false);
                            state = false;
                            return;
                        }
                    }
                }
            }
        } else {
            state = false;
        }
    }

    @EventHandler
    public void onRender3D(Render3DLastEvent event) {
        if (PlayerUtil.nullCheck()) return;

        MatrixStack matrix = event.getMatrix();
        float partialTicks = event.getPartialTicks();
        Vector3d cameraPos = RenderUtil3D.cameraPos();

        landingData.clear();
        linesToRender.clear();


        // Predict from hand
        if (fromHand.getValue()) {
            ItemStack itemInHand = mc.player.getHeldItemMainhand();
            PredictableItem predictable = PredictableItem.fromItemStack(itemInHand);

            if (isItemEnabled(predictable, itemInHand.getItem())) {
                if (canPredictFromHand(itemInHand)) {
                    Vector3d startPos = getPlayerEye(partialTicks);
                    Vector3d playerYMotion = getPlayerVerticalMotion(partialTicks);

                    if (itemInHand.getItem() == Items.CROSSBOW
                            && EnchantmentHelper.getEnchantmentLevel(Enchantments.MULTISHOT, itemInHand) > 0) {
                        double velocity = 3.15;
                        Vector3d look = mc.player.getLook(partialTicks);
                        Vector3d up = mc.player.getUpVector(partialTicks);

                        Quaternion qLeft = new Quaternion(new Vector3f(up), -10F, true);
                        Quaternion qRight = new Quaternion(new Vector3f(up), +10F, true);

                        Vector3f vCenter = new Vector3f(look);
                        Vector3f vLeft = new Vector3f(look);
                        vLeft.transform(qLeft);
                        Vector3f vRight = new Vector3f(look);
                        vRight.transform(qRight);

                        Vector3d centerMotion = new Vector3d(vCenter.getX(), vCenter.getY(), vCenter.getZ())
                                .normalize().scale(velocity).add(playerYMotion);
                        Vector3d leftMotion = new Vector3d(vLeft.getX(), vLeft.getY(), vLeft.getZ())
                                .normalize().scale(velocity).add(playerYMotion);
                        Vector3d rightMotion = new Vector3d(vRight.getX(), vRight.getY(), vRight.getZ())
                                .normalize().scale(velocity).add(playerYMotion);

                        buildProjectileHelper(mc.player, startPos.x, startPos.y, startPos.z,
                                centerMotion, false, 0, cameraPos);
                        buildProjectileHelper(mc.player, startPos.x, startPos.y, startPos.z,
                                leftMotion, false, 0, cameraPos);
                        buildProjectileHelper(mc.player, startPos.x, startPos.y, startPos.z,
                                rightMotion, false, 0, cameraPos);
                    } else {
                        Vector3d motion = getInitialMotionFromHand(itemInHand, partialTicks);
                        buildProjectileHelper(mc.player, startPos.x, startPos.y, startPos.z,
                                motion, false, 0, cameraPos);
                    }
                }
            }
        }

        RenderUtil3D.setupWorldRenderer();
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (LineData line : linesToRender) {
            drawLine(matrix, line.points, line.finalPos, line.color);
        }

        for (LandingData data : landingData) {
            drawArrivalIndicator(matrix, data);
        }

        matrix.translate(cameraPos.x, cameraPos.y, cameraPos.z);
        RenderUtil3D.cleanupWorldRenderer();
    }

    @EventHandler
    public void onRender2D(RenderPre2DEvent event) {
        MatrixStack matrix = event.getMatrix();

        for (LandingData data : landingData) {
            if (!data.isEntity) continue;

            Vector3d pos = data.landingPos;
            Vector2f screenPos = Vector2f.ZERO;
            float x = (float) (pos.x - mc.getRenderManager().renderPosX());
            float y = (float) (pos.y - mc.getRenderManager().renderPosY());
            float z = (float) (pos.z - mc.getRenderManager().renderPosZ());

            Matrix4f projection = mc.gameRenderer.getProject2DMatrix();
            if (Project.worldToScreen(projection, x, y, z, screenPos)) {
                double seconds = data.ticks * 50 / 1000.0;
                String text = Mathf.limitDecimals(seconds, 1) + "s";
                float w = FONT.getWidth(text, FONT_SIZE) + 8;
                float textX = screenPos.x - w / 2;
                float textY = screenPos.y - (FONT_SIZE + 8) / 2;

                RectUtil.drawRect(matrix, textX, textY + 3, w, FONT_SIZE + 2, ColorUtil.getColor(0, 0, 0, 128));
                FONT.drawCenter(matrix, text, textX + w / 2, textY + 4, ColorUtil.getColor(255, 255, 255, 255), FONT_SIZE);
            }
        }
    }

    private void buildProjectileHelper(Entity shooter, double startX, double startY, double startZ,
                                       Vector3d startMotion, boolean isEntityProjectile, int startTicks,
                                       Vector3d cameraPos) {
        List<Vector3d> points = new ArrayList<>();
        Vector3d pos = new Vector3d(startX, startY, startZ);
        Vector3d motion = startMotion;
        int maxSteps = 200;
        int ticks = startTicks;

        Vector3d finalPos = pos;
        Direction finalFace = null;
        Entity hitEntity = null;
        Vector3d impactNormal = null;
        boolean stopped = false;
        boolean isLivingEntityHit = false;

        for (int i = 0; i < maxSteps; i++) {
            points.add(pos);
            Vector3d prev = pos;
            pos = pos.add(motion);
            ticks++;

            Optional<EntityCollision> entityHit = checkLivingCollision(prev, pos, shooter);
            if (entityHit.isPresent()) {
                finalPos = entityHit.get().hitVec;
                hitEntity = entityHit.get().entity;
                if (hitEntity instanceof LivingEntity) {
                    isLivingEntityHit = true;
                }
                impactNormal = motion.normalize().scale(-1);
                stopped = true;
                break;
            }

            RayTraceContext ctx = new RayTraceContext(
                    prev, pos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, shooter);
            BlockRayTraceResult blockHit = mc.world.rayTraceBlocks(ctx);
            if (blockHit.getType() != RayTraceResult.Type.MISS) {
                finalPos = blockHit.getHitVec();
                finalFace = blockHit.getFace();
                impactNormal = faceToNormal(finalFace);
                stopped = true;
                break;
            }

            if (pos.y < 0) {
                finalPos = pos;
                stopped = true;
                break;
            }

            if (isEntityProjectile) {
                Entity pred = createPredictedEntity(shooter, pos);
                motion = updateMotion(motion, PredictableItem.fromEntity(shooter), pred);
            } else {
                PredictableItem it = PredictableItem.fromItemStack(mc.player.getHeldItemMainhand());
                motion = updateMotion(motion, it, null);
            }
        }

        if (!stopped) {
            finalPos = pos;
        }

        int color = isLivingEntityHit ? ColorUtil.getColor(64, 255, 64, 255) : ColorUtil.fade();
        linesToRender.add(new LineData(points, finalPos, color));
        landingData.add(new LandingData(finalPos, ticks, isEntityProjectile, finalFace, hitEntity, impactNormal));
    }

    private void drawLine(MatrixStack matrix, List<Vector3d> points, Vector3d finalPos, int color) {
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f mat = matrix.getLast().getMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(lineWidth.getValue());

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        int totalPoints = points.size() + 1;
        for (int i = 0; i < points.size(); i++) {
            Vector3d p = points.get(i);
            float x = (float) p.x;
            float y = (float) p.y;
            float z = (float) p.z;
            float alpha = (float) i / totalPoints;
            int gradientColor = (color & 0x00FFFFFF) | ((int) (255 * alpha) << 24);
            buffer.pos(mat, x, y, z).color(gradientColor).endVertex();
        }

        float fx = (float) finalPos.x;
        float fy = (float) finalPos.y;
        float fz = (float) finalPos.z;
        int finalGradientColor = (color & 0x00FFFFFF) | (255 << 24);

        buffer.pos(mat, fx, fy, fz).color(finalGradientColor).endVertex();

        tessellator.draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private void drawArrivalIndicator(MatrixStack matrix, LandingData data) {
        Vector3d pos = data.landingPos;
        float radius = indicatorSize.getValue();
        Vector3d normal = data.impactNormal != null ? data.impactNormal : (data.face != null ? faceToNormal(data.face) : new Vector3d(0, 1, 0));

        float cx = (float) pos.x;
        float cy = (float) pos.y;
        float cz = (float) pos.z;

        Matrix4f mat = matrix.getLast().getMatrix();
        int baseColor = (data.hitEntity instanceof LivingEntity) ? ColorUtil.getColor(64, 255, 64, 160) : ColorUtil.multAlpha(ColorUtil.fade(), 0.6F); //center

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        buffer.pos(mat, cx, cy, cz).color(baseColor).endVertex();

        for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            Vector3d ring = circleVertex(normal, rad, radius);
            float fx = cx + (float) ring.x;
            float fy = cy + (float) ring.y;
            float fz = cz + (float) ring.z;
            buffer.pos(mat, fx, fy, fz).color(ColorUtil.multAlpha(baseColor, 0.15F)).endVertex();
        }
        Tessellator.getInstance().draw();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(2.0f);

        int crossColor = (data.hitEntity instanceof LivingEntity) ? ColorUtil.getColor(64, 255, 64, 200) : ColorUtil.multAlpha(baseColor, 1);
        Vector3d axis1 = anyPerpVector(normal).normalize().scale(0.15);
        Vector3d axis2 = normal.crossProduct(axis1).normalize().scale(0.15);

        Vector3d p1 = new Vector3d(cx, cy, cz).add(axis1);
        Vector3d p2 = new Vector3d(cx, cy, cz).subtract(axis1);
        buffer.pos(mat, (float) p1.x, (float) p1.y, (float) p1.z).color(crossColor).endVertex();
        buffer.pos(mat, (float) p2.x, (float) p2.y, (float) p2.z).color(crossColor).endVertex();

        Vector3d p3 = new Vector3d(cx, cy, cz).add(axis2);
        Vector3d p4 = new Vector3d(cx, cy, cz).subtract(axis2);
        buffer.pos(mat, (float) p3.x, (float) p3.y, (float) p3.z).color(crossColor).endVertex();
        buffer.pos(mat, (float) p4.x, (float) p4.y, (float) p4.z).color(crossColor).endVertex();

        Tessellator.getInstance().draw();

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableAlphaTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest(); // Re-enable depth test
        RenderSystem.disableBlend();
    }

    private boolean shouldPredict(Entity entity) {
        PredictableItem item = PredictableItem.fromEntity(entity);
        if (!isItemEnabled(item, null)) return false;

        if (entity instanceof ArrowEntity arr && arr.isOnGround()) return false;
        if (entity instanceof TridentEntity tri && (tri.isOnGround() || tri.getNoClip())) return false;
        if (entity instanceof ItemEntity itemEnt) {
            return itemEnt.isAlive() && !itemEnt.isOnGround() && !itemEnt.isInWater() && !itemEnt.isInLava();
        }
        return true;
    }


    @EventHandler
    public void onEvent(Render2DEvent event) {
        InterFace interFace = InterFace.getInstance();

        for (Entity entity : mc.world.getAllEntities()) {
            if (shouldPredict(entity) && noMove(entity)) {
                Item item = entity instanceof EnderPearlEntity ? Items.ENDER_PEARL : entity instanceof ArrowEntity ? Items.ARROW : Items.TRIDENT;
                Entity shooter = ((ProjectileEntity) entity).getShooter();
                String shooterName = shooter != null ? shooter.getName().getString() : "Неизвестно";
                Vector3d pearlPosition = entity.getPositionVec();
                Vector3d pearlMotion = entity.getMotion();
                Vector3d lastPosition = new Vector3d(0, 0, 0);

                for (int i = 0; i <= 300; i++) {
                    lastPosition = pearlPosition;
                    pearlPosition = pearlPosition.add(pearlMotion);
                    pearlMotion = updatePearlMotion(entity, pearlMotion, pearlPosition);

                    if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                        break;
                    }
                }

                org.joml.Vector2f position = dev.wh1tew1ndows.client.utils.render.draw.Project.project2D(lastPosition.x, lastPosition.y, lastPosition.z);
                if (position.x == Float.MAX_VALUE && position.y == Float.MAX_VALUE) return;
                float width = renderName.getValue() ? Fonts.SFP_MEDIUM.getWidth(shooterName, IRenderer.fontSize) : -10;
                float x = position.x - width / 2 - 5;
                float y = position.y + 5;
                float stackSize = 8;
                float size = (stackSize / 2F);

                interFace.drawClientRect(event.getMatrix(), x - 5, y - 3, 10, 10, 0.8F, 2);
                if (renderName.getValue()) {
                    interFace.drawClientRect(event.getMatrix(), x + 7, y - 3, 5 + width, 10, 0.8F, 2);
                    Fonts.SFP_MEDIUM.draw(event.getMatrix(), shooterName, x + 10, y - 1.5F, interFace.textColor(), IRenderer.fontSize);
                }

                GLUtil.startScale(x + (stackSize / 2F), y + (stackSize / 2F), 0.5F);
                RenderSystem.translated((x - stackSize - size), (y - stackSize), 0);
                mc.getItemRenderer().renderItemAndEffectIntoGUI(item.getDefaultInstance(), 0, 0);
                RenderSystem.translated(-(x - stackSize - size), -(y - stackSize), 0);
                GLUtil.endScale();
            }
        }
    }

    @EventHandler
    public void onEvent(Render3DPosedEvent event) {
        MatrixStack matrix = event.getMatrix();

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrix.getLast().getMatrix());
        RenderSystem.translated(-mc.getRenderManager().renderPosX(), -mc.getRenderManager().renderPosY(), -mc.getRenderManager().renderPosZ());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(lineWidth.getValue());
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        BUFFER.begin(1, DefaultVertexFormats.POSITION_COLOR);
        for (Entity entity : mc.world.getAllEntities()) {
            if (shouldPredict(entity) && noMove(entity))
                renderLine(entity);
        }
        TESSELLATOR.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.translated(mc.getRenderManager().renderPosX(), mc.getRenderManager().renderPosY(), mc.getRenderManager().renderPosZ());
        RenderSystem.popMatrix();
    }

    private void renderLine(Entity pearl) {
        Vector3d pearlPosition = pearl.getPositionVec().add(0, 0, 0);
        Vector3d pearlMotion = pearl.getMotion();
        Vector3d lastPosition;
        for (int i = 0; i <= 300; i++) {
            lastPosition = pearlPosition;
            pearlPosition = pearlPosition.add(pearlMotion);
            pearlMotion = updatePearlMotion(pearl, pearlMotion, lastPosition);

            if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                break;
            }

            int color = ColorUtil.fade(i * 6);
            BUFFER.pos(lastPosition.x, lastPosition.y, lastPosition.z).color(color).endVertex();
            BUFFER.pos(pearlPosition.x, pearlPosition.y, pearlPosition.z).color(color).endVertex();
        }
    }

    public Vector3d updatePearlMotion(Entity entity, Vector3d originalPearlMotion, Vector3d pearlPosition) {
        Vector3d pearlMotion = originalPearlMotion;

        if ((entity.isInWater() || mc.world.getBlockState(new BlockPos(pearlPosition)).getBlock() == Blocks.WATER) && !(entity instanceof TridentEntity)) {
            float scale = entity instanceof EnderPearlEntity ? 0.8f : 0.6f;
            pearlMotion = pearlMotion.scale(scale);
        } else {
            pearlMotion = pearlMotion.scale(0.99f);
        }

        if (!entity.hasNoGravity())
            pearlMotion.y -= entity instanceof EnderPearlEntity ? 0.03 : 0.05;

        return pearlMotion;
    }

    public boolean shouldEntityHit(Vector3d pearlPosition, Vector3d lastPosition) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                lastPosition,
                pearlPosition,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

        return blockHitResult.getType() == RayTraceResult.Type.BLOCK;
    }

    boolean noMove(Entity entity) {
        return entity.prevPosY != entity.getPosY() || entity.prevPosX != entity.getPosX() || entity.prevPosZ != entity.getPosZ();
    }


    private boolean isItemEnabled(PredictableItem predictable, Item item) {
        if (predictable == null) return false;
        switch (predictable) {
            case ENDER_PEARL:
                return itemsToPredict.getValue("Жемчуг эндера");
            case ARROW:
                return itemsToPredict.getValue("Стрелы");
            case TRIDENT:
                return itemsToPredict.getValue("Трезубец");
            default:
                return false;
        }
    }

    private Vector3d getPlayerEye(float partialTicks) {
        Vector3d eyePos = mc.player.getEyePosition(partialTicks);
        return eyePos.add(mc.player.getLook(partialTicks).scale(0.2));
    }

    private Vector3d getPlayerVerticalMotion(float partialTicks) {
        double playerYMotion = mc.player.getMotion().y * 0.5;
        return new Vector3d(0, playerYMotion, 0);
    }

    private Vector3d getInitialMotionFromHand(ItemStack stack, float partialTicks) {
        Item item = stack.getItem();
        Vector3d playerYMotion = getPlayerVerticalMotion(partialTicks);
        Vector3d dir = angleToVector(mc.player.rotationYaw, mc.player.rotationPitch);

        if (item == Items.BOW) {
            int useDuration = stack.getUseDuration();
            float used = (useDuration - (mc.player.getItemInUseCount() - partialTicks));
            if (used < 0) used = 0;
            float force = Math.min((used / 20.0f) * (used / 20.0f + 2) / 3.0f, 1.0f);
            if (force < 0.05f) force = 0.05f;
            return dir.scale(3.0 * force).add(playerYMotion);
        } else if (item == Items.CROSSBOW) {
            return dir.scale(3.15).add(playerYMotion);
        } else if (item == Items.TRIDENT) {
            float usedTicks = mc.player.getItemInUseCount() - partialTicks;
            if (usedTicks < 0) usedTicks = 0;
            float force = Math.min(usedTicks / 10f, 1.0f);
            if (force < 0.1f) force = 0.1f;
            return dir.scale(2.5 * force).add(playerYMotion);
        } else {
            double velocity = (item == Items.SPLASH_POTION) ? 0.5 : 1.5;
            return dir.scale(velocity).add(playerYMotion);
        }
    }

    private boolean canPredictFromHand(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.BOW || item == Items.TRIDENT) {
            return mc.player.getItemInUseCount() > 0;
        }
        return true;
    }

    private Entity createPredictedEntity(Entity original, Vector3d pos) {
        if (original instanceof EnderPearlEntity) return new EnderPearlEntity(mc.world, pos.x, pos.y, pos.z);
        if (original instanceof ArrowEntity) return new ArrowEntity(mc.world, pos.x, pos.y, pos.z);
        if (original instanceof TridentEntity) return new TridentEntity(mc.world, pos.x, pos.y, pos.z);
        if (original instanceof PotionEntity) return new PotionEntity(mc.world, pos.x, pos.y, pos.z);
        if (original instanceof ItemEntity)
            return new ItemEntity(mc.world, pos.x, pos.y, pos.z, ((ItemEntity) original).getItem());
        return null;
    }

    private Vector3d updateMotion(Vector3d motion, PredictableItem item, Entity pred) {
        if (item == null) return motion;
        double drag = (pred != null && pred.isInWater()) ? item.waterDrag : item.drag;
        return motion.scale(drag).subtract(0, item.gravity, 0);
    }

    private Optional<EntityCollision> checkLivingCollision(Vector3d start, Vector3d end, Entity shooter) {
        double bestDist = Double.MAX_VALUE;
        Entity found = null;
        Vector3d hitVec = null;

        for (Entity e : mc.world.getAllEntities()) {
            if (e != shooter && e.isAlive() && e instanceof LivingEntity) {
                AxisAlignedBB aabb = e.getBoundingBox().grow(0.1);
                Optional<Vector3d> inter = rayTraceAABB(start, end, aabb);
                if (inter.isPresent()) {
                    double dist = start.distanceTo(inter.get());
                    if (dist < bestDist) {
                        bestDist = dist;
                        found = e;
                        hitVec = inter.get();
                    }
                }
            }
        }

        return (found != null && hitVec != null) ? Optional.of(new EntityCollision(found, hitVec)) : Optional.empty();
    }

    private Optional<Vector3d> rayTraceAABB(Vector3d start, Vector3d end, AxisAlignedBB box) {
        Vector3d d = end.subtract(start);
        double[] tMinMax = {0.0, 1.0};

        for (Direction.Axis axis : Direction.Axis.values()) {
            double min = axis.choose(box.minX, box.minY, box.minZ);
            double max = axis.choose(box.maxX, box.maxY, box.maxZ);
            double startCoord = axis.choose(start.x, start.y, start.z);
            double delta = axis.choose(d.x, d.y, d.z);

            if (Math.abs(delta) < 1e-7) {
                if (startCoord < min || startCoord > max) return Optional.empty();
            } else {
                double inv = 1.0 / delta;
                double t1 = (min - startCoord) * inv;
                double t2 = (max - startCoord) * inv;
                double rMin = Math.min(t1, t2);
                double rMax = Math.max(t1, t2);

                if (rMax < tMinMax[0] || rMin > tMinMax[1]) return Optional.empty();
                tMinMax[0] = Math.max(rMin, tMinMax[0]);
                tMinMax[1] = Math.min(rMax, tMinMax[1]);
                if (tMinMax[1] < tMinMax[0]) return Optional.empty();
            }
        }

        double tHit = tMinMax[0];
        return (tHit >= 0 && tHit <= 1) ? Optional.of(start.add(d.scale(tHit))) : Optional.empty();
    }

    private Vector3d angleToVector(float yaw, float pitch) {
        float cosPitch = MathHelper.cos(pitch * ((float) Math.PI / 180F));
        float sinPitch = MathHelper.sin(pitch * ((float) Math.PI / 180F));
        float cosYaw = MathHelper.cos(yaw * ((float) Math.PI / 180F));
        float sinYaw = MathHelper.sin(yaw * ((float) Math.PI / 180F));

        return new Vector3d(-sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch).normalize();
    }

    private Vector3d faceToNormal(Direction face) {
        switch (face) {
            case UP: return new Vector3d(0, 1, 0);
            case DOWN: return new Vector3d(0, -1, 0);
            case NORTH: return new Vector3d(0, 0, -1);
            case SOUTH: return new Vector3d(0, 0, 1);
            case WEST: return new Vector3d(-1, 0, 0);
            case EAST: return new Vector3d(1, 0, 0);
            default: return new Vector3d(0, 1, 0);
        }
    }

    private Vector3d circleVertex(Vector3d normal, double angle, double radius) {
        Vector3d axis1 = anyPerpVector(normal).normalize();
        Vector3d axis2 = normal.crossProduct(axis1).normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return axis1.scale(radius * cos).add(axis2.scale(radius * sin));
    }

    private Vector3d anyPerpVector(Vector3d normal) {
        Vector3d ref = (Math.abs(normal.y) < 0.9) ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);
        return ref.crossProduct(normal);
    }

    @Getter
    public enum PredictableItem {
        ENDER_PEARL(EnderPearlEntity.class, new Item[]{Items.ENDER_PEARL}, 1.5, 0.99, 0.03, 0.8),
        TRIDENT(TridentEntity.class, new Item[]{Items.TRIDENT}, 2.5, 0.99, 0.05, 0.99),
        ARROW(ArrowEntity.class, new Item[]{Items.BOW, Items.CROSSBOW}, 3.0, 0.99, 0.05, 0.6),
        POTION(PotionEntity.class, new Item[]{Items.SPLASH_POTION}, 0.5, 0.99, 0.05, 0.8),
        ITEM(ItemEntity.class, new Item[]{}, 0.3, 0.98, 0.04, 0.99);

        private final Class<? extends Entity> cls;
        private final Item[] items;
        private final double initialSpeed;
        private final double drag;
        private final double gravity;
        private final double waterDrag;

        PredictableItem(Class<? extends Entity> cls, Item[] items, double initialSpeed,
                        double drag, double gravity, double waterDrag) {
            this.cls = cls;
            this.items = items;
            this.initialSpeed = initialSpeed;
            this.drag = drag;
            this.gravity = gravity;
            this.waterDrag = waterDrag;
        }


        public static PredictableItem fromEntity(Entity e) {
            return Arrays.stream(values()).filter(p -> p.cls.isInstance(e)).findFirst().orElse(null);
        }

        public static PredictableItem fromItemStack(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return null;
            return Arrays.stream(values())
                    .filter(p -> Arrays.stream(p.items).anyMatch(it -> stack.getItem() == it))
                    .findFirst().orElse(null);
        }
    }

    private record LandingData(Vector3d landingPos, int ticks, boolean isEntity, Direction face,
                               Entity hitEntity, Vector3d impactNormal) {
    }

    private record EntityCollision(Entity entity, Vector3d hitVec) {
    }

    private record LineData(List<Vector3d> points, Vector3d finalPos, int color) {
    }
}