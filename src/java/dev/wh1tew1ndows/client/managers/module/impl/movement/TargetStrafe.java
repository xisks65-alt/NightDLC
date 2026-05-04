package dev.wh1tew1ndows.client.managers.module.impl.movement;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.ModeSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import dev.wh1tew1ndows.client.utils.player.MoveUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;

@Getter
@Accessors(fluent = true)
@ModuleInfo(name = "TargetStrafe", category = Category.MOVEMENT, desc = "Автоматически преследует игрока")
public class TargetStrafe extends Module {

    public final SliderSetting rageByte = new SliderSetting(this, "Дист", 2.5F, 0, 3, 0.1F);

    public final BooleanSetting speed = new BooleanSetting(this, "Буст скорости", true);

    public final ModeSetting speedmode = new ModeSetting(this, "Вид буста", "Обычный", "Энтити").setVisible(() -> speed.getValue());

    public final SliderSetting speed3 = new SliderSetting(this, "Скорость от энтити", 0.1F, 0.06F, 0.5F, 0.01F).setVisible(() -> speedmode.is("Энтити") && speed.getValue());

    public final SliderSetting hitBoxEntity = new SliderSetting(this, "Хит бокс энтити", 1.7F, 0.1F, 3, 0.1F).setVisible(() -> speedmode.is("Энтити") && speed.getValue());


    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (!mc.player.isOnGround() && speed.getValue() && speedmode.is("Обычный")) {
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
        } else if (speed.getValue()) {
            AxisAlignedBB aabb = mc.player.getBoundingBox().grow(hitBoxEntity.getValue());
            int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
            boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
            if (canBoost && !mc.player.isOnGround()) {
                mc.player.jumpMovementFactor = armorstans > 1 ? 1f / (float) armorstans : speed3.getValue();

            }
        }
    }

}
