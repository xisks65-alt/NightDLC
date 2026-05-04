package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.baritone.pathing.movement.MovementHelper;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import dev.wh1tew1ndows.common.impl.taskript.Script;
import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Speed", category = Category.MOVEMENT, desc = "Увеличение скорости передвижения игрока")
public class Speed extends Module {
    public final ModeSetting mode = new ModeSetting(this, "Режим", "HvH", "Entity", "SP-DUEL", "MetaHvH");
    private final StopWatch stopWatch = new StopWatch();
    private final Script script = new Script();

    @Override
    public void onDisable() {

    }


    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (!mc.player.isOnGround() && mode.is("HvH") && !mc.player.isElytraFlying()) {
            float melonBallSpeed = 0.36F;
            ItemStack offHandItem = mc.player.getHeldItemOffhand();
            EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
            EffectInstance DeEffect = mc.player.getActivePotionEffect(Effects.SLOWNESS);
            String itemName = offHandItem.getDisplayName().getString();

            float appliedSpeed = 0;

            if (speedEffect != null) {
                if (speedEffect.getAmplifier() == 2) {
                    appliedSpeed = melonBallSpeed * 1.155F;
                    if (itemName.contains("Ломтик Дыни")) {
                        if (speedEffect != null && speedEffect.getAmplifier() == 2) {
                            appliedSpeed = 0.41755F;
                        } else {
                            appliedSpeed = 0.41755F * 0.52F;
                        }
                    }
                } else if (speedEffect.getAmplifier() == 1) {
                    appliedSpeed = melonBallSpeed;
                }
            } else {
                appliedSpeed = melonBallSpeed * 0.68F;
            }

            if (DeEffect != null) {
                appliedSpeed *= 0.835f;
            }

            if (!mc.player.isOnGround()) {
                appliedSpeed *= 1.435F;
            }

            MoveUtil.setSpeed(appliedSpeed);
        }

        if (mode.is("MetaHvH")) {
            if (mc.player.isPotionActive(Effects.SLOWNESS)) return;
//                System.out.println((Arrays.toString(mc.player.getHeldItemOffhand().getDisplayName().getString().getBytes(StandardCharsets.UTF_8))));
            boolean sharKing = (Arrays.toString(mc.player.getHeldItemOffhand().getDisplayName().getString().getBytes(StandardCharsets.UTF_8)).equals("[-48, -88, -48, -80, -47, -128, 32, 75, 73, 78, 71]"));
            boolean sharTigr = (Arrays.toString(mc.player.getHeldItemOffhand().getDisplayName().getString().getBytes(StandardCharsets.UTF_8)).equals("[-48, -94, -48, -72, -48, -77, -47, -128, -48, -72, -48, -67, -48, -67, -48, -80, -47, -113, 32, -48, -77, -48, -66, -48, -69, -48, -66, -48, -78, -48, -80]"));
            //MovementHelper.setSpeed(0.46f); шар малекулы, проверку такую же как в 54 строчке. но уже проверять на шар малекулы
            EffectInstance effectInstance = mc.player.getActivePotionEffect(Effects.SPEED);
            if (mc.player.isPotionActive(Effects.SPEED) && effectInstance.getAmplifier() == 2 && mc.player.fallDistance <= 0.2f && !mc.player.isOnGround()) {
                MovementHelper.setSpeed(sharKing ? 0.72f : sharTigr ? 0.7f : 0.58f); //old shar king 0.88f new shar king 0.72f
            } else if (mc.player.isPotionActive(Effects.SPEED) && effectInstance.getAmplifier() == 2 && mc.player.isOnGround()) {
                MovementHelper.setSpeed(sharKing ? 0.5f : sharTigr ? 0.49f : 0.42f); //old shar king 0.6f new shar king 0.50f
            } else if (mc.player.isPotionActive(Effects.SPEED) && effectInstance.getAmplifier() == 3 && mc.player.fallDistance <= 0.2f && !mc.player.isOnGround()) {
                MovementHelper.setSpeed(0.7f);
            } else if (mc.player.isPotionActive(Effects.SPEED) && effectInstance.getAmplifier() == 3 && mc.player.isOnGround()) {
                MovementHelper.setSpeed(0.49f);
            } else if (mc.player.isPotionActive(Effects.SPEED) && effectInstance.getAmplifier() == 1 && mc.player.fallDistance <= 0.2f && !mc.player.isOnGround()) {
                MovementHelper.setSpeed(0.52f);
            } else if (mc.player.isPotionActive(Effects.SPEED) && effectInstance.getAmplifier() == 1 && mc.player.isOnGround()) {
                MovementHelper.setSpeed(0.36f);
            } else if (mc.player.fallDistance <= 0.2f && !mc.player.isOnGround() || mc.player.fallDistance <= 0.2f && mc.player.isPotionActive(Effects.SPEED)) {
                MovementHelper.setSpeed(0.36f);
            }
        }

        if (mode.is("Entity")) {
            AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.22f);
            int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
            boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
            if (canBoost && !mc.player.isOnGround()) {
                mc.player.jumpMovementFactor = armorstans > 1 ? 1f / (float) armorstans : 0.14f;

            }
        }
        if (mode.is("Test")) {
            AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.07f);
            int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
            boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
            if (canBoost && !mc.player.isOnGround()) {
                mc.player.jumpMovementFactor = armorstans > 1 ? 1f / (float) armorstans : 0.063f;

            }
        }
        if (mode.is("SP-DUEL")) {
            AxisAlignedBB aabb = mc.player.getBoundingBox().grow(1.2F);
            int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
            boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
            if (canBoost && !mc.player.isOnGround()) {
                mc.player.jumpMovementFactor = armorstans > 1 ? 1f / (float) armorstans : 0.07f;

            }
        }
    }
}
