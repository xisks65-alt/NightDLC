package dev.wh1tew1ndows.client.managers.module.impl.combat;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.AttackEvent;
import dev.wh1tew1ndows.client.managers.events.player.MotionEvent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.client.utils.other.ViaUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Criticals", category = Category.COMBAT, desc = "Гарантированные критические удары")
public class Criticals extends Module {
    public static Criticals getInstance() {
        return Instance.get(Criticals.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "Grim", "Grim-New", "HvH/Matrix/Test", "MetaHvH");

    @Override
    public void onEnable() {
    }

    boolean bostY = false;

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mode.is("MetaHvH")) {
            if (mc.player != null &&
                    mc.player.getCooledAttackStrength(2F) == 1F &&
                    mc.player.isOnGround() &&
                    AttackAura.getInstance().target != null) {

                mc.player.motion.y = 0.04;
                mc.player.isAirBorne = true;
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        Vector3d pos = mc.player.getPositionVec();
        float yaw = mc.player.lastReportedYaw;
        float pitch = mc.player.lastReportedPitch;
        if (mode.is("HvH/Matrix/Test") && mc.player.isOnGround()) {
            bostY = true;
            if (!mc.player.isOnGround() && mc.player.fallDistance > 0.2F && !mc.player.isElytraFlying()) {
                bostY = false;
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y - 1e-6, pos.z, yaw, pitch, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-6, pos.z, yaw, pitch, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, yaw, pitch, false));
            }
        }
        if (mode.is("Grim-New") && !mc.player.isOnGround()) {
            if (!mc.player.isOnGround() && !mc.player.isElytraFlying()) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y - 1e-6, pos.z, yaw, pitch, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-6, pos.z, yaw, pitch, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y, pos.z, yaw, pitch, false));
            }
        }
        if (ViaUtil.allowedBypass() && !mc.player.isElytraFlying() && !mc.player.isInWater()) {
            if (mode.is("Grim")) {
                if (!mc.player.isOnGround() && mc.player.fallDistance == 0) {
                    mc.player.fallDistance = 0.001f;
                    if (AttackAura.getInstance().target != null) {
                        return;
                    }
                    ViaUtil.sendPositionPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-6, mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, false);
                }
            }
        }
    }

    @EventHandler
    public void onAttack(MotionEvent event) {
        if (bostY) {
            event.setY(mc.player.getPosY() + 0.5F);
        }
    }

    private void critPacket(double yDelta, boolean full) {
        if (full)
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), false));
        else
            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), mc.player.lastReportedYaw, mc.player.lastReportedPitch, false));
    }
}