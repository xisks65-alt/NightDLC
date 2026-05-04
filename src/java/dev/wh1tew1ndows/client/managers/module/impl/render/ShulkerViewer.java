package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.ShulkerPreviewRenderEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.ProjectionUtil;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2d;

import java.util.Collections;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ShulkerViewer", category = Category.RENDER, desc = "Просмотр содержимого шалкер-боксов")
public class ShulkerViewer extends Module {
    public static ShulkerViewer getInstance() {
        return Instance.get(ShulkerViewer.class);
    }

    public SliderSetting size = new SliderSetting(this, "Размер", 0.5f, 0.3f, 0.8f, 0.1f);

    private static final ResourceLocation SHULKER_GUI_TEXTURE = new ResourceLocation("textures/shulker_box_tooltip.png");

    @EventHandler
    public void onRenderTooltip(ShulkerPreviewRenderEvent event) {
        Slot hoveredSlot = event.getHoveredSlot();
        ContainerScreen<?> screen = event.getScreen();
        MatrixStack matrixStack = event.getMatrixStack();
        int x = event.getX();
        int y = event.getY();

        if (hoveredSlot != null && hoveredSlot.getHasStack()) {
            ItemStack itemStack = hoveredSlot.getStack();

            if (Block.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock b) {
                TextureManager textureManager = mc.getTextureManager();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/shulker_box_tooltip.png"));
                int sx = x + 8;
                int sy = y + 8;

                List<ITextComponent> tooltip = itemStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                int tooltipHeight = tooltip.size() > 1 ? tooltip.size() * mc.fontRenderer.FONT_HEIGHT : 0;
                sy += tooltipHeight;

                matrixStack.push();
                matrixStack.translate(0, 0, 500);

                this.setShaderColor(b);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                //screen.blit(matrixStack, sx, sy, 0, 0, screen.getXSize(), 32 * 3);
                RenderUtil.clientStyledRectDark(matrixStack, sx + 6, sy + 6, screen.getXSize() - 10, 64 - 6, 1, 7);
                RenderSystem.disableBlend();
                matrixStack.pop();

                int offX = 8;
                int offY = 8;
                for (ItemStack stack : getItemInShulker(itemStack)) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(0, 0, 555);
                    mc.getItemRenderer().renderItemIntoGUI(stack, sx + offX, sy + offY);
                    mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, sx + offX, sy + offY);
                    RenderSystem.popMatrix();
                    offX += 18;
                    if (offX >= screen.getXSize() - 18) {
                        offX = 8;
                        offY += 18;
                    }
                }
                screen.setBlitOffset(0);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }


    public List<ItemStack> getItemInShulker(ItemStack s) {
        CompoundNBT compoundnbt = s.getChildTag("BlockEntityTag");
        if (compoundnbt != null && compoundnbt.contains("Items", 9)) {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
            return nonnulllist.stream().filter(item -> !item.isEmpty()).toList();
        }
        return Collections.emptyList();
    }

    private void setShaderColor(ShulkerBoxBlock b) {
        float alpha = 1.0f;

        float[] colors;
        if (b.getColor() != null) {
            colors = b.getColor().getColorComponentValues();
        } else {
            colors = new float[]{137f / 255f, 96f / 255f, 137f / 255f, 1f};
        }
        RenderSystem.color4f(colors[0], colors[1], colors[2], alpha);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRender(Render2DEvent e) {
        float partialTicks = mc.getRenderPartialTicks();
        for (Entity entity : mc.world.getAllEntities()) {
            double x = 0, y = 0, z = 0;
            ItemStack stack = null;

            if (entity instanceof PlayerEntity player) {
                if (player.getName().equals(mc.player.getName())) continue;

                stack = player.inventory.getStackInSlot(player.inventory.currentItem);
                x = player.getPosX();
                y = player.getPosY() + player.getHeight() + 1.25f;
                z = player.getPosZ();
            }

            if (entity instanceof ItemEntity itemEntity) {
                ItemStack s = itemEntity.getItem();
                if (Block.getBlockFromItem(s.getItem()) instanceof ShulkerBoxBlock) {
                    stack = s;
                    x = itemEntity.lastTickPosX + (itemEntity.getPosX() - itemEntity.lastTickPosX) * partialTicks;
                    y = itemEntity.lastTickPosY + (itemEntity.getPosY() - itemEntity.lastTickPosY) * partialTicks + 0.5f;
                    z = itemEntity.lastTickPosZ + (itemEntity.getPosZ() - itemEntity.lastTickPosZ) * partialTicks;
                }
            }

            if (stack == null || !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) continue;

            CompoundNBT tag = stack.getTag();
            if (tag == null || !tag.contains("BlockEntityTag", 10)) continue;

            CompoundNBT blocksTag = tag.getCompound("BlockEntityTag");
            if (!blocksTag.contains("Items", 9)) continue;

            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(blocksTag, items);

            if (items.isEmpty()) continue;

            GlStateManager.pushMatrix();
            Vector2f vec = ProjectionUtil.project(x, y, z);

            double startX = vec.x;
            double startY = vec.y;

            GlStateManager.translated(startX, startY, 0);
            GlStateManager.scaled(size.getValue(), size.getValue(), size.getValue());

            mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/shulker_box.png"));

            RenderSystem.enableBlend();
            float[] color = getShulkerColor(stack);
            RenderSystem.color4f(color[0], color[1], color[2], 0.8f);

            MatrixStack matrixStack = new MatrixStack();

            int width = 176;
            int height = 72;
            matrixStack.push();
            AbstractGui.blit(matrixStack, -88, -30, 0, 0, width, 17, 256, 256);
            AbstractGui.blit(matrixStack, -88, -13, 0, 17, width, height - 17, 256, 256);
            AbstractGui.blit(matrixStack, -88, height - 30, 0, 160, width, 7, 256, 256);
            matrixStack.pop();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

            for (int i = 0; i < items.size(); i++) {
                int slotX = -80 + (i % 9) * 18;
                int slotY = -13 + (i / 9) * 18;

                ItemStack item = items.get(i);
                if (!item.isEmpty()) {
                    mc.getItemRenderer().renderItemAndEffectIntoGUI(item, slotX, slotY);
                    mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, item, slotX, slotY, null);
                }
            }

            RenderSystem.disableBlend();
            GlStateManager.popMatrix();
        }
    }


    public static Vector2f project(double x, double y, double z) {
        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - x), (float) (camera_pos.y - y), (float) (camera_pos.z - z));
        result3f.transform(cameraRotation);

        if (mc.gameSettings.viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity playerentity) {
                calculateViewBobbing(playerentity, result3f);
            }
        }

        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return calculateScreenPosition(result3f, fov);
    }

    public static Vector2d project2D(net.minecraft.util.math.vector.Vector3d vec) {
        return project2D(vec.x, vec.y, vec.z);
    }

    public static Vector2d project2D(double x, double y, double z) {
        if (mc.getRenderManager().info == null) return new Vector2d();
        net.minecraft.util.math.vector.Vector3d cameraPosition = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f relativePosition = new Vector3f((float) (cameraPosition.x - x), (float) (cameraPosition.y - y), (float) (cameraPosition.z - z));
        relativePosition.transform(cameraRotation);

        if (mc.gameSettings.viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity playerEntity) {
                float walkedDistance = playerEntity.distanceWalkedModified;

                float deltaDistance = walkedDistance - playerEntity.prevDistanceWalkedModified;
                float interpolatedDistance = -(walkedDistance + deltaDistance * mc.getRenderPartialTicks());
                float cameraYaw = MathHelper.lerp(mc.getRenderPartialTicks(), playerEntity.prevCameraYaw, playerEntity.cameraYaw);

                Quaternion bobQuaternionX = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI - 0.2F) * cameraYaw) * 5.0F, true);
                bobQuaternionX.conjugate();
                relativePosition.transform(bobQuaternionX);

                Quaternion bobQuaternionZ = new Quaternion(Vector3f.ZP, MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 3.0F, true);
                bobQuaternionZ.conjugate();
                relativePosition.transform(bobQuaternionZ);

                Vector3f bobTranslation = new Vector3f((MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 0.5F), (-Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI) * cameraYaw)), 0.0f);
                bobTranslation.setY(-bobTranslation.getY());
                relativePosition.add(bobTranslation);
            }
        }

        double fieldOfView = (float) mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        float halfHeight = (float) mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (relativePosition.getZ() * (float) Math.tan(Math.toRadians(fieldOfView / 2.0F)));

        if (relativePosition.getZ() < 0.0F) {
            return new Vector2d(-relativePosition.getX() * scaleFactor + (float) (mc.getMainWindow().getScaledWidth() / 2), (float) (mc.getMainWindow().getScaledHeight() / 2) - relativePosition.getY() * scaleFactor);
        }
        return null;
    }


    private static void calculateViewBobbing(PlayerEntity playerentity, Vector3f result3f) {
        float walked = playerentity.distanceWalkedModified;
        float f = walked - playerentity.prevDistanceWalkedModified;
        float f1 = -(walked + f * mc.getRenderPartialTicks());
        float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);

        Quaternion quaternion = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
        quaternion.conjugate();
        result3f.transform(quaternion);

        Quaternion quaternion1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Vector3f bobTranslation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
        bobTranslation.setY(-bobTranslation.getY());
        result3f.add(bobTranslation);
    }

    private static Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
        float halfHeight = mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));
        if (result3f.getZ() < 0.0F) {
            return new Vector2f(-result3f.getX() * scaleFactor + mc.getMainWindow().getScaledWidth() / 2.0F, mc.getMainWindow().getScaledHeight() / 2.0F - result3f.getY() * scaleFactor);
        }
        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public float[] getShulkerColor(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block instanceof ShulkerBoxBlock) {
            DyeColor color = ((ShulkerBoxBlock) block).getColor();
            if (color == null) {
                return new float[]{0.537f, 0.341f, 0.898f};
            }
            float[] colorComponents = color.getColorComponentValues();
            return new float[]{colorComponents[0], colorComponents[1], colorComponents[2]};
        }
        return new float[]{0.537f, 0.341f, 0.898f};
    }

}