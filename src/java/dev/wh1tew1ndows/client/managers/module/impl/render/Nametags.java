package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.managers.component.impl.target.TargetComponent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.RenderNameEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.misc.FixHP;
import dev.wh1tew1ndows.client.managers.module.impl.misc.NameProtect;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.MultiBooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorFormatting;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.GLUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Project;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Nametags", category = Category.RENDER, desc = "Отображение имени предметов и энтити через стены")
public class Nametags extends Module {

    public static Nametags getInstance() {
        return Instance.get(Nametags.class);
    }

    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Предметы", true),
            BooleanSetting.of("Мобы", false),
            BooleanSetting.of("Монстры", false)
    );

    private final SliderSetting fontSize    = new SliderSetting(this, "Размер шрифта", 8F, 6F, 12F, 0.1F);
    private final BooleanSetting optimized  = new BooleanSetting(this, "Оптимизировать", true);
    private final BooleanSetting renderDonat= new BooleanSetting(this, "Отображать донат", true);
    private final BooleanSetting showArmor  = new BooleanSetting(this, "Отображать броню", true);
    private final BooleanSetting showMainHand = new BooleanSetting(this, "Правая рука", true);
    private final BooleanSetting showOffHand  = new BooleanSetting(this, "Левая рука", true);
    private final BooleanSetting showEffects  = new BooleanSetting(this, "Отображать эффекты", true);

    private final int black = ColorUtil.getColor(0, 128);

    // ── Кэш строк цветов — вычисляем один раз ────────────────────────────
    private final String C_RED   = ColorFormatting.getColor(TextFormatting.RED.getColor());
    private final String C_GREEN = ColorFormatting.getColor(TextFormatting.GREEN.getColor());
    private final String C_WHITE = ColorFormatting.getColor(TextFormatting.WHITE.getColor());
    private final String C_GRAY  = ColorFormatting.getColor(TextFormatting.GRAY.getColor());

    // ── Кэш данных игроков — обновляется раз в тик ───────────────────────
    private static class PlayerCache {
        String  tagText  = "";
        float   tagWidth = 0;
        boolean isFriend = false;
        final List<ItemStack>    armorItems = new ArrayList<>(6);
        final List<String>       effects    = new ArrayList<>(4);
        final List<ITextComponent> handItems= new ArrayList<>(2);
        int lastUpdateTick = -1;
    }

    private final Map<Integer, PlayerCache> playerCacheMap = new HashMap<>(32);
    private int currentTick = 0;

    @EventHandler
    public void onTick(UpdateEvent e) {
        if (mc.player == null) return;
        currentTick++;
        // Чистим кэш мёртвых игроков раз в 20 тиков
        if (currentTick % 20 == 0) {
            playerCacheMap.entrySet().removeIf(entry ->
                mc.world == null || mc.world.getEntityByID(entry.getKey()) == null
            );
        }
    }

    @EventHandler
    public void onRenderName(RenderNameEvent event) {
        if (event.getEntity() instanceof AbstractClientPlayerEntity && checks.getValue("Игроки")) event.cancel();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null) return;
        float fontHeight = fontSize.getValue();
        MatrixStack matrix = event.getMatrix();

        TargetComponent.getTargets(128, this::isValid, false)
                .forEach(entity -> renderNametag(entity, matrix, fontHeight));

        if (checks.getValue("Предметы")) {
            mc.world.loadedItemEntityList().forEach(e -> renderNametag(e, matrix, fontHeight));
        }
        if (checks.getValue("Мобы")) {
            for (Entity e : mc.world.loadedEntityList()) {
                if (e instanceof AnimalEntity) renderNametag(e, matrix, fontHeight);
            }
        }
        if (checks.getValue("Монстры")) {
            for (Entity e : mc.world.loadedEntityList()) {
                if (e instanceof MonsterEntity) renderNametag(e, matrix, fontHeight);
            }
        }
    }

    private void renderNametag(Entity entity, MatrixStack matrix, float fontHeight) {
        float pt = mc.getRenderPartialTicks();
        double ix = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * pt;
        double iy = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * pt;
        double iz = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * pt;

        double eH = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
        double eW = entity.getBoundingBox().maxX - entity.getBoundingBox().minX;

        // Только 2 проекции вместо 4
        Vector2f top = Project.project2D(ix, iy + eH + 0.15, iz);
        if (top.x == Float.MAX_VALUE) return;
        Vector2f bot = Project.project2D(ix, iy, iz);
        if (bot.x == Float.MAX_VALUE) return;

        // Ширину считаем через угловые точки только если нужна броня
        float centerX = top.x; // центр X совпадает для top/bot
        float minY = top.y;
        float maxY = bot.y;
        float y = minY - fontHeight - 5;

        if (entity instanceof PlayerEntity player) {
            if (!checks.getValue("Игроки")) return;

            // Получаем/обновляем кэш игрока
            PlayerCache cache = getOrUpdateCache(player, fontHeight);

            // Рисуем тег
            drawTag(matrix, cache.tagText, cache.tagWidth, centerX, y, fontHeight, cache.isFriend);

            // Броня — только если включена
            if (showArmor().getValue() && !cache.armorItems.isEmpty()) {
                float armorWidth = cache.armorItems.size() * 10f - 2f;
                float posX = centerX - armorWidth / 2f;
                float posY = y - 12;
                float stackSize = 8;
                for (ItemStack item : cache.armorItems) {
                    if (item.isEmpty()) continue;
                    GLUtil.startScale(posX + stackSize / 2f, posY + stackSize / 2f, 0.5F);
                    drawItemStack(matrix, item, posX, posY);
                    GLUtil.endScale();
                    if (item.isDamageable() && item.isDamaged()) {
                        float damage = (float) item.getDamage();
                        float maxDamage = (float) item.getMaxDamage();
                        float durability = Math.max(0.0F, (maxDamage - damage) / maxDamage);
                        float bgWidth = stackSize - 2;
                        int barWidth = Math.max(1, Math.round(bgWidth * durability));
                        int color = MathHelper.hsvToRGB(durability / 3.0F, 1.0F, 1.0F) | 0xFF000000;
                        float barX = posX + 2;
                        float barY = posY + stackSize + 2;
                        RectUtil.drawRect(matrix, barX, barY, bgWidth, 1, ColorUtil.getColor(0, 255));
                        RectUtil.drawRect(matrix, barX, barY, barWidth, 1, color);
                    }
                    posX += stackSize + 2;
                }
            }

            // Предметы в руках
            if (!cache.handItems.isEmpty()) {
                float curY = maxY + 2;
                for (ITextComponent item : cache.handItems) {
                    float tw = Fonts.SFP_SEMIBOLD.getWidth(item.getString(), fontHeight);
                    float rx = centerX - (tw + 4) / 2f;
                    RenderUtil.Rounded.smooth(matrix, rx, curY, tw + 4, fontHeight + 2, black, Round.of(3));
                    Fonts.SFP_SEMIBOLD.drawTextComponent(matrix, new StringTextComponent("").append(item),
                            centerX - tw / 2f, curY + 1, -1, false, fontHeight);
                    curY += fontHeight + 3;
                }
            }

            // Эффекты
            if (!cache.effects.isEmpty()) {
                float curY = maxY + 2 + (cache.handItems.size() * (fontHeight + 3)) + 2;
                for (String eff : cache.effects) {
                    float tw = Fonts.SFP_SEMIBOLD.getWidth(eff, 6.5F);
                    Fonts.SFP_SEMIBOLD.draw(matrix, eff, centerX - tw / 2f, curY, -1, 6.5F);
                    curY += 8.5F;
                }
            }

        } else if (entity instanceof ItemEntity item) {
            if (!checks.getValue("Предметы")) return;
            ITextComponent name = item.getItem().getDisplayName();
            String text = name.getString();
            if (item.getItem().getCount() > 1) text += C_GRAY + " " + item.getItem().getCount() + "x";
            float tw = Fonts.SFP_SEMIBOLD.getWidth(text, fontHeight);
            drawTag(matrix, text, tw, centerX, y, fontHeight, false);

        } else if (entity instanceof AnimalEntity animal) {
            if (!checks.getValue("Мобы")) return;
            String text = animal.getDisplayName().getString() + " " + C_RED +
                    (int) animal.getHealth() + C_WHITE + " HP";
            float tw = Fonts.SFP_SEMIBOLD.getWidth(text, fontHeight);
            drawTag(matrix, text, tw, centerX, y, fontHeight, false);

        } else if (entity instanceof MonsterEntity monster) {
            if (!checks.getValue("Монстры")) return;
            String text = monster.getDisplayName().getString() + " " + C_RED +
                    (int) monster.getHealth() + C_WHITE + " HP";
            float tw = Fonts.SFP_SEMIBOLD.getWidth(text, fontHeight);
            drawTag(matrix, text, tw, centerX, y, fontHeight, false);
        }
    }

    // ── Кэш игрока — обновляется раз в тик ───────────────────────────────
    private PlayerCache getOrUpdateCache(PlayerEntity player, float fontHeight) {
        int id = player.getEntityId();
        PlayerCache cache = playerCacheMap.computeIfAbsent(id, k -> new PlayerCache());

        if (cache.lastUpdateTick == currentTick) return cache; // уже обновлён в этом тике
        cache.lastUpdateTick = currentTick;

        // ── Тег (ник + хп) ──
        boolean isFriend = Zetrix.inst().friendManager().isFriend(player.getScoreboardName());
        cache.isFriend = isFriend;

        String playerName;
        if (isFriend && NameProtect.getInstance().isEnabled()) {
            playerName = "Protected";
        } else {
            playerName = (renderDonat.getValue() ? player.getDisplayName() : player.getName()).getString();
        }

        float hp = player.getHealth();
        if (FixHP.getInstance().isEnabled()) {
            Score score = mc.world.getScoreboard().getOrCreateScore(
                    player.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
            if (score.getScorePoints() != 0) hp = score.getScorePoints();
        }

        float finalHp = hp;
        finalHp = Math.round(finalHp * 2f) / 2f;

        String KT = "";
        for (EffectInstance potion : player.getActivePotionEffects()) {
            if (potion.getDuration() != 0 && potion.getPotion() == Effects.UNLUCK && potion.getAmplifier() == -1) {
                int dur = potion.getDuration();
                KT = " [" + C_RED + StringUtils.ticksToElapsedTime(dur)
                        .replace(dur >= 200 ? "0:" : "0:0", "") + C_WHITE + "]";
                break;
            }
        }

        String friendTag = isFriend ? C_GREEN + " [F]" + C_WHITE : "";
        cache.tagText = playerName + "  " + C_WHITE + "[" + C_RED + (int) finalHp + C_WHITE + "]" + friendTag + KT;
        cache.tagWidth = Fonts.SFP_SEMIBOLD.getWidth(cache.tagText, fontHeight);

        // ── Броня — не копируем ItemStack, просто ссылки ──
        cache.armorItems.clear();
        if (showArmor.getValue()) {
            // Броня идёт в обратном порядке (boots=0 → helmet=3)
            List<ItemStack> armorList = new ArrayList<>(4);
            for (ItemStack s : player.getArmorInventoryList()) armorList.add(s);
            for (int i = armorList.size() - 1; i >= 0; i--) {
                if (!armorList.get(i).isEmpty()) cache.armorItems.add(armorList.get(i));
            }
            if (!player.getHeldItemMainhand().isEmpty()) cache.armorItems.add(player.getHeldItemMainhand());
            if (!player.getHeldItemOffhand().isEmpty()) cache.armorItems.add(player.getHeldItemOffhand());
        }

        // ── Предметы в руках ──
        cache.handItems.clear();
        if (showOffHand.getValue() && !player.getHeldItemOffhand().isEmpty())
            cache.handItems.add(player.getHeldItemOffhand().getDisplayName());
        if (showMainHand.getValue() && !player.getHeldItem(Hand.MAIN_HAND).isEmpty())
            cache.handItems.add(player.getHeldItem(Hand.MAIN_HAND).getDisplayName());

        // ── Эффекты — строки, обновляем раз в тик ──
        cache.effects.clear();
        if (showEffects.getValue()) {
            for (EffectInstance eff : player.getActivePotionEffects()) {
                if (eff.getDuration() <= 0) continue;
                int sec = eff.getDuration() / 20;
                String time = sec >= 60
                        ? (sec / 60) + ":" + String.format("%02d", sec % 60)
                        : sec + "s";
                String amp = eff.getAmplifier() > 0 ? " " + (eff.getAmplifier() + 1) : "";
                cache.effects.add(eff.getPotion().getDisplayName().getString() + amp + " " + C_WHITE + time);
            }
        }

        return cache;
    }

    private void drawTag(MatrixStack matrix, String text, float tw, float cx, float y, float fh, boolean isFriend) {
        int bg = isFriend ? ColorUtil.getColor(0, 255, 0, 60) : black;
        RenderUtil.Rounded.smooth(matrix, cx - tw / 2f - 2, y, tw + 4, fh + 2, bg, Round.of(3));
        Fonts.SFP_SEMIBOLD.draw(matrix, text, cx - tw / 2f, y + 1, -1, fh);
    }

    public void drawItemStack(MatrixStack matrix, ItemStack stack, double x, double y) {
        matrix.push();
        RenderSystem.translated(x, y, 0);
        RectUtil.drawRect(matrix, 0, 0, 16, 16, black);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        RenderSystem.translated(-x, -y, 0);
        matrix.pop();
    }

    private boolean isValid(final Entity entity) {
        if (!entity.isAlive()) return false;
        if (mc.renderViewEntity != null && entity == mc.renderViewEntity
                && mc.gameSettings.getPointOfView().firstPerson()) return false;
        if (!isInView(entity)) return false;
        return entity instanceof PlayerEntity || entity instanceof ItemEntity ||
                (checks.getValue("Мобы") && entity instanceof AnimalEntity) ||
                (checks.getValue("Монстры") && entity instanceof MonsterEntity);
    }

    public boolean isInView(Entity entity) {
        if (mc.getRenderViewEntity() == null || mc.worldRenderer.getClippinghelper() == null) return false;
        return mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(entity.getBoundingBox());
    }

    @Override
    public void onDisable() {
        playerCacheMap.clear();
        super.onDisable();
    }
}
