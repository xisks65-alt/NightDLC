package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.managers.component.impl.target.TargetComponent;
import dev.wh1tew1ndows.client.managers.events.render.EventRenderer3D;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AntiBot;
import dev.wh1tew1ndows.client.managers.module.impl.misc.FixHP;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.math.Mathf;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.GLUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Project;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtilOLD;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import dev.wh1tew1ndows.client.utils.render.framebuffer.CustomFramebuffer;
import dev.wh1tew1ndows.client.utils.render.shader.ShaderManager;
import dev.wh1tew1ndows.client.utils.render.shader.impl.entity.EntityShader;
import dev.wh1tew1ndows.client.utils.render.shader.impl.outline.EntityOutlineShader;
import dev.wh1tew1ndows.client.utils.rotation.RayTraceUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.TranslationTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@Accessors(fluent = true)
@ModuleInfo(name = "Esp", category = Category.RENDER, desc = "Подсветка игроков и мобов через стены")
public class Esp extends Module {

    private final ModeSetting mode = new ModeSetting(this, "Type ESP", "3D-Box", "2D-Box", "Shader");


    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Эффекты зелий", true),
            BooleanSetting.of("Зачарования", false),
            BooleanSetting.of("Здоровье", true)
    ).setVisible(() -> mode.is("2D-Box"));

    private final ModeSetting modes = new ModeSetting(this, "Режим", "Внешний", "Внутренний", "Внешний и внутренний").setVisible(() -> mode.is("Shader"));
    private final SliderSetting iterations = new SliderSetting(this, "Сила", 3, 1, 5, 1).setVisible(() -> mode.is("Shader"));
    private final SliderSetting divider = new SliderSetting(this, "Сила размытия", 8, 1, 8, 0.1F).setVisible(() -> mode.is("Shader"));
    private final BooleanSetting layers = new BooleanSetting(this, "Слои", true).setVisible(() -> mode.is("Shader"));
    private final BooleanSetting chams = new BooleanSetting(this, "Чамсы", false).setVisible(() -> mode.is("Shader"));
    private final BooleanSetting outline = new BooleanSetting(this, "Обводка", true).setVisible(() -> !chams.getValue() && mode.is("Shader"));
    private final BooleanSetting visible = new BooleanSetting(this, "Только стены", false).setVisible(() -> mode.is("Shader"));

    public ModeSetting boxMode = new ModeSetting(this, "Box Mode", "All", "Outline", "Fill").setVisible(() -> mode.is("3D-Box"));
    public SliderSetting boxOutlineSize = new SliderSetting(this, "Box Outline Size", 3, 1, 5, 1).setVisible(() -> mode.is("3D-Box"));
    public SliderSetting boxAlphaOutline = new SliderSetting(this, "Box Alpha Outline", 255, 80, 255, 1).setVisible(() -> mode.is("3D-Box"));
    public SliderSetting boxAlphaFill = new SliderSetting(this, "Box Alpha Fill", 50, 10, 100, 1).setVisible(() -> mode.is("3D-Box"));
    public BooleanSetting boxBloom = new BooleanSetting(this, "Box Bloom", true).setVisible(() -> mode.is("3D-Box"));
    Tessellator tessellator = Tessellator.getInstance();

    //box esp
    @EventHandler
    public void onRender(EventRenderer3D e) {
        if (!mode.is("3D-Box")) return;
        GlStateManager.pushMatrix();
        RenderUtil.setup3dForBlockPos(this::render, boxBloom.getValue());
        GlStateManager.popMatrix();
    }

    public void render() {
        for (PlayerEntity player : Minecraft.getInstance().world.getPlayers()) {
            if (player == Minecraft.getInstance().player) continue;
            boolean isFriend = Zetrix.inst().friendManager().isFriend(player.getGameProfile().getName());
            int color1 = isFriend ? RenderUtil.Colors.swapAlpha(new Color(0, 255, 0).darker().getRGB(), boxAlphaOutline.getValue().intValue()) :
                    RenderUtil.Colors.swapAlpha(new Color(ColorUtil.fade(0)).darker().getRGB(), boxAlphaOutline.getValue().intValue());
            int color2 = isFriend ? RenderUtil.Colors.swapAlpha(new Color(0, 255, 0).getRGB(), (int) (boxAlphaOutline.getValue().floatValue())) :
                    RenderUtil.Colors.swapAlpha(new Color(ColorUtil.fade(0)).darker().getRGB(), boxAlphaOutline.getValue().intValue());
            int color3 = isFriend ? RenderUtil.Colors.swapAlpha(new Color(0, 255, 0).darker().getRGB(), (int) (boxAlphaFill.getValue().floatValue())) :
                    RenderUtil.Colors.swapAlpha(new Color(ColorUtil.fade(180)).darker().getRGB(), (int) (boxAlphaFill.getValue().floatValue()));
            int color4 = isFriend ? RenderUtil.Colors.swapAlpha(new Color(0, 255, 0).getRGB(), (int) (boxAlphaFill.getValue().floatValue())) :
                    RenderUtil.Colors.swapAlpha(new Color(ColorUtil.fade(270)).getRGB(), (int) (boxAlphaFill.getValue().floatValue()));


            if (boxMode.is("All")) {
                drawBoxFullGradient(getEntityBox(player), color1, color1, color1, color1, color2, color2, color2, color2, true, boxOutlineSize.getValue());
                drawBoxFullGradient(getEntityBox(player), color3, color3, color3, color3, color4, color4, color4, color4, false, boxOutlineSize.getValue());
            } else if (boxMode.is("Outline")) {
                drawBoxFullGradient(getEntityBox(player), color1, color1, color1, color1, color2, color2, color2, color2, true, boxOutlineSize.getValue());
            } else if (boxMode.is("Fill")) {
                drawBoxFullGradient(getEntityBox(player), color3, color3, color3, color3, color4, color4, color4, color4, false, boxOutlineSize.getValue());
            }

        }
    }

    private AxisAlignedBB getEntityBox(Entity entity) {
        AxisAlignedBB aabb;
        Vector3d pos = getEntityVec3dPosition(entity);

        double width = entity.getWidth();
        double height = entity.getHeight();

        if (entity instanceof PlayerEntity player) {
            width = 0.6;
            height = player.isSneaking() ? 1.65 : 1.8;
        } else if ((aabb = entity.getBoundingBox()) != null) {
            width = aabb.maxX - aabb.minX;
            height = aabb.maxY - aabb.minY;
        }

        Vector3d first = new Vector3d(pos.x - width / 2.0, pos.y, pos.z - width / 2.0);
        Vector3d second = new Vector3d(pos.x + width / 2.0, pos.y + height, pos.z + width / 2.0);
        return new AxisAlignedBB(first, second);
    }

    private Vector3d getEntityVec3dPosition(Entity entity) {
        float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
        double x = MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getPosX());
        double y = MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getPosY());
        double z = MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getPosZ());
        return new Vector3d(x, y, z);
    }


    private void drawBoxFullGradient(AxisAlignedBB bb, int color1, int color2, int color3, int color4, int color5, int color6, int color7, int color8, boolean linesMode, float lineWidth) {
        double x = bb.minX;
        double y = bb.minY;
        double z = bb.minZ;
        double x2 = bb.maxX;
        double y2 = bb.maxY;
        double z2 = bb.maxZ;
        GlStateManager.shadeModel(7425);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        if (linesMode) {
            GlStateManager.glLineWidth(lineWidth);
            GL11.glLineStipple(3, Short.reverseBytes((short) -24769));
            GL11.glEnable(2852);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
        }
        BufferBuilder buffer = tessellator.getBuffer();

        if (linesMode) {
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y, z).color(color1).endVertex();
            buffer.pos(x, y, z2).color(color3).endVertex();
            buffer.pos(x2, y, z2).color(color5).endVertex();
            buffer.pos(x2, y, z).color(color7).endVertex();
            this.tessellator.draw();
        } else {
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y, z).color(color1).endVertex();
            buffer.pos(x, y, z2).color(color3).endVertex();
            buffer.pos(x2, y, z2).color(color5).endVertex();
            buffer.pos(x2, y, z).color(color7).endVertex();
            this.tessellator.draw();
        }
        if (linesMode) {
            buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z).color(color2).endVertex();
            buffer.pos(x, y2, z2).color(color4).endVertex();
            buffer.pos(x2, y2, z2).color(color6).endVertex();
            buffer.pos(x2, y2, z).color(color8).endVertex();
            this.tessellator.draw();
        } else {
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z).color(color2).endVertex();
            buffer.pos(x, y2, z2).color(color4).endVertex();
            buffer.pos(x2, y2, z2).color(color6).endVertex();
            buffer.pos(x2, y2, z).color(color8).endVertex();
            this.tessellator.draw();
        }
        if (linesMode) {
            buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z).color(color2).endVertex();
            buffer.pos(x, y, z).color(color1).endVertex();
            this.tessellator.draw();
        } else {
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z).color(color2).endVertex();
            buffer.pos(x, y2, z2).color(color4).endVertex();
            buffer.pos(x, y, z2).color(color3).endVertex();
            buffer.pos(x, y, z).color(color1).endVertex();
            this.tessellator.draw();
        }
        if (linesMode) {
            buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x2, y2, z2).color(color6).endVertex();
            buffer.pos(x2, y, z2).color(color5).endVertex();
            this.tessellator.draw();
        } else {
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x2, y2, z).color(color8).endVertex();
            buffer.pos(x2, y2, z2).color(color6).endVertex();
            buffer.pos(x2, y, z2).color(color5).endVertex();
            buffer.pos(x2, y, z).color(color7).endVertex();
            this.tessellator.draw();
        }
        if (linesMode) {
            buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z2).color(color4).endVertex();
            buffer.pos(x, y, z2).color(color3).endVertex();
            this.tessellator.draw();
        } else {
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z2).color(color4).endVertex();
            buffer.pos(x2, y2, z2).color(color6).endVertex();
            buffer.pos(x2, y, z2).color(color5).endVertex();
            buffer.pos(x, y, z2).color(color3).endVertex();
            this.tessellator.draw();
        }
        if (linesMode) {
            buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x2, y2, z).color(color8).endVertex();
            buffer.pos(x2, y, z).color(color7).endVertex();
            this.tessellator.draw();
        } else {
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x, y2, z).color(color2).endVertex();
            buffer.pos(x2, y2, z).color(color8).endVertex();
            buffer.pos(x2, y, z).color(color7).endVertex();
            buffer.pos(x, y, z).color(color1).endVertex();
            this.tessellator.draw();
        }
        if (linesMode) {
            GlStateManager.glLineWidth(1.0f);
            GL11.glDisable(2852);
            GL11.glDisable(2848);
            GL11.glHint(3154, 4352);
        }
        GlStateManager.enableCull();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
    }


    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    private final dev.wh1tew1ndows.client.utils.render.font.Font font = Fonts.SFP_SEMIBOLD;

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtil.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }

    public final Set<Entity> collectedEntities = new HashSet<>();

    @EventHandler
    public void onRender(Render2DEvent event) {
        if (!mode.is("2D-Box")) return;
        if (mc.world == null) {
            return;
        }


        this.collectEntities();

        MatrixStack matrix = event.getMatrix();

        for (Entity entity : collectedEntities) {
            org.joml.Vector3d iVec = Mathf.interpolate(entity, mc.getRenderPartialTicks());
            double posX = iVec.x;
            double posY = iVec.y;
            double posZ = iVec.z;

            double nametagWidth = entity.getWidth() / 1.5, nametagHeight = entity.getHeight() + 0.1f - (entity.isSneaking() ? 0.2f : 0.0f);

            AxisAlignedBB aabb = new AxisAlignedBB(posX - nametagWidth, posY, posZ - nametagWidth, posX + nametagWidth, posY + nametagHeight, posZ + nametagWidth);

            Vector2d min = null;
            Vector2d max = null;

            for (org.joml.Vector3d vector : getVectors(aabb)) {
                Vector2d vec = RenderUtil.project2D(vector.x, vector.y, vector.z);

                if (vec != null) {
                    if (min == null) {
                        min = new Vector2d(vec.x, vec.y);
                        max = new Vector2d(vec.x, vec.y);
                    } else {
                        min.x = Math.min(min.x, vec.x);
                        min.y = Math.min(min.y, vec.y);
                        max.x = Math.max(max.x, vec.x);
                        max.y = Math.max(max.y, vec.y);
                    }
                }
            }

            //noinspection ConstantValue
            if (max != null && min != null) {
                float minX = (float) min.x;
                float minY = (float) min.y;
                float maxX = (float) max.x;
                float maxY = (float) max.y;

                int color1 = Zetrix.inst().friendManager().isFriend(entity.getName().toString()) ? ColorUtil.getColor(90, 190, 90) : InterFace.getInstance().themeColor(0);
                int color2 = Zetrix.inst().friendManager().isFriend(entity.getName().toString()) ? ColorUtil.getColor(40, 120, 40) : InterFace.getInstance().themeColor(90);
                int color3 = Zetrix.inst().friendManager().isFriend(entity.getName().toString()) ? ColorUtil.getColor(90, 190, 90) : InterFace.getInstance().themeColor(180);
                int color4 = Zetrix.inst().friendManager().isFriend(entity.getName().toString()) ? ColorUtil.getColor(40, 120, 40) : InterFace.getInstance().themeColor(270);
                int black = ColorUtil.getColor(0, 0, 0, 128);

                if (entity instanceof PlayerEntity player) {
                    {

                        drawBox(maxX, minX, maxY, minY, matrix, color1, color2, color3, color4);
                    }


                    if (checks.getValue("Здоровье")) {
                        float healthWidth = 0.5F;
                        int green = ColorUtil.getColor(0, 255, 0);
                        int red = ColorUtil.getColor(255, 0, 0);

                        float x = maxX + 3F + healthWidth;

                        float health = FixHP.getInstance().isEnabled() ? PlayerUtil.getHealthFromScoreboard(player)[0] : player.getHealth();
                        float healthHeight = health / Math.max(health, FixHP.getInstance().isEnabled() ? PlayerUtil.getHealthFromScoreboard(player)[1] : (player.getMaxHealth())) * (maxY - minY);

                        float offset = 0.5F;

                        RectUtilOLD.drawGradientV(matrix, x - healthWidth - offset, minY - offset, x + healthWidth + offset, maxY + offset, black, black, false);
                        RectUtilOLD.drawGradientV(matrix, x - healthWidth, minY + ((maxY - minY) - healthHeight), x + healthWidth, maxY, green, red, false);
                    }
                    if (checks.getValue("Эффекты зелий")) {
                        renderEffects(matrix, player, maxX - ((maxX - minX) / 2F), maxY + 5);
                    }


                    dev.wh1tew1ndows.client.utils.render.font.Font font = Fonts.SFP_SEMIBOLD;

                    List<ItemStack> items = new ArrayList<>();

                    ItemStack mainStack = player.getHeldItemMainhand();

                    if (!mainStack.isEmpty()) {
                        items.add(mainStack);
                    }

                    for (ItemStack itemStack : entity.getArmorInventoryList()) {
                        if (itemStack.isEmpty()) continue;
                        items.add(itemStack);
                    }

                    ItemStack offStack = player.getHeldItemOffhand();

                    if (!offStack.isEmpty()) {
                        items.add(offStack);
                    }

                    float x = minX + ((maxX - minX) / 2F) + (-items.size() * 8);

                    float nameTagY = minY - this.font.getHeight(7) * 2;
                    nametagHeight = this.font.getHeight(7) - 0.5F;

                    float y = (float) (nameTagY - (nametagHeight + 5));

                    float stackSize = 16;
                    for (ItemStack item : items) {
                        if (item.isEmpty()) continue;
                        GLUtil.scaleStart(x + (stackSize / 2F), y + (stackSize / 2F), 0.75F);
                        //drawItemStack(item, x, y, true, item.getCount() != 1 ? String.valueOf(item.getCount()) : "");
                        GLUtil.scaleEnd();
                        float enchWidth = (float) EnchantmentHelper.getEnchantments(item).entrySet().stream()
                                .mapToDouble(enchant -> font.getWidth(getShortEnchantment(enchant), 7))
                                .max()
                                .orElse(0);
                        if (checks.getValue("Зачарования")) {
                            float yOffset = 0;
                            for (Map.Entry<Enchantment, Integer> enchant : EnchantmentHelper.getEnchantments(item).entrySet()) {
                                if (!getShortEnchantment(enchant).isEmpty()) {
                                    font.drawCenter(event.getMatrix(), getShortEnchantment(enchant), x + (stackSize / 2F), y + 6 - (stackSize / 2F) + yOffset, -1, 6);
                                    yOffset -= font.getHeight(7);
                                }
                            }
                        }
                        x += Math.max(enchWidth, stackSize);
                    }
                }
            }

        }


    }

    private void drawBox(float maxX, float minX, float maxY, float minY, MatrixStack matrix, int color1, int color2, int color3, int color4) {
        float boxWidth = (maxX - minX);
        float boxHeight = (maxY - minY);
        float lineWidth = 1;

        float sect = Math.min(boxWidth / 4, boxHeight / 4);

        // top left
        RectUtilOLD.drawRect(matrix, minX, minY, minX + sect, minY + lineWidth, color1, color1, color1, color1, false, false);
        RectUtilOLD.drawRect(matrix, minX, minY, minX + lineWidth, minY + sect, color1, color1, color1, color1, false, false);

        // top right
        RectUtilOLD.drawRect(matrix, maxX, minY, maxX - sect, minY + lineWidth, color2, color2, color2, color2, false, false);
        RectUtilOLD.drawRect(matrix, maxX, minY, maxX + lineWidth, minY + sect, color2, color2, color2, color2, false, false);

        // bottom right
        RectUtilOLD.drawRect(matrix, maxX, minY + boxHeight - lineWidth, maxX - sect, minY + boxHeight, color3, color3, color3, color3, false, false);
        RectUtilOLD.drawRect(matrix, maxX, maxY, maxX + lineWidth, maxY - sect, color3, color3, color3, color3, false, false);

        // bottom left
        RectUtilOLD.drawRect(matrix, minX, minY + boxHeight - lineWidth, minX + sect, minY + boxHeight, color4, color4, color4, color4, false, false);
        RectUtilOLD.drawRect(matrix, minX, maxY, minX + lineWidth, maxY - sect, color4, color4, color4, color4, false, false);
    }

    private void renderEffects(MatrixStack matrix, PlayerEntity player, float x, float y) {
        EffectInstance[] effects = player.getActivePotionEffects().toArray(new EffectInstance[0]);
        for (int index = 0; index < effects.length; index++) {
            EffectInstance effect = effects[index];
            if (effect == null) continue;
            String name = I18n.format(effect.getEffectName());
            String amplifier = I18n.format("enchantment.level." + (effect.getAmplifier() + 1)).replaceAll("enchantment.level.0", "");
            String duration = EffectUtils.getPotionDurationString(effect, 1);
            if (effect.getIsPotionDurationMax()) {
                amplifier = "**:**";
            } else {
                amplifier = "";
            }
            String effectText = (name + " " + amplifier + ColorFormatting.getColor(ColorUtil.getColor(200, 80, 80)) + " (" + duration + ")" + ColorFormatting.reset()).toLowerCase().replace("**:**", "беск");

            font.drawCenterShadow(matrix, effectText, x, y + (index * font.getHeight(7)), -1, 7);
        }
    }

    public static void drawItemStack(ItemStack stack, double x, double y, boolean withOverlay, String text) {
        RenderSystem.translated(x, y, 0);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (withOverlay) mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, stack, 0, 0, text);
        RenderSystem.translated(-x, -y, 0);
    }

    private String getShortEnchantment(Map.Entry<Enchantment, Integer> nbt) {
        if (nbt.getValue() < 0) return "";
        String output = nbt.getKey().getDisplayName(0).getString().substring(0, 2);

        output += " ";

        if (nbt.getValue() != 1 || nbt.getKey().getMaxLevel() != 1) {
            if (nbt.getValue() == Short.MAX_VALUE) {
                output += "∞";
            } else if (nbt.getValue() > 10) {
                output += nbt.getValue().toString();
            } else {
                output += new TranslationTextComponent("enchantment.level." + nbt.getValue()).getString();
            }
        }

        return output.replaceAll("enchantment.level.", "");
    }

    private org.joml.Vector3d[] getVectors(AxisAlignedBB boundingBox) {
        return new org.joml.Vector3d[]{new org.joml.Vector3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new org.joml.Vector3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new org.joml.Vector3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new org.joml.Vector3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new org.joml.Vector3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new org.joml.Vector3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new org.joml.Vector3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new org.joml.Vector3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)};
    }

    private void collectEntities() {
        this.collectedEntities.clear();
        final Iterable<Entity> entities = mc.world.getAllEntities();
        for (final Entity entity : entities) {
            if (!this.isValid(entity)) {
                continue;
            }
            this.collectedEntities.add(entity);

        }
    }

    private boolean isValid(final Entity entity) {
        if (entity instanceof ItemEntity) return true;

        if (entity == mc.player && mc.gameSettings.getPointOfView().equals(PointOfView.FIRST_PERSON)) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof PlayerEntity wrapper) {
            if (AntiBot.getInstance().isBot(wrapper)) return false;
        }
        return entity instanceof PlayerEntity;
    }

    private final CustomFramebuffer buffer = new CustomFramebuffer(true);
    private final EntityShader bloom = new EntityShader();


    public void patch(PlayerEntity entity, Runnable runnable) {
        Vector3d interpolated = entity.getPositionVec().subtract(entity.getPositionVec(mc.getRenderPartialTicks()));

        AxisAlignedBB aabb = entity.getBoundingBox().offset(interpolated.inverse().add(interpolated.scale(mc.getRenderPartialTicks())));
        org.joml.Vector2f center = Project.project2D(aabb.getCenter());

        if (center.x == Float.MAX_VALUE && center.y == Float.MAX_VALUE) {
            return;
        }

        float minX = center.x, minY = center.y, maxX = center.x, maxY = center.y;

        for (Vector3d corner : aabb.getCorners()) {
            org.joml.Vector2f vec = Project.project2D(corner);

            if (vec.x == Float.MAX_VALUE && vec.y == Float.MAX_VALUE) {
                continue;
            }

            minX = Math.min(minX, vec.x);
            minY = Math.min(minY, vec.y);
            maxX = Math.max(maxX, vec.x);
            maxY = Math.max(maxY, vec.y);
        }

        float posX = minX, posY = minY, width = maxX - minX, height = maxY - minY;

        float hurtPC = (float) Math.sin(entity.hurtTime * (18F * Math.PI / 180F));

        boolean isFriend = Zetrix.inst().friendManager().isFriend(entity.getGameProfile().getName());

        int color = isFriend ? ColorUtil.GREEN : InterFace.getInstance().clientColor();

        ShaderManager gradient = ShaderManager.entityChamsShader;
        gradient.load();
        gradient.setUniformi("tex", 0);
        gradient.setUniformf("location", posX, posY);
        gradient.setUniformf("rectSize", width, height);
        gradient.setUniformf("color", ColorUtil.getRGBAf(color));

        runnable.run();

        gradient.unload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(Render3DPosedEvent event) {
        if (!mode.is("Shader")) return;
        MatrixStack stack = event.getMatrix();
        buffer.setup();

        TargetComponent.getTargets(128, this::isValid2, false)
                .forEach(entity -> {
                    if (entity instanceof PlayerEntity player) {

                        if (!isPlayerBehindBlocks(player, event.getPartialTicks()) && visible.getValue()) return;

                        patch(player, () -> {
                            EntityRendererManager rendererManager = mc.getRenderManager();
                            stack.push();
                            stack.translate(-rendererManager.renderPosX(), -rendererManager.renderPosY(), -rendererManager.renderPosZ());
                            RenderSystem.depthMask(true);
                            rendererManager.setRenderShadow(false);
                            rendererManager.setRenderName(false);
                            IRenderTypeBuffer.Impl irendertypebuffer$impl = mc.getRenderTypeBuffers().getBufferSource();
                            Vector3d pos = entity.getPositionVec(event.getPartialTicks());
                            EntityRenderer<?> renderer = rendererManager.getRenderer(entity);
                            boolean nameVisible = renderer.isRenderName();

                            if (nameVisible) renderer.setRenderName(false);
                            if (!layers.getValue()) renderer.setRenderLayers(false);
                            rendererManager.renderClearEntityStatic(entity, pos.getX(), pos.getY(), pos.getZ(), entity.rotationYaw, event.getPartialTicks(), stack, irendertypebuffer$impl, rendererManager.getPackedLight(entity, event.getPartialTicks()));
                            if (!layers.getValue()) renderer.setRenderLayers(true);
                            if (nameVisible) renderer.setRenderName(true);

                            irendertypebuffer$impl.finish();

                            rendererManager.setRenderName(true);
                            rendererManager.setRenderShadow(true);
                            RenderSystem.depthMask(false);
                            RenderSystem.enableDepthTest();
                            stack.pop();
                        });
                    }
                });

        buffer.stop();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(Render2DEvent event) {
        if (!mode.is("Shader")) return;
        if (outline.getValue()) {
            EntityOutlineShader.draw(1, buffer.framebufferTexture);
        }
        bloom.render(event.getMatrix(), buffer.framebufferTexture, iterations.getValue().intValue(), 1.5F, 4 + divider.getValue());
        if (chams.getValue()) buffer.draw();
        buffer.framebufferClear();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    private boolean isValid2(final Entity entity) {
        if (!entity.isAlive() || entity.isGlowing()) {
            return false;
        }
        if (mc.renderViewEntity != null && entity == mc.renderViewEntity && mc.gameSettings.getPointOfView().firstPerson()) {
            return false;
        }
        return isInView2(entity) && entity instanceof PlayerEntity;
    }

    public boolean isInView2(Entity entity) {
        if (mc.getRenderViewEntity() == null || mc.worldRenderer.getClippinghelper() == null) {
            return false;
        }
        return mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(entity.getBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public boolean isPlayerBehindBlocks(PlayerEntity player, float partialTicks) {
        if (mc.getRenderViewEntity() == null || mc.world == null) return true;

        Vector3d cameraPos = mc.getRenderViewEntity().getEyePosition(partialTicks);
        Vector3d playerPos = player.getPositionVec(partialTicks).add(0, player.getEyeHeight(), 0);
        
        RayTraceResult result = mc.world.rayTraceBlocks(
            new RayTraceContext(
                cameraPos,
                playerPos,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.getRenderViewEntity()
            )
        );
        
        return result.getType() != RayTraceResult.Type.MISS;
    }

}
