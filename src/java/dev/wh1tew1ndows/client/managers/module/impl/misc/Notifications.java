package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.api.annotations.Client;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.DelimiterSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.util.Easings;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.other.SoundUtil;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.util.*;
import java.util.stream.Collectors;

@Client
@ModuleInfo(
        name = "Notifications",
        category = Category.MISC,
        desc = "Система уведомлений о событиях в игре"
)
public class Notifications extends Module {
    private final BooleanSetting sound = new BooleanSetting(this, "Звук", true);
    public final ModeSetting soundtype = new ModeSetting(this, "Мод звука",  "Тип 1", "Тип 2");
    private final SliderSetting volume = new SliderSetting(this, "Громкость", 50.0F, 1.0F, 100.0F, 1.0F);
    private final Deque<Toast> queue = new ArrayDeque<>();

    // === Helper настройки ===
    private final DelimiterSetting helper = new DelimiterSetting(this, "Игровые уведомления");

    private final BooleanSetting notifyHubExit = new BooleanSetting(this, "Подтверждение выхода из КТ", true);
    private final BooleanSetting notifyPlayerEffects = new BooleanSetting(this, "Зелья других игроков", false);
    private final BooleanSetting notifyPotionEnd = new BooleanSetting(this, "Заканчивающиеся зелья", true);
    private final BooleanSetting notifySpecRequest = new BooleanSetting(this, "Просьба спека", true);
    private final BooleanSetting notifyLowHealth = new BooleanSetting(this, "Низкое здоровье", true);
    private final BooleanSetting notifyArmorBreak = new BooleanSetting(this, "Поломка брони (30%)", true);

    // === Приватные поля для helper ===
    private final Map<UUID, Set<EffectInstance>> playerEffects = new HashMap<>();
    private boolean confirmedHubExit = false;
    private final Set<String> notifiedEffects = new HashSet<>();
    private boolean lowHealthNotified = false;
    private final Map<EquipmentSlotType, Boolean> armorNotified = new HashMap<>();

    // Constants
    private static final int MAX_VISIBLE = 10;
    private static final float NOTIFICATION_HEIGHT = 16;
    private static final float NOTIFICATION_MARGIN = 2;
    private static final float ICON_SIZE = 23;
    private static final float MIN_WIDTH = 50.0F;
    private static final float MAX_WIDTH_FRACTION = 0.9F;
    private static final long BASE_LIFETIME = 300L;
    private static final long PER_CHAR_LIFETIME = 20;
    private static final float FONT_SIZE = 6;

    public static Notifications getInstance() {
        return Instance.get(Notifications.class);
    }

    public void pushCustom(String title, String message, boolean isSuccess, Category category) {
        int backgroundColor = ColorUtil.replAlpha(InterFace.getInstance().backgroundColorBg(), 0.8F);
        int stripeColor = isSuccess ? ColorUtil.getColor(95, 215, 95) : ColorUtil.getColor(215, 95, 95);
        int titleColor = ColorUtil.multAlpha(ColorUtil.WHITE, 0.95F);
        int themeColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.95F);
        int bodyColor = ColorUtil.multAlpha(ColorUtil.getColor(210, 210, 210), 0.9F);
        long lifetime = BASE_LIFETIME + Math.min(2200L, Math.max(0L, (long) message.length() * PER_CHAR_LIFETIME));

        this.queue.addFirst(new Toast(title, message, lifetime, backgroundColor, stripeColor, titleColor, themeColor, bodyColor, isSuccess, category, null));
    }

    public void pushCustomWithIcon(String title, String message, boolean isSuccess, String icon) {
        int backgroundColor = ColorUtil.replAlpha(InterFace.getInstance().backgroundColorBg(), 0.8F);
        int stripeColor = isSuccess ? ColorUtil.getColor(95, 215, 95) : ColorUtil.getColor(215, 95, 95);
        int titleColor = ColorUtil.multAlpha(ColorUtil.WHITE, 0.95F);
        int themeColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.95F);
        int bodyColor = ColorUtil.multAlpha(ColorUtil.getColor(210, 210, 210), 0.9F);
        long lifetime = BASE_LIFETIME + Math.min(2200L, Math.max(0L, (long) message.length() * PER_CHAR_LIFETIME));

        if (sound.getValue())
            SoundUtil.playSound("nur/low.wav", volume.getValue() / 500);

        this.queue.addFirst(new Toast(title, message, lifetime, backgroundColor, stripeColor, titleColor, themeColor, bodyColor, isSuccess, null, icon));
    }

    public void pushTestNotification() {
        String[] titles = {"Тест", "Проверка", "Демо", "Пример"};
        String[] messages = {"уведомление работает", "система активна", "все функции работают", "тестирование завершено"};
        String[] icons = {"k", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

        String title = titles[(int) (Math.random() * titles.length)];
        String message = messages[(int) (Math.random() * messages.length)];
        String icon = icons[(int) (Math.random() * icons.length)];
        boolean isSuccess = Math.random() > 0.5;

        this.pushCustomWithIcon(title, message, isSuccess, icon);
    }

    public void push(String moduleName, boolean isEnabled, boolean isBeta, Category category) {
        String title = (isBeta ? "(beta) " : "") + "Модуль";
        String message = moduleName + " " + (isEnabled ? "включён" : "выключен");
        int backgroundColor = ColorUtil.replAlpha(InterFace.getInstance().backgroundColorBg(), 0.8F);
        int stripeColor = isEnabled ? ColorUtil.getColor(95, 215, 95) : ColorUtil.getColor(215, 95, 95);
        int titleColor = ColorUtil.multAlpha(ColorUtil.WHITE, 0.95F);
        int themeColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.95F);
        int bodyColor = ColorUtil.multAlpha(ColorUtil.getColor(210, 210, 210), 0.9F);
        long lifetime = BASE_LIFETIME + Math.min(2200L, Math.max(0L, (long) message.length() * PER_CHAR_LIFETIME));

        // Используем иконку модуля вместо кастомной иконки, но передаем состояние для определения цвета
        this.queue.addFirst(new Toast(title, message, lifetime, backgroundColor, stripeColor, titleColor, themeColor, bodyColor, isEnabled, category, null));
    }

    @EventHandler
    public void onRender(Render2DEvent event) {
        if (this.queue.isEmpty()) return;

        MatrixStack matrix = event.getMatrix();
        Minecraft mc = Minecraft.getInstance();
        float screenWidth = (float) mc.getMainWindow().getScaledWidth();
        float screenHeight = (float) mc.getMainWindow().getScaledHeight();

        int visibleCount = 0;
        int stackIndex = 0;
        Iterator<Toast> iterator = this.queue.iterator();

        while (iterator.hasNext()) {
            Toast toast = iterator.next();

            if (visibleCount >= MAX_VISIBLE) {
                toast.appearRequested = true;
                continue;
            }

            if (!toast.active) {
                toast.active = true;
                toast.start();
            }

            float maxWidth = Math.max(MIN_WIDTH, screenWidth * MAX_WIDTH_FRACTION);
            toast.recalcWidth(maxWidth);
            toast.update();

            float stackPosition = (float) stackIndex * 22;
            if (!toast.hasIndex) {
                toast.hasIndex = true;
                toast.stackAnim.set(stackPosition);
            } else if (Math.abs(toast.stackAnim.get() - stackPosition) > 0.5F) {
                // Плавная анимация с задержкой для создания волнового эффекта
                float delay = stackIndex * 0.05F; // Задержка для каждого уведомления
                toast.stackAnim.run(stackPosition, 0.4F + delay, Easings.BACK_OUT, false);
            }

            // Используем анимированную позицию стека для плавного движения
            float animatedStackPosition = toast.stackAnim.get();
            renderToast(matrix, toast, screenWidth, screenHeight, animatedStackPosition);

            if (toast.isFinished()) {
                iterator.remove();
            }

            visibleCount++;
            stackIndex++;
        }
    }

    private void renderToast(MatrixStack matrix, Toast toast, float screenWidth, float screenHeight, float stackPosition) {
        float alpha = toast.alphaAnim.get();
        float yOffset = toast.yAnim.get();
        float x = (screenWidth - toast.width) / 2.0F;
        float baseY = screenHeight - 150.0F - NOTIFICATION_MARGIN + NOTIFICATION_HEIGHT + stackPosition;

        // Плавная анимация появления с подпрыгиванием
        float bounceOffset = (1.0F - yOffset);

        // Если уведомление исчезает, добавляем эффект улетания вверх
        float flyUpOffset = 0.0F;
        if (toast.closing) {
            flyUpOffset = (1.0F - yOffset) * 2.0F; // Улетает вверх
        }


        float finalY = baseY + bounceOffset + flyUpOffset;

        matrix.push();


        float scale = toast.alphaAnim.get() ;
        matrix.translate((x + toast.width / 2F), (finalY + NOTIFICATION_HEIGHT / 2F), 0);
        matrix.scale(scale, scale, 0);
        matrix.translate(-(x + toast.width / 2F), -(finalY + NOTIFICATION_HEIGHT / 2F), 0);

        // Render background

        InterFace.getInstance().drawClientRect(matrix, x, finalY, toast.width, NOTIFICATION_HEIGHT, alpha, 5);


        RenderUtil.Rounded.smooth(matrix, x, finalY, 16, 16, ColorUtil.getColor(20, 0.2F * scale), Round.of(5, 5, 0, 0));


        RectUtil.drawRect(matrix, x + 16.5F, finalY, 0.8F, 16, ColorUtil.getColor(255, 0.02F * scale));


        // Render text с раскраской под тему
        float textX = x + 21.5F;
        float textY = finalY + 7 - 2.9F;

        // Раскрашиваем title под цвет темы, а body обычным цветом
        int titleColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), alpha);
        int bodyColor = ColorUtil.multAlpha(ColorUtil.getColor(180), alpha);

        //RectUtil.drawRect(matrix, x + 18, finalY + 3.15F, 0.5F, 7, ColorUtil.getColor(255, 0.1F * alpha));

        // Рендерим title и body отдельно
        if (!toast.title.isEmpty()) {
            Fonts.SF_BOLD.draw(matrix, toast.title, textX, textY, bodyColor, FONT_SIZE);
            float titleWidth = Fonts.SF_BOLD.getWidth(toast.title, FONT_SIZE);
            Fonts.SF_BOLD.draw(matrix, " " + toast.body, textX  + titleWidth, textY, bodyColor, FONT_SIZE);
        } else {
            Fonts.SF_BOLD.draw(matrix, toast.body, textX , textY, bodyColor, FONT_SIZE);
        }

        // Render separator line
        // Rounded.smooth(matrix, textX - 4.7F, finalY + 2.0F, 1.0F, 10,
        //         ColorUtil.multAlpha(-1, 0.06F * alpha), Round.of(4.5F));

        // Render icon
        if (toast.customIcon != null) {
            int iconColor = ColorUtil.multAlpha(
                    toast.ensp ? ColorUtil.fade() : ColorUtil.getColor(200, 90, 90), alpha);
            Fonts.ICON_V1.draw(matrix, toast.customIcon, x + 5, finalY + 4.7F, iconColor, 6.5F);
        } else if (toast.category != null) {
            if (toast.ensp) {
                Fonts.ICON_DESHUX.draw(matrix, toast.category.getIcon(), x + 5.5F, finalY + 5.2F, ColorUtil.multAlpha(toast.ensp ? ColorUtil.fade() : ColorUtil.getColor(200, 90, 90), alpha), 6.5F);
            } else {
                Fonts.ICON_DESHUX.draw(matrix, toast.category.getIcon(), x + 5.5F, finalY + 5.2F, ColorUtil.multAlpha(toast.ensp ? ColorUtil.fade() : ColorUtil.getColor(200, 90, 90), alpha), 6.5F);
            }
        }

        matrix.pop();
    }

    // Getters for settings
    public BooleanSetting getSound() {
        return this.sound;
    }

    public ModeSetting getSoundType() {
        return this.soundtype;
    }

    public SliderSetting getVolume() {
        return this.volume;
    }

    // ================= HELPER ФУНКЦИОНАЛ =================

    @EventHandler
    private void onUpdate(UpdateEvent e) {
        if (mc.player == null || mc.world == null) return;

        // Проверка эффектов игроков
        if (notifyPlayerEffects.getValue()) {
            checkEffectChanges();
        }

        // Проверка заканчивающихся зелий
        checkPotionDurations();

        // Проверка низкого здоровья
        checkLowHealth();

        // Проверка брони
        checkArmorDurability();
    }

    @EventHandler
    private void onPacket(PacketEvent e) {
        if (mc.player == null) return;

        // Обработка входящих пакетов
        if (e.getPacket() instanceof SChatPacket chatPacket && notifySpecRequest.getValue()) {
            ITextComponent component = chatPacket.getChatComponent();
            checkForSpecRequest(component.getString(), component);
        }

        // Обработка исходящих пакетов
        if (!e.isSend()) return;

        if (e.getPacket() instanceof CChatMessagePacket p) {
            String message = p.getMessage().toLowerCase();

            if (message.equals("/hub") && notifyHubExit.getValue() && PlayerUtil.isPvp()) {
                if (!confirmedHubExit) {
                    ChatUtil.addText("Вы находитесь в КТ, Вы точно хотите выйти? Напишите /hub еще 1 раз");
                    pushCustomWithIcon("", "Подтвердите выход из КТ", true, "j");
                    confirmedHubExit = true;
                    e.cancel();
                } else {
                    confirmedHubExit = false;
                }
            } else if (message.startsWith("/an") && notifyHubExit.getValue() && PlayerUtil.isPvp()) {
                String number = message.substring(3);
                if (number.matches("\\d+")) {
                    if (!confirmedHubExit) {
                        ChatUtil.addText("Вы находитесь в КТ, Вы точно хотите перейти на другую анархию? Напишите " + message + " еще 1 раз");
                        pushCustomWithIcon("", "Подтвердите переход", true, "j");
                        confirmedHubExit = true;
                        e.cancel();
                    } else {
                        confirmedHubExit = false;
                    }
                }
            } else {
                confirmedHubExit = false;
            }
        }
    }

    private void checkEffectChanges() {
        if (mc.world == null || mc.player == null) return;

        PlayerEntity localPlayer = mc.player;

        for (PlayerEntity player : mc.world.getPlayers()) {
            // Пропускаем самого себя
            if (player == localPlayer) continue;

            if (!isPlayerInRadius(localPlayer, player, 100)) {
                continue;
            }

            UUID playerId = player.getUniqueID();
            Collection<EffectInstance> currentEffects = player.getActivePotionEffects();

            Set<String> currentEffectKeys = currentEffects.stream()
                    .map(eff -> eff.getPotion().getName() + ":" + eff.getAmplifier())
                    .collect(Collectors.toSet());

            Set<String> previousEffectKeys = playerEffects.getOrDefault(playerId, Collections.emptySet())
                    .stream()
                    .map(eff -> eff.getPotion().getName() + ":" + eff.getAmplifier())
                    .collect(Collectors.toSet());

            Set<String> newEffectKeys = new HashSet<>(currentEffectKeys);
            newEffectKeys.removeAll(previousEffectKeys);

            Set<String> effectLines = new LinkedHashSet<>();
            for (EffectInstance effect : currentEffects) {
                String key = effect.getPotion().getName() + ":" + effect.getAmplifier();
                if (newEffectKeys.contains(key)) {
                    String localizedName = I18n.format(effect.getPotion().getName());
                    String duration = getPotionDurationString(effect, 1);
                    int level = effect.getAmplifier() + 1;

                    effectLines.add(localizedName + " " + level + " на " + duration);
                }
            }

            if (!effectLines.isEmpty()) {
                String playerName = player.getName().getString();
                for (String line : effectLines) {
                    String message = playerName + " " + line;
                    ChatUtil.addText(message);
                    //pushCustomWithIcon(playerName, line, true, "G");
                }
            }

            playerEffects.put(playerId, new HashSet<>(currentEffects));
        }
    }

    private boolean isPlayerInRadius(PlayerEntity player1, PlayerEntity player2, double radius) {
        return player1.getDistanceSq(player2) <= radius * radius;
    }

    private String getPotionDurationString(EffectInstance effect, int multiplier) {
        if (effect.getIsPotionDurationMax()) return "∞";
        int duration = effect.getDuration() / multiplier;
        int seconds = duration / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes > 0 ? minutes + " мин " + seconds + " сек" : seconds + " сек";
    }

    private void checkPotionDurations() {
        if (!notifyPotionEnd.getValue() || mc.player == null) return;

        for (EffectInstance effect : mc.player.getActivePotionEffects()) {
            String effectKey = effect.getPotion().getName() + ":" + effect.getAmplifier();
            if (effect.getDuration() < 200 && isTrackedPotion(effect) && !notifiedEffects.contains(effectKey)) {
                String potionName = I18n.format(effect.getPotion().getName());
                String message = "Заканчивается " + potionName;
                ChatUtil.addText(message);
                pushCustomWithIcon("", message, false, "j");
                notifiedEffects.add(effectKey);
            }
        }
        notifiedEffects.removeIf(effectKey -> mc.player.getActivePotionEffects().stream().noneMatch(effect ->
                (effect.getPotion().getName() + ":" + effect.getAmplifier()).equals(effectKey)));
    }

    private boolean isTrackedPotion(EffectInstance effect) {
        return effect.getPotion() == Effects.SPEED ||
                effect.getPotion() == Effects.STRENGTH ||
                effect.getPotion() == Effects.FIRE_RESISTANCE;
    }

    private void checkForSpecRequest(String message, ITextComponent component) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("спек") || lowerMessage.contains("spec") || lowerMessage.contains("spek")) {
            String playerName = "";
            if (message.contains("⇨")) {
                int arrowIndex = message.indexOf("⇨");
                int prefixEnd = message.lastIndexOf("]", arrowIndex);
                if (prefixEnd > 0) {
                    playerName = message.substring(prefixEnd + 1, arrowIndex).trim();
                }
            }

            if (playerName.isEmpty()) {
                playerName = "Кто-то";
            }
            playerName = playerName.replaceAll("§[0-9a-fk-or]", "");
            boolean isOwnMessage = false;
            if (mc.getSession() != null) {
                isOwnMessage = playerName.equals(mc.getSession().getUsername());
            }
            if (mc.player != null && !isOwnMessage) {
                isOwnMessage = playerName.equals(mc.player.getName().getString());
            }

            if (!isOwnMessage) {
                message = playerName + " попросил спек";
                ChatUtil.addText(message);
                pushCustomWithIcon("", message, true, "j");
            }
        }
    }

    private void checkLowHealth() {
        if (mc.player == null) return;

        if (notifyLowHealth.getValue()) {
            if (mc.player.getHealth() <= 8.0f) {
                if (!lowHealthNotified) {
                    String message = "Низкий уровень здоровья!";
                    ChatUtil.addText("§c§l" + message);
                    pushCustomWithIcon("", message, false, "j");
                    lowHealthNotified = true;
                }
            } else {
                lowHealthNotified = false;
            }
        }
    }

    private void checkArmorDurability() {
        if (!notifyArmorBreak.getValue() || mc.player == null) return;

        EquipmentSlotType[] armorSlots = {
                EquipmentSlotType.HEAD,
                EquipmentSlotType.CHEST,
                EquipmentSlotType.LEGS,
                EquipmentSlotType.FEET
        };

        String[] armorNames = {"Шлем", "Нагрудник", "Поножи", "Ботинки"};

        for (int i = 0; i < armorSlots.length; i++) {
            EquipmentSlotType slot = armorSlots[i];
            ItemStack armorItem = mc.player.getItemStackFromSlot(slot);

            if (!armorItem.isEmpty() && armorItem.isDamageable()) {
                int maxDurability = armorItem.getMaxDamage();
                int currentDurability = maxDurability - armorItem.getDamage();
                float durabilityPercent = (float) currentDurability / maxDurability;

                if (durabilityPercent <= 0.30f && durabilityPercent > 0) {
                    Boolean wasNotified = armorNotified.get(slot);
                    if (wasNotified == null || !wasNotified) {
                        String message = armorNames[i] + " почти сломан!";
                        ChatUtil.addText("§e" + message);
                        pushCustomWithIcon("", message, false, "j");
                        armorNotified.put(slot, true);
                    }
                } else if (durabilityPercent > 0.30f) {
                    armorNotified.put(slot, false);
                }
            }
        }
    }

    static class Toast {
        final String title;
        final String body;
        final long lifeMs;
        final int bgCol;
        final int stripCol;
        final int titleColL;
        final int titleColR;
        final int bodyCol;
        Category category;
        String customIcon;
        float width;
        boolean active = false;
        boolean appearRequested = false;
        long startTime;
        long endTime;
        boolean closing = false;
        boolean ensp = false;
        final Animation slideAnim = new Animation();
        final Animation alphaAnim = new Animation();
        final Animation yAnim = new Animation();
        final Animation stackAnim = new Animation();
        boolean hasIndex = false;

        Toast(String title, String body, long lifeMs, int bgCol, int stripCol, int titleColL, int titleColR, int bodyCol, boolean ensp, Category category, String customIcon) {
            this.title = title;
            this.body = body;
            this.lifeMs = lifeMs;
            this.bgCol = bgCol;
            this.stripCol = stripCol;
            this.titleColL = titleColL;
            this.titleColR = titleColR;
            this.bodyCol = bodyCol;
            this.ensp = ensp;
            this.category = category;
            this.customIcon = customIcon;
            this.slideAnim.set(0.0F);
            this.alphaAnim.set(0.0F);
            this.yAnim.set(0.0F);
            this.stackAnim.set(0.0F);
            this.width = MIN_WIDTH;
            this.recalcWidth(Float.MAX_VALUE);
        }

        void recalcWidth(float maxWidth) {
            String fullText = this.title + " " + this.body;
            float textWidth = this.fontWidth(fullText, 6);
            float totalWidth = ICON_SIZE + textWidth + 4.5F; // 8.0F is right padding
            this.width = Math.max(MIN_WIDTH, Math.min(totalWidth, maxWidth));
        }

        private float fontWidth(String text, float size) {
            try {
                return Fonts.SF_BOLD.getWidth(text, size);
            } catch (Throwable e) {
                return (float) Minecraft.getInstance().fontRenderer.getStringWidth(text);
            }
        }

        void start() {
            long currentTime = System.currentTimeMillis();
            this.startTime = currentTime;
            this.endTime = currentTime + this.lifeMs;
            this.alphaAnim.run(1.0F, 0.7F, Easings.EXPO_OUT, false);
            this.yAnim.run(1.0F, 0.6F, Easings.EXPO_OUT, false);
        }

        void update() {
            long currentTime = System.currentTimeMillis();
            this.alphaAnim.update();
            this.yAnim.update();
            this.stackAnim.update();

            if (!this.closing && currentTime >= this.endTime) {
                this.closing = true;
                this.alphaAnim.run(0.0F, 0.7F, Easings.EXPO_OUT, false);
                this.yAnim.run(0.0F, 0.6F, Easings.EXPO_OUT, false);
            }
        }

        float lifeProgress() {
            long currentTime = System.currentTimeMillis();
            if (currentTime <= this.startTime) {
                return 1.0F;
            } else if (currentTime >= this.endTime) {
                return 0.0F;
            } else {
                float progress = (float) (this.endTime - currentTime) / (float) this.lifeMs;
                return Math.max(0.0F, Math.min(1.0F, progress));
            }
        }

        boolean isFinished() {
            return this.closing && this.alphaAnim.get() <= 0.02F && this.yAnim.get() <= 0.02F;
        }
    }
}
