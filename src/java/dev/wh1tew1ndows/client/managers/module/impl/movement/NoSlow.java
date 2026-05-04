package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.SlowWalkingEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoSlow", category = Category.MOVEMENT, desc = "Отключение замедления при использовании предметов")
public class NoSlow extends Module {
    public static NoSlow getInstance() {
        return Instance.get(NoSlow.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Mode", "Grim", "Grim/tick", "Grim/air");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final StopWatch stopWatch = new StopWatch();
    public TimerUtil timerUtil = new TimerUtil();
    private final Random random = new Random();
    int ticks = 0;
    private int tick = 0;

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mc.player.isHandActive()) {
            ticks++;
        } else {
            ticks = 0;
        }
    }

    @EventHandler
    public void onSlowWalk(SlowWalkingEvent e) {
        if (mc.player == null || mc.player.isElytraFlying()) return;

        switch (mode.getValue()) {
            case "Grim" -> {
                if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getActiveHand() == Hand.MAIN_HAND) {
                    return;
                }

                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand()));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
                e.cancel();
            }
            case "Grim/tick" -> {
                handleGrim(e);
            }
            case "Grim/air" -> {
                if (!mc.player.isOnGround()) {
                    if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getActiveHand() == Hand.MAIN_HAND) {
                        return;
                    }

                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand()));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
                    e.cancel();
                }
            }
        }
    }

    private void handleGrim(SlowWalkingEvent event) {
        if (mc.player.getItemInUseCount() > 27)
            return;

        if (!mc.player.isHandActive()) {
            tick = 0;
            return;
        }

        if (tick > 0) {
            event.cancel();
            tick = 0;
        } else {
            tick++;
        }
    }

    public boolean isBlockUnderWithMotion() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(mc.player.getMotion().x, -0.1, mc.player.getMotion().z);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
}