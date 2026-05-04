package dev.wh1tew1ndows.client.managers.module.impl.misc;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.annotations.Funtime;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.MoveInputEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.events.render.Render3DPosedEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;
import dev.wh1tew1ndows.client.managers.module.settings.impl.*;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import dev.wh1tew1ndows.client.utils.other.SoundUtil;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.client.utils.render.color.ColorUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RectUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil;
import dev.wh1tew1ndows.client.utils.render.draw.RenderUtil3D;
import dev.wh1tew1ndows.client.utils.render.draw.Round;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Funtime
@ModuleInfo(name = "FunTimeHelper", category = Category.MISC, desc = "Помощник для сервера FunTime/SpookyTime")
public class FunTimeHelper extends Module {
    private final DragSetting drag = new DragSetting(this, "FTHelper");
    private String lastEventKey = "";
    private static final Pattern EVENT_COORD_PATTERN_BRACKETS = Pattern.compile("\\[(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)]");
    private static final Pattern EVENT_COORD_PATTERN_NO_BRACKETS = Pattern.compile("(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§.");

    // === FPS-independent сглаживание ===
    private static final float SIZE_SMOOTH_SPEED = 12f;
    private static final float ITEM_SPACING = 2; // расстояние между элементами
    private static final float TEXT_SIZE = 7; // размер текста

    private long lastTimeNs = 0L;

    // Анимации для каждого предмета (по имени)
    private final Map<String, Float> itemAnimations = new HashMap<>();

    public final BooleanSetting antiDAYNANKA = new BooleanSetting(this, "Не заходить на уебанские анки без спец придметов", true);
    // === Event ===
    private final BooleanSetting autogps = new BooleanSetting(this, "Auto GPS Event", true);
    private final DelimiterSetting eventGroup = new DelimiterSetting(this, "Event")
            .setVisible(() -> autogps.getValue());
    private final ModeSetting eventMarkerMode = new ModeSetting(this, "Тип метки ивента", "Стрелочка", "Точка в мире")
            .setVisible(() -> autogps.getValue());
    private final BooleanSetting ignoreBeacon = new BooleanSetting(this, "Игнорировать Загадочный Маяк", true)
            .setVisible(() -> autogps.getValue());
    private final BooleanSetting autogpsNotify = new BooleanSetting(this, "Оповещения ивентов", true)
            .setVisible(() -> autogps.getValue());

    // === Бинды ===
    private final DelimiterSetting bindsGroup = new DelimiterSetting(this, "Бинды");
    public final BooleanSetting renderHud = new BooleanSetting(this, "Рендер HUD", true);
    private final BooleanSetting notif = new BooleanSetting(this, "Оповещения", true);
    public BindSetting dezor = new BindSetting(this, "Дезориентация", -1);
    private final BooleanSetting dezorPreview = new BooleanSetting(this, "Проекция дезориентации", true);
    public BindSetting yavka = new BindSetting(this, "Явная пыль", -1);
    private final BooleanSetting yavkaPreview = new BooleanSetting(this, "Проекция явной пыли", true);
    final BindSetting trap = new BindSetting(this, "Трапка", -1);
    private final BooleanSetting trapPreview = new BooleanSetting(this, "Проекция трапки", true);
    private final BooleanSetting trapTimer = new BooleanSetting(this, "Таймер трапки", true);
    public BindSetting plast = new BindSetting(this, "Пласт", -1);
    private final BooleanSetting plastTimer = new BooleanSetting(this, "Таймер пласта", true);
    private final BooleanSetting plastPreview = new BooleanSetting(this, "Проекция пласта", true);
    public BindSetting bojka = new BindSetting(this, "Божья аура", -1);
    public BindSetting snejok = new BindSetting(this, "Снежок заморозка", -1);

    public BindSetting ognenSmercz = new BindSetting(this, "Огненный смерч", -1);

    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    // Переменные для таймера трапки
    private boolean trapTimerActive = false;
    private long trapTimerEndsAtMs = 0L;
    private boolean prevTrapCooldown = false;
    private double trapX = 0.0, trapY = 0.0, trapZ = 0.0; // координаты где была брошена трапка

    // Переменные для таймера пласта
    private boolean plastTimerActive = false;
    private long plastTimerEndsAtMs = 0L;
    private boolean prevPlastCooldown = false;
    private double plastX = 0.0, plastY = 0.0, plastZ = 0.0; // координаты где был брошен пласт

    // Переменная для обнаружения анки в scoreboard
    private boolean ankaDetected = false;

    // Зацикленный звук для анки
    private final SoundUtil.AudioClip ankaSoundClip = SoundUtil.AudioClip.build("ankadettect.wav", true);
    private final SoundUtil.AudioClipPlayController ankaSoundController =
            SoundUtil.AudioClipPlayController.build(ankaSoundClip, () -> ankaDetected && isEnabled(), false);

    long delay;
    boolean disorientationThrow, trapThrow, ivkaThrow, plastThrow, boshkaThrow, snejokThrow;
    boolean ognenSmercThrow;

    // Универсальный движок свапов/юзов
    final SwapEngine swap = new SwapEngine();

    private void printIfEnabled(String message) {
        if (notif.getValue()) ChatUtil.addText(message);
    }

    private void notifyIfEnabled(String message, boolean success) {
        if (notif.getValue()) {
            Notifications.getInstance().pushCustomWithIcon("", message, success, "j");
        }
    }


    @EventHandler
    private void onPacketChat(PacketEvent e) {
        if (!(e.getPacket() instanceof SChatPacket packet)) return;
        if (!autogps.getValue()) return;

        String chatMessage = packet.getChatComponent().getString();

        // Отладочный вывод (можно убрать потом)
        if (chatMessage != null && (chatMessage.toLowerCase().contains("координатах") || chatMessage.contains("]") || chatMessage.matches(".*-?\\d+\\s+-?\\d+\\s+-?\\d+.*"))) {
            System.out.println("[FunTimeHelper] Получено сообщение: " + chatMessage);
        }

        EventData eventData = parseEventData(chatMessage);
        if (eventData == null) return;

        System.out.println("[FunTimeHelper] Распознан ивент: " + eventData.name + " на координатах " + eventData.x + " " + eventData.y + " " + eventData.z);

        if (ignoreBeacon.getValue() && "загадочный маяк".equalsIgnoreCase(eventData.name)) return;

        String eventKey = (eventData.name + "|" + eventData.x + "|" + eventData.y + "|" + eventData.z).toLowerCase(Locale.ROOT);
        if (eventKey.equals(lastEventKey)) return;
        lastEventKey = eventKey;

        String command;
        if (eventMarkerMode.is("Стрелочка")) {
            command = String.format(Locale.ROOT, ".gps set %d %d", eventData.x, eventData.z);
        } else {
            String safeName = eventData.name.replace('"', ' ').trim();
            if (safeName.isEmpty()) {
                safeName = "Ивент";
            }
            command = ".way add " + "Event" + " " + eventData.x + " " + eventData.y + ' ' + eventData.z;
        }

        sendCommand(command);
        if (autogpsNotify.getValue()) {
            printIfEnabled(String.format(Locale.ROOT, "Точка для ивента \"%s\" была добавлена!", eventData.name));
            notifyIfEnabled("Точка для ивента добавлена", true);
        }
    }

    private void sendCommand(String command) {
        if (mc.player != null) {
            mc.player.sendChatMessage(command);
        }
    }

    private EventData parseEventData(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) return null;

        String message = COLOR_CODE_PATTERN.matcher(rawMessage).replaceAll("");
        message = message.replace('\u00A7', ' ').trim();
        message = message.replaceAll("\\s+", " ").trim();

        // Пробуем найти координаты в скобках или без них
        Matcher matcher = EVENT_COORD_PATTERN_BRACKETS.matcher(message);
        if (!matcher.find()) {
            // Если не нашли в скобках, ищем без скобок
            matcher = EVENT_COORD_PATTERN_NO_BRACKETS.matcher(message);
            if (!matcher.find()) return null;
        }

        int x;
        int y;
        int z;
        try {
            x = Integer.parseInt(matcher.group(1));
            y = Integer.parseInt(matcher.group(2));
            z = Integer.parseInt(matcher.group(3));
        } catch (NumberFormatException ex) {
            return null;
        }

        // Извлекаем текст перед координатами
        String prefix = message.substring(0, matcher.start()).trim();

        // Убираем лишние части текста
        prefix = prefix.replaceAll("(?i)(?:на\\s+координатах.*)$", "").trim();
        prefix = prefix.replaceAll("(?i)(?:появился|появился|замечен|обнаружен|активирован|запущен).*$", "").trim();
        prefix = prefix.replaceAll("(?i)(?:появился\\s+на\\s+координатах.*)$", "").trim();
        prefix = prefix.replaceAll("(?i)^(?:ивент|событие|event)[:\\s\"«]+", "").trim();
        prefix = prefix.replaceAll("^[\\-:\\s]+", "").trim();
        prefix = prefix.replaceAll("[\"«»]", "").trim();

        // Ищем название в квадратных скобках в начале (например, [Маяк убийца])
        Pattern bracketNamePattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher bracketMatcher = bracketNamePattern.matcher(rawMessage);
        if (bracketMatcher.find()) {
            String bracketName = bracketMatcher.group(1);
            bracketName = COLOR_CODE_PATTERN.matcher(bracketName).replaceAll("");
            bracketName = bracketName.replace('\u00A7', ' ').trim();
            if (!bracketName.isEmpty()) {
                prefix = bracketName;
            }
        }

        // Если после всех обработок префикс пустой, пытаемся найти любое слово перед координатами
        if (prefix.isEmpty()) {
            // Берем последние 1-3 слова перед координатами
            String[] words = message.substring(0, matcher.start()).trim().split("\\s+");
            if (words.length > 0) {
                StringBuilder sb = new StringBuilder();
                int startIdx = Math.max(0, words.length - 3);
                for (int i = startIdx; i < words.length; i++) {
                    if (!words[i].isEmpty() && !words[i].matches("(?i)(на|координатах|появился|замечен)")) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(words[i]);
                    }
                }
                prefix = sb.toString().trim();
            }
        }

        if (prefix.isEmpty()) {
            prefix = "Ивент";
        }

        return new EventData(prefix, x, y, z);
    }

    private record EventData(String name, int x, int y, int z) {
    }

    @EventHandler
    private void onKey(EventKeyboardMouse k) {
        if (true) {
            // Используем обход через флаги (как в beame)
            if (k.getKey() == dezor.getValue()) {
                disorientationThrow = true;
            } else if (k.getKey() == trap.getValue()) {
                trapThrow = true;
            } else if (k.getKey() == plast.getValue()) {
                plastThrow = true;
            } else if (k.getKey() == yavka.getValue()) {
                ivkaThrow = true;
            } else if (k.getKey() == bojka.getValue()) {
                boshkaThrow = true;
            } else if (k.getKey() == snejok.getValue()) {
                snejokThrow = true;
            } else if (k.getKey() == ognenSmercz.getValue()) {
                ognenSmercThrow = true;
            }
        } else {
            // Используем стандартный SwapEngine
            if (k.getKey() == dezor.getValue()) {
                boolean success = swap.use(Items.ENDER_EYE, null, false);
                if (success) {
                    printIfEnabled("Дезориентация использована!");
                    notifyIfEnabled("Дезориентация использована!", true);
                } else {
                    printIfEnabled("Дезориентация не найдена!");
                    notifyIfEnabled("Дезориентация не найдена!", false);
                }
            } else if (k.getKey() == trap.getValue()) {
                boolean success = swap.use(Items.NETHERITE_SCRAP, null, false);
                if (success) {
                    printIfEnabled("Трапка использована!");
                    notifyIfEnabled("Трапка использована!", true);
                } else {
                    printIfEnabled("Трапка не найдена!");
                    notifyIfEnabled("Трапка не найдена!", false);
                }
            } else if (k.getKey() == plast.getValue()) {
                boolean success = swap.use(Items.DRIED_KELP, null, false);
                if (success) {
                    printIfEnabled("Пласт использован!");
                    notifyIfEnabled("Пласт использован!", true);
                } else {
                    printIfEnabled("Пласт не найден!");
                    notifyIfEnabled("Пласт не найден!", false);
                }
            } else if (k.getKey() == yavka.getValue()) {
                boolean success = swap.use(Items.SUGAR, null, false);
                if (success) {
                    printIfEnabled("Явная пыль использована!");
                    notifyIfEnabled("Явная пыль использована!", true);
                } else {
                    printIfEnabled("Явная пыль не найдена!");
                    notifyIfEnabled("Явная пыль не найдена!", false);
                }
            } else if (k.getKey() == bojka.getValue()) {
                boolean success = swap.use(Items.PHANTOM_MEMBRANE, null, false);
                if (success) {
                    printIfEnabled("Божья аура использована!");
                    notifyIfEnabled("Божья аура использована!", true);
                } else {
                    printIfEnabled("Божья аура не найдена!");
                    notifyIfEnabled("Божья аура не найдена!", false);
                }
            } else if (k.getKey() == snejok.getValue()) {
                boolean success = swap.use(Items.SNOWBALL, null, false);
                if (success) {
                    printIfEnabled("Снежок использован!");
                    notifyIfEnabled("Снежок использован!", true);
                } else {
                    printIfEnabled("Снежок не найден!");
                    notifyIfEnabled("Снежок не найден!", false);
                }
            } else if (k.getKey() == ognenSmercz.getValue()) {
                boolean success = swap.use(Items.FIRE_CHARGE, null, false);
                if (success) {
                    printIfEnabled("Огненный смерч использован!");
                    notifyIfEnabled("Огненный смерч использован!", true);
                } else {
                    printIfEnabled("Огненный смерч не найден!");
                    notifyIfEnabled("Огненный смерч не найден!", false);
                }
            }
        }
    }

    private void actionPotion(String nameLower, String okMsg, String failMsg) {
        boolean success = swap.use(Items.SPLASH_POTION, nameLower, false);
        if (success) {
            printIfEnabled(okMsg);
            notifyIfEnabled(okMsg, true);
        } else {
            printIfEnabled(failMsg);
            notifyIfEnabled(failMsg, false);
        }
    }


    private boolean isPlayerNearby(double radius) {
        if (mc.world == null || mc.player == null) return false;
        return mc.world.getPlayers().stream()
                .anyMatch(p -> p != mc.player &&
                        !p.isSpectator() &&
                        !Zetrix.inst().friendManager().isFriend(p.getGameProfile().getName()) &&
                        p.getDistanceSq(mc.player) <= radius * radius);
    }

    @EventHandler
    private void onUpdate(UpdateEvent e) {
        swap.tick(); // обработка отложенного восстановления

        // Проверка scoreboard на наличие анки (как в NameProtect)
        if (mc.world == null || mc.player == null) {
            ankaDetected = false;
        } else {
            Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard == null) {
                ankaDetected = false;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    ankaDetected = false;
                } else {
                    ITextComponent header = objective.getDisplayName();
                    if (header == null) {
                        ankaDetected = false;
                    } else {
                        String headerString = header.getString();
                        String headerLower = TextFormatting.getTextWithoutFormattingCodes(headerString).toLowerCase();
                        // Проверяем наличие "216" в header
                        ankaDetected = (headerLower.contains("216") && PlayerUtil.isFuntime() || headerLower.contains("215") && PlayerUtil.isFuntime() || headerLower.contains("217") || headerLower.contains("105") && PlayerUtil.isFuntime() || headerLower.contains("106") && PlayerUtil.isFuntime() || headerLower.contains("312") && PlayerUtil.isFuntime() || headerLower.contains("313") && PlayerUtil.isFuntime()) && antiDAYNANKA.getValue();
                    }
                }
            }
        }

        // Обновление статуса воспроизведения звука
        boolean shouldPlay = ankaDetected && isEnabled();
        ankaSoundController.updatePlayingStatus();

        // Установка громкости звука (если звук играет)
        if (ankaSoundController.isSucessPlaying()) {
            ankaSoundClip.setVolume(1); // 50% громкость
        } else if (shouldPlay && !ankaSoundClip.isPlaying()) {
            // Если звук должен играть, но не играет - пробуем запустить вручную
            try {
                ankaSoundClip.startPlayingAudio();
                ankaSoundClip.setVolume(1);
            } catch (Exception ex) {
                // Игнорируем ошибки, возможно файл не найден
            }
        }

        // Отслеживание кулдауна трапки для таймера
        if (trapTimer.getValue() && mc.player != null) {
            boolean cdNow = mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP);
            // Если трапка только что вошла в кулдаун
            if (cdNow && !prevTrapCooldown) {
                trapTimerActive = true;
                trapTimerEndsAtMs = System.currentTimeMillis() + 15_000L; // 15 секунд действия

                // Сохраняем координаты трапки (позиция прицела - куда смотришь)
                Vector3d eyePos = new Vector3d(mc.player.getPosX(), mc.player.getPosY() + mc.player.getEyeHeight(), mc.player.getPosZ());
                trapX = eyePos.x;
                trapY = eyePos.y;
                trapZ = eyePos.z;

                // Отладочная информация
                printIfEnabled("Трапка на координатах: " + String.format("%.1f %.1f %.1f", trapX, trapY, trapZ));
            }
            prevTrapCooldown = cdNow;

            // Проверяем, не истек ли таймер
            if (trapTimerActive && System.currentTimeMillis() >= trapTimerEndsAtMs) {
                trapTimerActive = false;
            }
        } else {
            trapTimerActive = false;
            prevTrapCooldown = false;
        }

        // Отслеживание кулдауна пласта для таймера
        if (plastTimer.getValue() && mc.player != null) {
            boolean cdNow = mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP);
            // Если пласт только что вошёл в кулдаун
            if (cdNow && !prevPlastCooldown) {
                plastTimerActive = true;
                plastTimerEndsAtMs = System.currentTimeMillis() + 20_000L; // 20 секунд действия

                // Сохраняем координаты пласта (позиция прицела + 2 блока впереди)
                Vector3d eyePos = new Vector3d(mc.player.getPosX(), mc.player.getPosY() + mc.player.getEyeHeight(), mc.player.getPosZ());
                Vector3d lookVec = mc.player.getLookVec();
                // Пласт появляется в 2 блоках перед прицелом по направлению взгляда
                Vector3d plastPos = eyePos.add(lookVec.scale(2.0));
                plastX = plastPos.x;
                plastY = plastPos.y;
                plastZ = plastPos.z;

                // Отладочная информация
                printIfEnabled("Пласт на координатах: " + String.format("%.1f %.1f %.1f", plastX, plastY, plastZ));
            }
            prevPlastCooldown = cdNow;

            // Проверяем, не истек ли таймер
            if (plastTimerActive && System.currentTimeMillis() >= plastTimerEndsAtMs) {
                plastTimerActive = false;
            }
        } else {
            plastTimerActive = false;
            prevPlastCooldown = false;
        }

        // Обход Спуки тайма всегда включен

        // Дезориентация
        if (disorientationThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.ENDER_EYE, true);
            int invSlot = getItem(Items.ENDER_EYE, false);

            if (invSlot == -1 && hbSlot == -1) {
                disorientationThrow = false;
                printIfEnabled("Дезориентация не найдена!");
                notifyIfEnabled("Дезориентация не найдена!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Дезориентация использована!");
                notifyIfEnabled("Дезориентация использована!", true);
            } else {
                printIfEnabled("Дезориентация в КД!");
                notifyIfEnabled("Дезориентация в КД!", false);
            }
            disorientationThrow = false;
        }

        // Трапка
        if (trapThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.NETHERITE_SCRAP, true);
            int invSlot = getItem(Items.NETHERITE_SCRAP, false);

            if (invSlot == -1 && hbSlot == -1) {
                trapThrow = false;
                printIfEnabled("Трапка не найдена!");
                notifyIfEnabled("Трапка не найдена!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Трапка использована!");
                notifyIfEnabled("Трапка использована!", true);
            } else {
                printIfEnabled("Трапка в КД!");
                notifyIfEnabled("Трапка в КД!", false);
            }
            trapThrow = false;
        }

        // Пласт
        if (plastThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.DRIED_KELP, true);
            int invSlot = getItem(Items.DRIED_KELP, false);

            if (invSlot == -1 && hbSlot == -1) {
                plastThrow = false;
                printIfEnabled("Пласт не найден!");
                notifyIfEnabled("Пласт не найден!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Пласт использован!");
                notifyIfEnabled("Пласт использован!", true);
            } else {
                printIfEnabled("Пласт в КД!");
                notifyIfEnabled("Пласт в КД!", false);
            }
            plastThrow = false;
        }

        // Явная пыль
        if (ivkaThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.SUGAR, true);
            int invSlot = getItem(Items.SUGAR, false);

            if (invSlot == -1 && hbSlot == -1) {
                ivkaThrow = false;
                printIfEnabled("Явная пыль не найдена!");
                notifyIfEnabled("Явная пыль не найдена!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SUGAR)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Явная пыль использована!");
                notifyIfEnabled("Явная пыль использована!", true);
            } else {
                printIfEnabled("Явная пыль в КД!");
                notifyIfEnabled("Явная пыль в КД!", false);
            }
            ivkaThrow = false;
        }

        // Божья аура
        if (boshkaThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.PHANTOM_MEMBRANE, true);
            int invSlot = getItem(Items.PHANTOM_MEMBRANE, false);

            if (invSlot == -1 && hbSlot == -1) {
                boshkaThrow = false;
                printIfEnabled("Божья аура не найдена!");
                notifyIfEnabled("Божья аура не найдена!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.PHANTOM_MEMBRANE)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Божья аура использована!");
                notifyIfEnabled("Божья аура использована!", true);
            } else {
                printIfEnabled("Божья аура в КД!");
                notifyIfEnabled("Божья аура в КД!", false);
            }
            boshkaThrow = false;
        }

        // Снежок
        if (snejokThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.SNOWBALL, true);
            int invSlot = getItem(Items.SNOWBALL, false);

            if (invSlot == -1 && hbSlot == -1) {
                snejokThrow = false;
                printIfEnabled("Снежок не найден!");
                notifyIfEnabled("Снежок не найден!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SNOWBALL)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Снежок использован!");
                notifyIfEnabled("Снежок использован!", true);
            } else {
                printIfEnabled("Снежок в КД!");
                notifyIfEnabled("Снежок в КД!", false);
            }
            snejokThrow = false;
        }

        // Огненный смерч
        if (ognenSmercThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);

            int hbSlot = getItem(Items.FIRE_CHARGE, true);
            int invSlot = getItem(Items.FIRE_CHARGE, false);

            if (invSlot == -1 && hbSlot == -1) {
                ognenSmercThrow = false;
                printIfEnabled("Огненный смерч не найден!");
                notifyIfEnabled("Огненный смерч не найден!", false);
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.FIRE_CHARGE)) {
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                printIfEnabled("Огненный смерч использован!");
                notifyIfEnabled("Огненный смерч использован!", true);
            } else {
                printIfEnabled("Огненный смерч в КД!");
                notifyIfEnabled("Огненный смерч в КД!", false);
            }
            ognenSmercThrow = false;
        }

        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @EventHandler
    private void onPacket(PacketEvent e) {
        this.handUtil.onEventPacket(e);
    }

    @EventHandler
    public void onEvent(MoveInputEvent e) {
        if (ankaDetected) {
            e.setForward(0);
            e.setStrafe(0);
            e.setSneaking(false);
            e.setJump(false);
        }
    }

    @Override
    public void onDisable() {
        disorientationThrow = trapThrow = ivkaThrow = boshkaThrow = plastThrow = snejokThrow = ognenSmercThrow = false;
        delay = 0;
        swap.reset();
        trapTimerActive = false;
        trapTimerEndsAtMs = 0L;
        prevTrapCooldown = false;
        trapX = trapY = trapZ = 0.0;
        plastTimerActive = false;
        plastTimerEndsAtMs = 0L;
        prevPlastCooldown = false;
        plastX = plastY = plastZ = 0.0;
        ankaDetected = false;

        // Останавливаем звук анки
        ankaSoundController.updatePlayingStatus();
        if (ankaSoundClip.isPlaying()) {
            ankaSoundClip.stopPlayingAudio();
        }

        super.onDisable();
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }

    private int getItem(Item input, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.isEmpty()) {
                continue;
            }
            if (itemStack.getItem() == input) {
                return i;
            }
        }
        return -1;
    }


    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        MatrixStack matrix = event.getMatrix();

        // Основной HUD рендерится только если включен renderHud
        if (!this.renderHud.getValue()) {
            return;
        }

        float dt = computeDtSeconds();

        // Создаем список всех возможных HUD элементов
        List<HudEntry> allEntries = Arrays.asList(
                new HudEntry("Дезориентация", Items.ENDER_EYE, this.dezor),
                new HudEntry("Явная пыль", Items.SUGAR, this.yavka),
                new HudEntry("Трапка", Items.NETHERITE_SCRAP, this.trap),
                new HudEntry("Пласт", Items.DRIED_KELP, this.plast),
                new HudEntry("Божья аура", Items.PHANTOM_MEMBRANE, this.bojka),
                new HudEntry("Снежок", Items.SNOWBALL, this.snejok),
                new HudEntry("Огненный смерч", Items.FIRE_CHARGE, this.ognenSmercz)
        );

        // Фильтруем только элементы с привязанными клавишами
        List<HudEntry> entriesWithBind = allEntries.stream()
                .filter(hud -> hud.bind != null && hud.bind.getValue() != -1)
                .collect(Collectors.toList());

        // Обновляем анимации для элементов с биндами
        updateAnimations(entriesWithBind, dt);

        // В режиме чата показываем все элементы для настройки
        final boolean isEmpty = entriesWithBind.isEmpty();
        boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
        if (closeCondition) return;

        // Получаем позицию
        float x = drag.position.x;
        float y = drag.position.y;

        CooldownTracker cooldownTracker = mc.player.getCooldownTracker();
        float partialTicks = mc.getRenderPartialTicks();

        float offsetX = 0f;
        float maxHeight = 16f;

        // Рендерим каждый элемент: ИКОНКА - КОЛИЧЕСТВО - БИНД
        for (HudEntry entry : entriesWithBind) {
            float animPC = getAnimation(entry.title);
            if (animPC < 0.01f) continue;

            matrix.push();

            int textColor = ColorUtil.multAlpha(InterFace.getInstance().ThemeTextHud(), animPC);

            String keyName = Keyboard.keyName(entry.bind.getValue());
            String prettyKeyName = this.prettyKey(keyName);
            float keyWidth = Fonts.SF_BOLD.getWidth(prettyKeyName, 6.5F);

            int totalCount = this.countFor(entry);

            float currentX = x + offsetX;
            float currentY = y;

            // Рисуем фон для элемента
            float elementWidth = keyWidth + 26;
            InterFace.getInstance().drawClientRect(matrix, currentX, currentY - 1, elementWidth, 16, 1, 4);

            // Рендер иконки предмета
            drawItemIconMinimal(matrix, entry, currentX + 3, currentY + 1.5F, animPC, cooldownTracker, partialTicks, totalCount);

            // Разделитель между иконкой и текстом
            RenderUtil.Rounded.smooth(matrix,currentX , currentY -1, 16, 16,ColorUtil.replAlpha(new Color(0xFF000000, true).getRGB(),  0.15F), Round.of(4,4,0,0));
            RectUtil.drawRect(matrix, currentX + 16.5F, currentY -1, 0.8F, 16, ColorUtil.getColor(200, 3));

            // Текст бинда
            Fonts.SF_BOLD.draw(
                    matrix,
                    prettyKeyName,
                    currentX + 21,
                    currentY + 3.6F,
                    textColor,
                    6.5F
            );
            matrix.pop();

            // Общая ширина этого элемента + отступ
            offsetX += (elementWidth + ITEM_SPACING + 2) * animPC;
        }

        // Обновляем размер для drag (убираем последний отступ)
        if (offsetX > 0) {
            offsetX -= ITEM_SPACING;
        }
        drag.size.set(offsetX, maxHeight);

        // Рендерим таймер трапки в 2D на экране
        if (trapTimer.getValue() && trapTimerActive) {
            long msLeft = Math.max(0L, trapTimerEndsAtMs - System.currentTimeMillis());
            float secLeft = msLeft / 1000.0f;

            if (secLeft > 0.01f) {
                String text = "До пропадания трапки: " + String.format(java.util.Locale.ROOT, "%.1fс", secLeft);

                float screenWidth = (float) mc.getMainWindow().getScaledWidth();
                float screenHeight = (float) mc.getMainWindow().getScaledHeight();

                // Позиция выше хотбара и HP (примерно 90 пикселей от низа)
                float textX = screenWidth / 2.0f;
                float textY = screenHeight - 90.0f;

                // Центрируем текст используя кастомный шрифт
                float fontSize = 7.5F;
                float textWidth = Fonts.SFP_SEMIBOLD.getWidth(text, fontSize);
                textX -= textWidth / 2.0f;

                // Рендерим текст кастомным шрифтом как в Interface
                matrix.push();
                matrix.translate(0, 0, 1000); // Поверх всего

                // Рисуем текст полностью белым кастомным шрифтом
                Fonts.SFP_SEMIBOLD.draw(matrix, text, textX, textY, -1, fontSize);

                matrix.pop();
            } else {
                trapTimerActive = false;
            }
        }

        // Рендерим предупреждение об анке на весь экран
        if (ankaDetected) {

            RectUtil.drawRect(matrix, 0, 0, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), ColorUtil.getColor(0, 100));

            RenderUtil.bindTexture(new Namespaced("texture/images.png"));
            RectUtil.drawRect(matrix, mc.getMainWindow().getScaledWidth() / 2 - 50, mc.getMainWindow().getScaledHeight() / 2 - 50, 100, 100, ColorUtil.getColor(255, 255), false, true);

            float screenWidth = (float) mc.getMainWindow().getScaledWidth();
            float screenHeight = (float) mc.getMainWindow().getScaledHeight();

            String warningText = "Внимания вы сын хуйни если играете на этой анка";
            float fontSize = 15;
            float textWidth = Fonts.SFP_SEMIBOLD.getWidth(warningText, fontSize);
            float textX = (screenWidth) / 2.0f;
            float textY = screenHeight / 2.0f;

            matrix.push();

            // Рисуем красный текст
            int redColor = ColorUtil.getColor(200, 200, 200, 255);
            Fonts.SF_BOLD.drawCenterShadow(matrix, warningText, textX, textY - 175, redColor, 12);
            redColor = ColorUtil.getColor(255, 100, 100, 255);
            Fonts.SF_BOLD.drawCenterShadow(matrix, "ЛИВАЙ НАХУЙ", textX, textY - 160, redColor, fontSize);
            redColor = ColorUtil.getColor(200, 200, 200, 255);
            Fonts.SF_BOLD.drawCenterShadow(matrix, "Тут запрещены спец прeдметы по типу дезок / трапок", textX, textY - 140, redColor, 10);

            matrix.pop();
        }
    }

    @EventHandler
    private void onRender3D(Render3DPosedEvent event) {
        MatrixStack matrix = event.getMatrix();
        ActiveRenderInfo activeRenderInfo = event.getActiveRenderInfo();

        if (activeRenderInfo.isValid() && mc.getRenderManager().options != null && mc.player != null && mc.world != null) {
            // Рендерим таймер пласта в 3D пространстве
            if (plastTimer.getValue() && plastTimerActive) {
                long msLeft = Math.max(0L, plastTimerEndsAtMs - System.currentTimeMillis());
                float secLeft = msLeft / 1000.0f;

                if (secLeft > 0.01f) {
                    String text = "До пропадания пласта: " + String.format(java.util.Locale.ROOT, "%.1fс", secLeft);
                    render3DText(matrix, event, text, plastX, plastY, plastZ);
                } else {
                    plastTimerActive = false;
                }
            }

            // Рендерим проекцию пласта 5x5, когда игрок держит ламинарию в руке
            renderPlastPreview(event);

            // Рендерим проекцию трапки вокруг игрока
            renderTrapPreview(event);

            // Рендерим проекции для дезориентации, явной пыли и жемчуга края
            renderCircularPreview(event, Items.ENDER_EYE, dezorPreview);
            renderCircularPreview(event, Items.SUGAR, yavkaPreview);
        }
    }

    private void renderPlastPreview(Render3DPosedEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!plastPreview.getValue()) return;

        ItemStack mainHand = mc.player.getHeldItemMainhand();
        ItemStack offHand = mc.player.getHeldItemOffhand();
        boolean hasLaminaria = (mainHand.getItem() == Items.DRIED_KELP) || (offHand.getItem() == Items.DRIED_KELP);

        if (!hasLaminaria) return;

        float pitch = mc.player.rotationPitch;
        float yaw = mc.player.rotationYaw;

        // Определяем расстояние: если смотрим вверх/вниз - на 1 блок дальше, горизонтально (вперед) - на 1 блок дальше
        double baseDistance = (Math.abs(pitch) > 60F) ? 4.0 : 3.0;

        Vector3d eyePos = new Vector3d(mc.player.getPosX(), mc.player.getPosY() + mc.player.getEyeHeight(), mc.player.getPosZ());
        Vector3d lookVec = mc.player.getLookVec().normalize();
        Vector3d targetPos = eyePos.add(lookVec.scale(baseDistance));

        BlockPos centerBlock = new BlockPos(targetPos);
        BlockPos baseBlockPos = centerBlock;

        AxisAlignedBB playerBB = mc.player.getBoundingBox();
        List<Entity> entities = new ArrayList<>((Collection) mc.world.getAllEntities());

        // Нормализуем yaw к диапазону 0-360
        float normalizedYaw = yaw % 360;
        if (normalizedYaw < 0) normalizedYaw += 360;

        Direction facing;
        Direction widthDir;

        // Определяем ориентацию по pitch и yaw
        // Если смотрим вверх (pitch от -45 до -90) или вниз (pitch от 45 до 90)
        if ((pitch >= -90 && pitch <= -45) || (pitch >= 45 && pitch <= 90)) {
            // Смотрим вверх/вниз - горизонтальная плоскость, ориентация зависит от yaw
            if ((normalizedYaw >= 0 && normalizedYaw < 45) || (normalizedYaw >= 315 && normalizedYaw <= 360)) {
                facing = Direction.NORTH; // Вперед
                widthDir = Direction.EAST;
            } else if (normalizedYaw >= 45 && normalizedYaw < 135) {
                facing = Direction.EAST; // Вправо
                widthDir = Direction.SOUTH;
            } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
                facing = Direction.SOUTH; // Назад
                widthDir = Direction.WEST;
            } else {
                // Влево (225-315)
                facing = Direction.WEST;
                widthDir = Direction.NORTH;
            }
        } else {
            // Смотрим горизонтально - определяем направление по yaw
            // Вбок вперед налево: 120-160 (приоритетная проверка)
            if (normalizedYaw >= 120 && normalizedYaw < 160) {
                facing = Direction.NORTH;
                widthDir = Direction.WEST;
            } else if ((normalizedYaw >= 0 && normalizedYaw < 45) || (normalizedYaw >= 315 && normalizedYaw <= 360)) {
                // Вперед
                facing = Direction.NORTH;
                widthDir = Direction.EAST;
            } else if (normalizedYaw >= 45 && normalizedYaw < 135) {
                // Вправо
                facing = Direction.EAST;
                widthDir = Direction.SOUTH;
            } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
                // Назад
                facing = Direction.SOUTH;
                widthDir = Direction.WEST;
            } else {
                // Влево (225-315)
                facing = Direction.WEST;
                widthDir = Direction.NORTH;
            }
        }

        renderPlastPlaneByBlocks(event, baseBlockPos, facing, widthDir, playerBB, entities);
    }

    private void renderPlastPlaneByBlocks(Render3DPosedEvent event,
                                          BlockPos basePos,
                                          Direction facing,
                                          Direction widthDir,
                                          AxisAlignedBB playerBB,
                                          List<Entity> entities) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int greenOutline = ColorUtil.getColor(0, 255, 0, 180);
        int redOutline = ColorUtil.getColor(255, 0, 0, 180);

        // Создаем общий bounding box для всего пласта
        AxisAlignedBB plastBB = null;
        List<BlockPos> blockPositions = new ArrayList<>();

        // Собираем все позиции блоков пласта
        for (int w = -2; w <= 2; w++) {
            for (int h = -2; h <= 2; h++) {
                for (int depth = 0; depth < 2; depth++) {
                    BlockPos blockPos = basePos
                            .offset(widthDir, w)
                            .offset(Direction.UP, h)
                            .offset(facing, depth);

                    blockPositions.add(blockPos);

                    AxisAlignedBB blockBB = new AxisAlignedBB(
                            blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                            blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1
                    );

                    if (plastBB == null) {
                        plastBB = blockBB;
                    } else {
                        plastBB = plastBB.union(blockBB);
                    }
                }
            }
        }

        // Проверяем пересечение всего пласта с энтити
        boolean plastIntersectsEntity = false;
        if (plastBB != null) {
            for (Entity entity : entities) {
                if (entity == mc.player) continue;
                if (plastBB.intersects(entity.getBoundingBox())) {
                    plastIntersectsEntity = true;
                    break;
                }
            }
        }

        int outlineColor = plastIntersectsEntity ? redOutline : greenOutline;

        RenderUtil3D.setup3dForBlockPos(event, () -> {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(2.0f);

            // Рендерим все блоки пласта строго по блокам
            for (BlockPos blockPos : blockPositions) {
                double blockX = blockPos.getX() + 0.5;
                double blockY = blockPos.getY() + 0.5;
                double blockZ = blockPos.getZ() + 0.5;

                AxisAlignedBB blockBB = new AxisAlignedBB(
                        blockX - 0.5, blockY - 0.5, blockZ - 0.5,
                        blockX + 0.5, blockY + 0.5, blockZ + 0.5
                );

                if (blockBB.intersects(playerBB)) {
                    continue;
                }

                // Используем общий цвет для всех блоков
                RenderUtil3D.drawCanisterBox(event.getMatrix(), buffer, tessellator, blockBB, true, false, false, outlineColor, 0, 0);
            }

            GL11.glLineWidth(1.0f);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }, false, false);
    }

    private boolean plastIntersectsEntities(AxisAlignedBB blockBB, List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity == mc.player) continue;
            if (blockBB.intersects(entity.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private void renderCircularPreview(Render3DPosedEvent event, Item item, BooleanSetting setting) {
        if (mc.player == null || mc.world == null) return;

        // Для жемчуга края всегда показываем, для остальных проверяем настройку
        if (setting != null && !setting.getValue()) return;

        ItemStack mainHand = mc.player.getHeldItemMainhand();
        ItemStack offHand = mc.player.getHeldItemOffhand();
        boolean hasItem = (mainHand.getItem() == item) || (offHand.getItem() == item);

        if (!hasItem) return;

        // Радиус 10 блоков
        double radius = 10.0;
        BlockPos playerBlockPos = mc.player.getPosition();
        Vector3d playerPos = new Vector3d(mc.player.getPosX(), playerBlockPos.getY() + 0.5, mc.player.getPosZ());

        AxisAlignedBB playerBB = mc.player.getBoundingBox();
        List<Entity> entities = new ArrayList<>((Collection) mc.world.getAllEntities());

        int greenOutline = ColorUtil.getColor(0, 255, 0, 180);
        int redOutline = ColorUtil.getColor(255, 0, 0, 180);

        // Для дезориентации и явной пыли проверяем весь круг на энтити
        boolean circleHasEntity = false;
        if (item == Items.ENDER_EYE || item == Items.SUGAR) {
            for (Entity entity : entities) {
                if (entity == mc.player) continue;
                Vector3d entityPos = entity.getPositionVec().add(0, entity.getHeight() / 2.0, 0);
                double distX = entityPos.x - playerPos.x;
                double distZ = entityPos.z - playerPos.z;
                double dist = Math.sqrt(distX * distX + distZ * distZ);
                if (dist <= radius) {
                    circleHasEntity = true;
                    break;
                }
            }
        }

        int circleColor = circleHasEntity ? redOutline : greenOutline;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderUtil3D.setup3dForBlockPos(event, () -> {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(2.0f);
            GL11.glDisable(GL11.GL_DEPTH_TEST); // Видно через блоки

            // Проходимся по всем блокам в квадрате радиуса
            int radiusInt = (int) Math.ceil(radius);
            for (int x = -radiusInt; x <= radiusInt; x++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    BlockPos blockPos = playerBlockPos.add(x, 0, z);
                    double blockX = blockPos.getX() + 0.5;
                    double blockY = blockPos.getY() + 0.5;
                    double blockZ = blockPos.getZ() + 0.5;

                    // Вычисляем расстояние от центра блока до игрока
                    double distX = blockX - playerPos.x;
                    double distZ = blockZ - playerPos.z;
                    double dist = Math.sqrt(distX * distX + distZ * distZ);

                    // Проверяем, находится ли блок на окружности (с допуском 0.5 блока)
                    if (Math.abs(dist - radius) > 0.5) continue;

                    AxisAlignedBB blockBB = new AxisAlignedBB(
                            blockX - 0.5, blockY - 0.5, blockZ - 0.5,
                            blockX + 0.5, blockY + 0.5, blockZ + 0.5
                    );

                    if (blockBB.intersects(playerBB)) {
                        continue;
                    }

                    // Используем общий цвет для всего круга (для дезориентации и явной пыли)
                    int outline = (item == Items.ENDER_EYE || item == Items.SUGAR) ? circleColor : greenOutline;
                    RenderUtil3D.drawCanisterBox(event.getMatrix(), buffer, tessellator, blockBB, true, false, false, outline, 0, 0);
                }
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glLineWidth(1.0f);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }, false, false);
    }

    private void renderTrapPreview(Render3DPosedEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!trapPreview.getValue()) return;

        // Проверяем, держит ли игрок трапку в руке
        ItemStack mainHand = mc.player.getHeldItemMainhand();
        ItemStack offHand = mc.player.getHeldItemOffhand();
        boolean hasTrap = (mainHand.getItem() == Items.NETHERITE_SCRAP) || (offHand.getItem() == Items.NETHERITE_SCRAP);

        if (!hasTrap) return;

        BlockPos basePos = mc.player.getPosition().down();
        AxisAlignedBB playerBB = mc.player.getBoundingBox();
        List<Entity> entities = new ArrayList<>((Collection) mc.world.getAllEntities());

        int greenOutline = ColorUtil.getColor(0, 255, 0, 180);
        int redOutline = ColorUtil.getColor(255, 0, 0, 180);

        // Создаем общий bounding box для всего куба трапки
        AxisAlignedBB trapCubeBB = new AxisAlignedBB(
                basePos.getX() - 2.5, basePos.getY(), basePos.getZ() - 2.5,
                basePos.getX() + 3.5, basePos.getY() + 5, basePos.getZ() + 3.5
        );

        // Проверяем, есть ли энтити внутри всего куба
        boolean trapHasEntity = false;
        for (Entity entity : entities) {
            if (entity == mc.player) continue;
            if (trapCubeBB.intersects(entity.getBoundingBox())) {
                trapHasEntity = true;
                break;
            }
        }

        int trapColor = trapHasEntity ? redOutline : greenOutline;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderUtil3D.setup3dForBlockPos(event, () -> {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(2.0f);

            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y <= 4; y++) {
                    for (int z = -2; z <= 2; z++) {
                        boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2 || y == 0 || y == 4;
                        if (!shell) continue;

                        BlockPos blockPos = basePos.add(x, y, z);
                        double blockX = blockPos.getX() + 0.5;
                        double blockY = blockPos.getY() + 0.5;
                        double blockZ = blockPos.getZ() + 0.5;

                        AxisAlignedBB blockBB = new AxisAlignedBB(
                                blockX - 0.5, blockY - 0.5, blockZ - 0.5,
                                blockX + 0.5, blockY + 0.5, blockZ + 0.5
                        );

                        if (blockBB.intersects(playerBB)) {
                            continue;
                        }

                        // Используем общий цвет для всего куба
                        RenderUtil3D.drawCanisterBox(event.getMatrix(), buffer, tessellator, blockBB, true, false, false, trapColor, 0, 0);
                    }
                }
            }

            GL11.glLineWidth(1.0f);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }, false, false);
    }

    private void render3DText(MatrixStack matrix, Render3DPosedEvent event, String text, double posX, double posY, double posZ) {
        matrix.push();

        // Используем координаты камеры из event
        double cameraX = event.getX();
        double cameraY = event.getY();
        double cameraZ = event.getZ();

        // Перемещаемся к позиции (текст всегда на месте posX, posY, posZ)
        matrix.translate(posX - cameraX, posY + 1.0 - cameraY, posZ - cameraZ);

        // Поворачиваем к камере
        matrix.rotate(mc.getRenderManager().getCameraOrientation());

        // Масштабируем текст
        float scale = 0.025F;
        matrix.scale(scale, -scale, scale);

        // Отражаем текст (как в DebugRenderer)
        matrix.scale(-1.0F, 1.0F, 1.0F);

        // Центрируем текст
        float textWidth = mc.fontRenderer.getStringWidth(text);
        matrix.translate(-textWidth / 2.0F, 0.0F, 0.0F);

        // Получаем финальную матрицу
        Matrix4f matrix4f = matrix.getLast().getMatrix();

        // Настройки рендеринга
        RenderSystem.enableTexture();
        RenderSystem.disableDepthTest(); // Виден сквозь стены
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();

        // Рендерим текст
        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        Fonts.SFP_MEDIUM.draw(matrix, text, 0, 0, -1, 9);
        //mc.fontRenderer.renderString(text, 0.0F, 0.0F, 0xFFFFFFFF, false, matrix4f, buffer, true, 0, 15728880);
        buffer.finish();

        // Восстанавливаем настройки
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();

        matrix.pop();
    }

    // === Вспомогательные методы ===

    /**
     * Вычисляет время с предыдущего кадра в секундах (FPS-independent)
     */
    private float computeDtSeconds() {
        long now = System.nanoTime();
        if (lastTimeNs == 0L) {
            lastTimeNs = now;
            return 1f / 60f; // первый кадр — условные ~16.7мс
        }
        long d = now - lastTimeNs;
        lastTimeNs = now;
        // clamp dt, чтобы при фризах не прыгало
        double dt = Math.min(Math.max(d / 1_000_000_000.0, 0.0), 0.1); // [0 .. 100мс]
        return (float) dt;
    }

    /**
     * Экспоненциальное сглаживание к цели с учётом dt (FPS-independent)
     */
    private float smoothTowards(float current, float target, float dt, float speedPerSec) {
        if (!Float.isFinite(dt) || dt <= 0f) return target;
        float k = 1f - (float) Math.exp(-speedPerSec * dt);
        return current + (target - current) * k;
    }

    /**
     * Обновляет анимации для всех элементов
     */
    private void updateAnimations(List<HudEntry> entries, float dt) {
        Set<String> activeItems = entries.stream()
                .map(e -> e.title)
                .collect(Collectors.toSet());

        // Обновляем анимации
        for (HudEntry entry : entries) {
            float current = itemAnimations.getOrDefault(entry.title, 0f);
            float target = 1f;
            float newValue = smoothTowards(current, target, dt, 8f);
            itemAnimations.put(entry.title, newValue);
        }

        // Удаляем анимации для неактивных элементов
        itemAnimations.keySet().removeIf(key -> {
            if (!activeItems.contains(key)) {
                float current = itemAnimations.get(key);
                float newValue = smoothTowards(current, 0f, dt, 8f);
                if (newValue < 0.01f) {
                    return true; // удаляем
                }
                itemAnimations.put(key, newValue);
            }
            return false;
        });
    }

    /**
     * Получает текущее значение анимации для элемента
     */
    private float getAnimation(String title) {
        return itemAnimations.getOrDefault(title, 0f);
    }

    /**
     * Рендерит минималистичную иконку предмета (16x16) с кулдауном
     */
    private void drawItemIconMinimal(MatrixStack matrix, HudEntry entry, float x, float y,
                                     float animPC, CooldownTracker cooldownTracker, float partialTicks, int count) {
        // Если предмета нет в инвентаре, всё равно показываем иконку
        int displayCount = Math.max(1, count);

        ItemStack itemStack;
        if (entry.item == Items.SPLASH_POTION && entry.tintRGB != null) {
            itemStack = new ItemStack(Items.SPLASH_POTION, displayCount);
            CompoundNBT nbt = itemStack.getOrCreateTag();
            nbt.putInt("CustomPotionColor", entry.tintRGB);
        } else {
            itemStack = new ItemStack(entry.item, displayCount);
        }

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float scale = 0.7F;
        RenderSystem.scalef(scale, scale, scale);

        float drawX = x / scale;
        float drawY = y / scale;

        // рендер иконки как в TargetHudRenderer
        mc.ingameGUI.renderHotbarItem((int) drawX, (int) drawY, mc.getRenderPartialTicks(), mc.player, itemStack);

        RenderSystem.popMatrix();
    }


    private int countItem(Item item) {
        int totalCount = 0;

        // Подсчитываем во всем инвентаре
        for (ItemStack stack : mc.player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                totalCount += stack.getCount();
            }
        }

        // Добавляем количество из оффхэнда
        ItemStack offhandStack = mc.player.getHeldItemOffhand();
        if (!offhandStack.isEmpty() && offhandStack.getItem() == item) {
            totalCount += offhandStack.getCount();
        }

        return totalCount;
    }

    private int countFor(HudEntry entry) {
        // Если у предмета есть фильтр по имени, используем специальный подсчет
        return entry.nameFilterLower != null
                ? this.countItemByName(entry.item, entry.nameFilterLower)
                : this.countItem(entry.item);
    }

    private int countInHotbar(HudEntry entry) {
        int hotbarCount = 0;

        // Подсчитываем только в хотбаре (слоты 0-8)
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == entry.item) {
                hotbarCount += stack.getCount();
            }
        }

        return hotbarCount;
    }

    private int countItemByName(Item item, String nameFilterLower) {
        int totalCount = 0;

        // Подсчитываем во всем инвентаре с фильтром по имени
        for (ItemStack stack : mc.player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                String displayName = TextFormatting.getTextWithoutFormattingCodes(stack.getDisplayName().getString());
                if (displayName != null && displayName.toLowerCase().contains(nameFilterLower)) {
                    totalCount += stack.getCount();
                }
            }
        }

        // Проверяем оффхэнд
        ItemStack offhandStack = mc.player.getHeldItemOffhand();
        if (!offhandStack.isEmpty() && offhandStack.getItem() == item) {
            String displayName = TextFormatting.getTextWithoutFormattingCodes(offhandStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(nameFilterLower)) {
                totalCount += offhandStack.getCount();
            }
        }

        return totalCount;
    }

    private String prettyKey(String keyName) {
        String result = keyName.toUpperCase();

        // Заменяем названия мыши на короткие версии
        result = result.replace("MOUSE5", "M5")
                .replace("MOUSE4", "M4")
                .replace("MOUSE3", "MMB")
                .replace("MOUSE2", "RMB")
                .replace("MOUSE1", "LMB");

        // Упрощаем NUMPAD
        result = result.replace("NUMPAD_", "N");

        // Сокращаем CONTROL
        result = result.replace("CONTROL", "CTRL");

        return result;
    }

    /**
     * Класс для описания элемента HUD
     *
     * @param title           Название предмета
     * @param item            Тип предмета
     * @param bind            Привязка клавиши
     * @param nameFilterLower Фильтр по имени (в нижнем регистре)
     * @param tintRGB         Цвет окраски для зелий
     */
    record HudEntry(String title, Item item, BindSetting bind, String nameFilterLower, Integer tintRGB) {
        HudEntry(String title, Item item, BindSetting bind) {
            this(title, item, bind, null, null);
        }

        HudEntry(String title, Item item, BindSetting bind, String nameFilter) {
            this(title, item, bind, nameFilter, null);
        }

        HudEntry(String title, Item item, BindSetting bind, String nameFilterLower, Integer tintRGB) {
            this.title = title;
            this.item = item;
            this.bind = bind;
            this.nameFilterLower = nameFilterLower == null ? null : nameFilterLower.toLowerCase();
            this.tintRGB = tintRGB;
        }
    }

    /**
     * ================== SwapEngine ==================
     * Универсальный движок для свапа предметов и автоматического восстановления
     */
    final class SwapEngine {
        // 70 мс — задержка перед восстановлением
        final long RESTORE_DELAY_MS = 100;

        int originalHotbarSlot = -1;  // какой хотбар-слот был выбран до юза
        boolean needRestore = false;
        long restoreAtMs = 0L;
        long lastUseMs = 0L;

        // возврат в исходный инвент-слот, если брали из инвентаря
        int srcInvIndex = -1;           // откуда взяли (9..35)
        boolean usedFromInventory = false;

        /**
         * Использовать предмет с автоматическим свапом
         *
         * @param item            предмет для использования
         * @param nameFilterLower фильтр по имени (для зелий)
         * @param allowOffhand    разрешить использование из оффхэнда
         * @return true если успешно использован
         */
        boolean use(Item item, String nameFilterLower, boolean allowOffhand) {
            if (mc.player == null || mc.world == null) return false;

            // Не используем предметы, если открыт инвентарь/сундук
            if (mc.currentScreen instanceof ContainerScreen) return false;

            long now = System.currentTimeMillis();
            if (now - lastUseMs < 50) return false;

            CooldownTracker cd = mc.player.getCooldownTracker();
            if (cd.hasCooldown(item)) return false;

            // offhand
            if (allowOffhand) {
                ItemStack off = mc.player.getHeldItemOffhand();
                if (!off.isEmpty() && off.getItem() == item && (nameFilterLower == null || containsName(off, nameFilterLower))) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                    lastUseMs = now;
                    return true;
                }
            }

            int hb = findInHotbar(item, nameFilterLower);
            if (hb != -1) {
                return useFromHotbar(hb);
            }

            int inv = findInInventory(item, nameFilterLower);
            if (inv != -1) {
                return useFromInventory(inv);
            }

            return false;
        }

        void tick() {
            if (needRestore && System.currentTimeMillis() >= restoreAtMs) {
                restore();
            }
        }

        void reset() {
            needRestore = false;
            originalHotbarSlot = -1;
            restoreAtMs = 0L;
            lastUseMs = 0L;
            usedFromInventory = false;
            srcInvIndex = -1;
        }

        // ----- helpers -----

        private boolean useFromHotbar(int targetHotbar) {
            int current = mc.player.inventory.currentItem;

            if (current != targetHotbar) {
                originalHotbarSlot = current;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(targetHotbar));
                mc.player.inventory.currentItem = targetHotbar;
            } else {
                originalHotbarSlot = -1; // уже на целевом
            }

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            scheduleRestore();
            lastUseMs = System.currentTimeMillis();
            return true;
        }

        private boolean useFromInventory(int invIndex) {
            // берём в текущую руку через pickItem, не меняя выбранный хотбар-слот
            originalHotbarSlot = mc.player.inventory.currentItem;
            usedFromInventory = true;
            srcInvIndex = invIndex;

            mc.playerController.pickItem(invIndex); // invIndex <-> current hotbar

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));

            scheduleRestore();
            lastUseMs = System.currentTimeMillis();
            handUtil.handleItemChange(System.currentTimeMillis() - delay > 200L);
            return true;
        }

        private void scheduleRestore() {
            needRestore = true;
            restoreAtMs = System.currentTimeMillis() + RESTORE_DELAY_MS;
        }

        private void restore() {
            if (!needRestore || mc.player == null) return;

            // 1) если брали из инвентаря — вернуть предмет назад в srcInvIndex
            if (usedFromInventory && srcInvIndex >= 9 && srcInvIndex < 36) {
                // в руке сейчас остаток предмета (или пусто, если израсходован)
                ItemStack held = mc.player.inventory.getStackInSlot(originalHotbarSlot);
                if (!held.isEmpty()) {
                    // вернуть обратно: swap хотбар <-> srcInvIndex
                    mc.playerController.pickItem(srcInvIndex);
                }
            }

            // 2) восстановить выбранный хотбар-слот (на случай внешних переключений)
            if (originalHotbarSlot >= 0 && originalHotbarSlot <= 8 &&
                    mc.player.inventory.currentItem != originalHotbarSlot) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(originalHotbarSlot));
                mc.player.inventory.currentItem = originalHotbarSlot;
            }

            // очистка флагов
            needRestore = false;
            originalHotbarSlot = -1;
            usedFromInventory = false;
            srcInvIndex = -1;
            restoreAtMs = 0L;
        }

        private int findInHotbar(Item item, String nameFilterLower) {
            for (int i = 0; i < 9; i++) {
                ItemStack st = mc.player.inventory.getStackInSlot(i);
                if (st.isEmpty()) continue;
                if (st.getItem() != item) continue;
                if (nameFilterLower != null && !containsName(st, nameFilterLower)) continue;
                return i;
            }
            return -1;
        }

        private int findInInventory(Item item, String nameFilterLower) {
            for (int i = 9; i < 36; i++) {
                ItemStack st = mc.player.inventory.getStackInSlot(i);
                if (st.isEmpty()) continue;
                if (st.getItem() != item) continue;
                if (nameFilterLower != null && !containsName(st, nameFilterLower)) continue;
                return i;
            }
            return -1;
        }

        private boolean containsName(ItemStack st, String nameLower) {
            String nm = TextFormatting.getTextWithoutFormattingCodes(st.getDisplayName().getString());
            return nm != null && nm.toLowerCase().contains(nameLower);
        }
    }
}
