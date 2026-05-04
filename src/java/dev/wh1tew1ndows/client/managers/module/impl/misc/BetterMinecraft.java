package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.render.ChunkRenderEvent;
import dev.wh1tew1ndows.client.managers.events.render.RenderHotbarEvent;
import dev.wh1tew1ndows.client.managers.events.world.ChunkPositionEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldChangeEvent;
import dev.wh1tew1ndows.client.managers.events.world.WorldLoadEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.render.ChunkAnimator;
import dev.wh1tew1ndows.client.managers.module.settings.impl.*;
import dev.wh1tew1ndows.client.utils.animation.animation.anim2.Animation;
import dev.wh1tew1ndows.client.utils.animation.animation.anim2.Easing;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.common.impl.chunkanimator.AnimationHandler;
import dev.wh1tew1ndows.common.impl.chunkanimator.easing.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import net.optifine.CustomItems;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "BetterMinecraft", category = Category.MISC, desc = "Улучшения интерфейса и качества жизни в игре")
public class BetterMinecraft extends Module {
    public static BetterMinecraft getInstance() {
        return Instance.get(BetterMinecraft.class);
    }


    private final DelimiterSetting utility = new DelimiterSetting(this, "Утилиты");

    public final BooleanSetting keepChat = new BooleanSetting(this, "Сохранять историю чата", true);
    private final BooleanSetting antiSpam = new BooleanSetting(this, "Анти спам в чате", false);

    private final DelimiterSetting visuals = new DelimiterSetting(this, "Визуалы");

    public final BooleanSetting blurInventory = new BooleanSetting(this, "Блюрить Инвентарь", true);

    public final BooleanSetting smoothChat = new BooleanSetting(this, "Плавный чат", true);

    public final BooleanSetting customChat = new BooleanSetting(this, "Кастом чат", false);

    public final BooleanSetting customHotBar = new BooleanSetting(this, "Кастом хот бар", false);



    private final AnimationHandler animationHandler;

    private final DelimiterSetting visuals2 = new DelimiterSetting(this, "Анимации");

    public final BooleanSetting animChun = new BooleanSetting(this,"Анимация загрузки чанков",false);

    private final SliderSetting animationDuration = new SliderSetting(this, "Длительность анимации", 1000, 100, 5000, 100).setVisible(() -> animChun.getValue());
    private final ListSetting<BetterMinecraft.AnimationMode> animationMode = new ListSetting<>(this, "Режим анимации", BetterMinecraft.AnimationMode.values()).set(BetterMinecraft.AnimationMode.BELOW).setVisible(() -> animChun.getValue());
    private final ModeSetting easingMode = new ModeSetting(this, "Режим интерполяции",
            "Linear",
            "Quad",
            "Cubic",
            "Quart",
            "Quint",
            "Expo",
            "Sine",
            "Circ",
            "Back",
            "Bounce",
            "Elastic"
    ).set("Sine").setVisible(() -> animChun.getValue());

    public BetterMinecraft() {
        this.animationHandler = new AnimationHandler();
    }

    @EventHandler
    public void onEvent(ChunkRenderEvent event) {
        animationHandler.preRender(event.getChunkRender());
    }

    @EventHandler
    public void onEvent(ChunkPositionEvent event) {
        animationHandler.setOrigin(event.getChunkRender(), new BlockPos(event.getX(), event.getY(), event.getZ()));
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        animationHandler.clear();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        animationHandler.clear();
    }

    public float getFunctionValue(final float t, @SuppressWarnings("SameParameterValue") final float b, final float c, final float d) {
        return switch (easingMode.getValue()) {
            case "Quad" -> Quad.easeOut(t, b, c, d);
            case "Cubic" -> Cubic.easeOut(t, b, c, d);
            case "Quart" -> Quart.easeOut(t, b, c, d);
            case "Quint" -> Quint.easeOut(t, b, c, d);
            case "Expo" -> Expo.easeOut(t, b, c, d);
            case "Sine" -> Sine.easeOut(t, b, c, d);
            case "Circ" -> Circ.easeOut(t, b, c, d);
            case "Back" -> Back.easeOut(t, b, c, d);
            case "Bounce" -> Bounce.easeOut(t, b, c, d);
            case "Elastic" -> Elastic.easeOut(t, b, c, d);
            default -> Linear.easeOut(t, b, c, d);
        };

    }

    @Getter
    @RequiredArgsConstructor
    public enum AnimationMode {
        BELOW(0),
        ABOVE(1),
        HORIZONT(2),
        PLAYER_DIRECTION(3),
        DIRECTION(4);
        private final int mode;

        @Override
        public String toString() {
            String name = name().toLowerCase();
            String[] words = name.split("_");
            StringBuilder formattedName = new StringBuilder();

            for (String word : words) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
            return formattedName.toString().trim();
        }
    }

    private final Animation scrollAnimation = new Animation(Easing.LINEAR, 40);


    @EventHandler
    public void onEvent(RenderHotbarEvent event) {
        if (mc.player == null) return;
        if (!customHotBar.getValue()) return;
        event.cancel();
        MatrixStack matrixStack = event.getMatrix();
        float partialTicks = event.getPartialTicks();
        PlayerEntity playerentity = mc.ingameGUI.getRenderViewPlayer();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(IngameGui.WIDGETS_TEX_PATH);
        ItemStack itemstack = playerentity.getHeldItemOffhand();
        HandSide handside = playerentity.getPrimaryHand().opposite();
        int i = mc.ingameGUI.scaledWidth / 2;
        int j = mc.ingameGUI.getBlitOffset();

        mc.ingameGUI.setBlitOffset(-90);

        scrollAnimation.run(i - 91 + playerentity.inventory.currentItem * 20);

        RenderUtil.clientStyledRectDark(matrixStack, i - 91, mc.ingameGUI.scaledHeight - 22, 182, 22, 1, 6);
        RenderUtil.clientStyledRect(matrixStack, (float) scrollAnimation.getValue() + 1, mc.ingameGUI.scaledHeight - 21, 21, 20, 1, 4);

        if (!itemstack.isEmpty()) {
            if (handside == HandSide.LEFT) {
                RenderUtil.clientStyledRectDark(matrixStack, i - 91 - 7 - 21.5F, mc.ingameGUI.scaledHeight - 22, 22, 22, 1, 6);
            } else {
                RenderUtil.clientStyledRectDark(matrixStack, i + 91 + 7.5F, mc.ingameGUI.scaledHeight - 22, 22, 22, 1, 6);
            }
        }

        mc.ingameGUI.setBlitOffset(j);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        CustomItems.setRenderOffHand(false);

        for (int i1 = 0; i1 < 9; ++i1) {
            int j1 = i - 90 + i1 * 20 + 2;
            int k1 = mc.ingameGUI.scaledHeight - 16 - 3;
            mc.ingameGUI.renderHotbarItem(j1, k1, partialTicks, playerentity, playerentity.inventory.mainInventory.get(i1));
        }

        if (!itemstack.isEmpty()) {
            CustomItems.setRenderOffHand(true);
            int i2 = mc.ingameGUI.scaledHeight - 16 - 3;

            if (handside == HandSide.LEFT) {
                mc.ingameGUI.renderHotbarItem(i - 91 - 26, i2, partialTicks, playerentity, itemstack);
            } else {
                mc.ingameGUI.renderHotbarItem(i + 91 + 10, i2, partialTicks, playerentity, itemstack);
            }

            CustomItems.setRenderOffHand(false);
        }

        if (mc.gameSettings.attackIndicator == AttackIndicatorStatus.HOTBAR) {
            float f = mc.player.getCooledAttackStrength(0.0F);

            if (f < 1.0F) {
                int j2 = mc.ingameGUI.scaledHeight - 20;
                int k2 = i + 91 + 6;

                if (handside == HandSide.RIGHT) {
                    k2 = i - 91 - 22;
                }

                mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
                int l1 = (int) (f * 19.0F);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.ingameGUI.blit(matrixStack, k2, j2, 0, 94, 18, 18);
                mc.ingameGUI.blit(matrixStack, k2, j2 + 18 - l1, 18, 112 - l1, 18, l1);
            }
        }

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableBlend();
    }
}