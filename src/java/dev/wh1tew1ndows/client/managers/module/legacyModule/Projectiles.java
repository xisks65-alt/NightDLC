package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IRender;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.GLUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Project;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import dev.wh1tew1ndows.client.screen.hud.IRenderer;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Projectiles", category = Category.RENDER, desc = "Отображение снарядов и их траекторий")
public class Projectiles extends Module implements IRender {
    public static Projectiles getInstance() {
        return Instance.get(Projectiles.class);
    }

    private final BooleanSetting renderName = new BooleanSetting(this, "Показывать владельца", false);
    private final MultiBooleanSetting projectiles = new MultiBooleanSetting(this, "Снаряды",
            BooleanSetting.of("Эндер Пёрл", true),
            BooleanSetting.of("Стрела", true),
            BooleanSetting.of("Трезубец", true));

    @EventHandler
    public void onEvent(Render2DEvent event) {
        InterFace interFace = InterFace.getInstance();

        for (Entity entity : mc.world.getAllEntities()) {
            if (validEntity(entity) && noMove(entity)) {
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

                Vector2f position = Project.project2D(lastPosition.x, lastPosition.y, lastPosition.z);
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
        RenderSystem.lineWidth(1.5F);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        BUFFER.begin(1, DefaultVertexFormats.POSITION_COLOR);
        for (Entity entity : mc.world.getAllEntities()) {
            if (validEntity(entity) && noMove(entity))
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

            int color = ColorUtil.fade(i * 5);
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

    boolean validEntity(Entity entity) {
        return (entity instanceof EnderPearlEntity && projectiles.getValue("Эндер Пёрл"))
                || (entity instanceof ArrowEntity && projectiles.getValue("Стрела"))
                || (entity instanceof TridentEntity && projectiles.getValue("Трезубец"));
    }
}
