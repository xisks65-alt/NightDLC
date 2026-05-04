package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.MovementInputKeysEvent;
import dev.wh1tew1ndows.client.managers.events.other.PacketEvent;
import dev.wh1tew1ndows.client.managers.events.player.SetSprintEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "InvMove", category = Category.MOVEMENT, desc = "Позволяет двигаться при открытом инвентаре")
public class InvMove extends Module {

    public static InvMove getInstance() {
        return Instance.get(InvMove.class);
    }

    public final ModeSetting type = new ModeSetting(this, "Мод", "Обычный", "Grim", "Spooky");

    // Очередь пакетов для отправки после стопа
    private final List<CClickWindowPacket> packetQueue = new ArrayList<>();

    // Дедупликация пакетов кликов
    private final LinkedList<CClickWindowPacket> windowClickPacketQueue = new LinkedList<>();

    // Таймер для Grim/SpookyTime
    private final StopWatch timer = new StopWatch();

    // Тики стопа (Обычный)
    private int tick = 0;

    // Тики после стопа (Grim/SpookyTime)
    private int ticksPostOnStop = 0;

    // Счётчик тиков остановки
    private int stopTicksOut = 0;

    // Флаг — игрок остановлен
    private boolean stoppedStatus = false;

    private final KeyBinding[] moveKeys = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump
    };

    // ─── onUpdate ────────────────────────────────────────────────────────────

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mc.player == null) return;

        // --- Обычный ---
        if (type.is("Обычный")) {
            KeyBinding[] allKeys = new KeyBinding[]{
                    mc.gameSettings.keyBindForward,
                    mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft,
                    mc.gameSettings.keyBindRight,
                    mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint
            };

            if (tick > 0) {
                for (KeyBinding k : allKeys) k.setPressed(false);
                tick--;
                return;
            }

            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) return;
            updateKeyBindingState(allKeys);
            return;
        }

        // --- Grim ---
        if (type.is("Grim")) {
            // Обрабатываем тики стопа
            if (stopTicksOut > 0) {
                stopTicksOut--;
                if (stopTicksOut == 0) {
                    stoppedStatus = false;
                    useAccumulatedPackets();
                }
                return;
            }

            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) return;

            // Только в инвентаре игрока
            if (mc.currentScreen instanceof InventoryScreen) {
                updateKeyBindingState(moveKeys);
            }
            return;
        }

        // --- Spooky (Grim fork) ---
        if (type.is("Spooky")) {
            // Обрабатываем тики стопа — отправляем сразу по окончании
            if (stopTicksOut > 0) {
                stopTicksOut--;
                if (stopTicksOut == 0) {
                    stoppedStatus = false;
                    useAccumulatedPackets();
                }
                return;
            }

            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) return;

            Arrays.stream(moveKeys).forEach(k ->
                    k.setPressed(InputMappings.isKeyDown(mc.getMainWindow().getHandle(), k.getDefault().getKeyCode()))
            );
        }
    }

    // ─── onSendPacket ─────────────────────────────────────────────────────────

    @EventHandler
    public void onSendPacket(PacketEvent event) {
        if (!type.is("Grim") && !type.is("Spooky")) return;

        if (event.isReceive()) {
            // Отменяем SCloseWindowPacket только в инвентаре игрока — в серверных меню (/darena и др.) пусть закрывается
            if (event.getPacket() instanceof SCloseWindowPacket && mc.currentScreen instanceof InventoryScreen) {
                event.cancel();
            }
            return;
        }

        // Исходящий CClickWindowPacket
        if (event.getPacket() instanceof CClickWindowPacket toSend) {
            if (canStoppingOnWindowClick()) {
                if (toSend.getSlotId() != -1) {
                    // Сначала добавляем в очередь, потом ставим стоп
                    if (rememberCClickWindowPacket(toSend)) {
                        setStop(toSend.getClickType());
                        ticksPostOnStop = 0;
                        event.cancel();
                    }
                }
            }
        }
    }

    // ─── onSprintAction ───────────────────────────────────────────────────────

    @EventHandler
    public void onSprintAction(SetSprintEvent event) {
        if (!type.is("Grim") && !type.is("Spooky")) return;

        // Блокируем спринт пока игрок остановлен
        if (stoppedStatus) {
            event.setState(false);
        }
    }

    // ─── onKeyMove ────────────────────────────────────────────────────────────

    @EventHandler
    public void onKeyMove(MovementInputKeysEvent event) {
        if (!type.is("Grim") && !type.is("Spooky")) return;

        if (mc.currentScreen instanceof ChatScreen) return;

        // Подменяем ввод реальными нажатиями клавиш
        event.spoofAsKeyboard(false);

        // Если остановлены — блокируем WASD
        if (stoppedStatus) {
            event.stopWASD();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    // Публичный метод для внешних модулей (AutoSwap и др.)
    // Добавляет пакет в очередь InvMove и активирует стоп — пакет отправится по логике мода
    public void queueSwapPacket(CClickWindowPacket packet) {
        if (!this.isEnabled()) return;
        if (rememberCClickWindowPacket(packet)) {
            setStop(packet.getClickType());
        }
    }

    private boolean canStoppingOnWindowClick() {
        return mc.currentScreen instanceof ContainerScreen || mc.currentScreen instanceof InventoryScreen;
    }

    private boolean rememberCClickWindowPacket(CClickWindowPacket packetIn) {
        if (!windowClickPacketQueue.contains(packetIn)) {
            return windowClickPacketQueue.add(packetIn);
        }
        return false;
    }

    private void setStop(ClickType clickType) {
        if (!this.isEnabled()) return;

        if (clickType == null) clickType = ClickType.PICKUP;

        int offset = ticksWindowClickOffset(clickType);

        // Если уже в стопе — продлеваем, не сбрасываем
        this.stopTicksOut = Math.max(this.stopTicksOut, offset + 1);
        this.stoppedStatus = true;
        this.timer.reset();
    }

    private int ticksWindowClickOffset(ClickType clickType) {
        switch (clickType) {
            case QUICK_MOVE:
                return type.is("Spooky") ? 2 : 5;
            case SWAP:
            case CLONE:
            case THROW:
                return type.is("Spooky") ? 1 : 4;
            case QUICK_CRAFT:
            default:
                return type.is("Spooky") ? 1 : 3;
        }
    }

    private void useAccumulatedPackets() {
        if (windowClickPacketQueue.isEmpty()) return;

        windowClickPacketQueue.removeIf(packet -> {
            mc.player.connection.sendPacketWithoutEvent(packet);
            return true;
        });
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding k : keyBindings) {
            k.setPressed(InputMappings.isKeyDown(mc.getMainWindow().getHandle(), k.getDefault().getKeyCode()));
        }
    }

    public boolean isMoving() {
        if (mc.player == null) return false;
        return mc.player.movementInput.moveForward != 0.0F || mc.player.movementInput.moveStrafe != 0.0F;
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        useAccumulatedPackets();
        windowClickPacketQueue.clear();
        packetQueue.clear();
        ticksPostOnStop = 0;
        stoppedStatus = false;
        stopTicksOut = 0;
        tick = 0;
    }

    @Override
    public void onDisable() {
        useAccumulatedPackets();
        ticksPostOnStop = 0;
        stopTicksOut = 0;
        stoppedStatus = false;
        packetQueue.clear();
        super.onDisable();
    }
}
