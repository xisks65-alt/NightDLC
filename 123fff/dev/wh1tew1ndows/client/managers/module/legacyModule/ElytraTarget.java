package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.impl.combat.AttackAura;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.player.InventoryUtil;
import dev.wh1tew1ndows.client.utils.rotation.SensUtil;
import dev.wh1tew1ndows.client.utils.time.TimerUtil;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.clamp;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ElytraTarget", category = Category.COMBAT, desc = "Автоматическое преследование на элитрах")
public class ElytraTarget extends Module {


    public final BooleanSetting offlyth = new BooleanSetting(this, "Отключать проверку луча при таргете [Риск бана на холиворлде]", false);

    public final BooleanSetting autofirefork = new BooleanSetting(this, "Авто феерверк", false);
    public final SliderSetting fireworktick = new SliderSetting(this, "Скорость пускание", 300f, 50f, 500f, 50f).setVisible(() -> autofirefork.getValue());

    public final StopWatch timer = new StopWatch();
    public boolean disabled = false;


    public SliderSetting distance = new SliderSetting(this, "Дистанция на элитре", 15, 5, 60, 1);
    public SliderSetting drivingdistance = new SliderSetting(this, "Расстояние перегона", 3, 1, 6, 1);
    public BindSetting drivingkey = new BindSetting(this, "Клавиша перегона", -1);


    public int driving = 0;
    public static Vector2f rot = Vector2f.ZERO;
    LivingEntity target = null;


    @EventHandler
    public void onEvent(UpdateEvent e) {
        target = AttackAura.getInstance().target;
        if (target != null) {
            Vector3d vec3d = new Vector3d(target.getPosX(), target.getPosY(), target.getPosZ());

            if (mc.player.isElytraFlying()) {
                smartRotation();
            }
        }
    }


    private void smartRotation() {
        Vector3d vec3d = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(), 0, target.getHeight() * (mc.player.getDistance(target) / AttackAura.getInstance().attackDistance())), 0).subtract(mc.player.getEyePosition(1.0F));

        if (mc.player.isElytraFlying()) {
            if (driving > 0) {
                Vector3d targetPosition = target.getPositionVec();
                Vector3d scale = target.getForward().normalize().scale(drivingdistance.getValue());
                vec3d = targetPosition.add(scale);
            } else {
                vec3d = getVector(target);
            }
        }
        double vecX = vec3d.x - ((mc.player.isElytraFlying() && driving > 0) ? mc.player.getPosX() : 0);
        double vecY = vec3d.y - ((mc.player.isElytraFlying() && driving > 0) ? mc.player.getPosY() : 0);
        double vecZ = vec3d.z - ((mc.player.isElytraFlying() && driving > 0) ? mc.player.getPosZ() : 0);
        float[] rotations = new float[]{(float) Math.toDegrees(Math.atan2(vecZ, vecX)) - 90.0F, (float) (-Math.toDegrees(Math.atan2(vecY, hypot(vecX, vecZ))))};
        float deltaYaw = wrapDegrees(calculateDelta(rotations[0], rot.x));
        float deltaPitch = calculateDelta(rotations[1], rot.y);
        float limitedYaw = Math.min(Math.max(Math.abs(deltaYaw), 1.0F), 360);
        float limitedPitch = Math.min(Math.max(Math.abs(deltaPitch), 1.0F), 90);
        float finalYaw = rot.x + (deltaYaw > 0.0F ? limitedYaw : -limitedYaw);
        float finalPitch = clamp(rot.y + (deltaPitch > 0.0F ? limitedPitch : -limitedPitch), -90.0F, 90.0F);
        float gcd = SensUtil.getGCDValue();
        finalYaw = finalYaw - (finalYaw - rot.x) % gcd;
        finalPitch = finalPitch - (finalPitch - rot.y) % gcd;

        RotationComponent.update(new Rotation(finalYaw + ThreadLocalRandom.current().nextFloat(-1, 1), finalPitch + ThreadLocalRandom.current().nextFloat(-1, 1)), 360, 360, 0, 30);

        rot = new Vector2f(finalYaw, finalPitch);


    }


    public static Vector3d getSpookyVector(LivingEntity target) {
        double yExpand = net.minecraft.util.math.MathHelper.clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());
        double xExpand = net.minecraft.util.math.MathHelper.clamp(mc.player.getPosX() - target.getPosX(), -0, 0);
        double zExpand = MathHelper.clamp(mc.player.getPosZ() - target.getPosZ(), -0, 0);

        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }

    public static Vector3d getVector(LivingEntity target) {

        double wHalf = target.getWidth() / 2;

        double yExpand = clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());

        double xExpand = clamp(mc.player.getPosX() - target.getPosX(), -wHalf, wHalf);
        double zExpand = clamp(mc.player.getPosZ() - target.getPosZ(), -wHalf, wHalf);

        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }


    public static float calculateDelta(float a, float b) {
        return a - b;
    }

    public static float wrapDegrees(float value) {
        float f = value % 360.0F;

        if (f >= 180.0F) {
            f -= 360.0F;
        }

        if (f < -180.0F) {
            f += 360.0F;
        }

        return f;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
        if (mc.player != null)
            rot = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        mc.timer.setSpeed(1);
    }

    @EventHandler
    public void onKey(EventKeyboardMouse e) {
        if (e.getKey() == (drivingkey.getValue())) {
            if (driving == 0) {
                ChatUtil.addText("Komar - Перегон Включен");
                driving = 1;
            } else {
                ChatUtil.addText("Komar - Перегон Выключен");
                driving = 0;
            }
        }
    }

    final TimerUtil stopWatch = new TimerUtil();

    @EventHandler
    public void onUpdate(MotionEvent e) {
        int Firework = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                Firework = i;
                break;
            }
            Firework = -1;
        }
        if (mc.player.isElytraFlying()) {
            if (autofirefork.getValue() && AttackAura.getInstance().isEnabled() && AttackAura.getInstance().target != null && !mc.player.isSwingInProgress) {
                if (Firework != -1 && stopWatch.isReached(fireworktick.getValue().intValue())) {
                    if (Firework >= 0 && Firework < 9) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(Firework));
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    } else {
                        int emptyHotbarSlot = InventoryUtil.findEmptySlot(true);
                        if (emptyHotbarSlot == -1) emptyHotbarSlot = 8;
                        mc.playerController.windowClick(0, Firework, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, emptyHotbarSlot + 36, 0, ClickType.PICKUP, mc.player);
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(emptyHotbarSlot));
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        mc.playerController.windowClick(0, emptyHotbarSlot + 36, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, Firework, 0, ClickType.PICKUP, mc.player);
                    }
                    stopWatch.reset();
                }
            }
        }
    }
}

