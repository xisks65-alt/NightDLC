package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.FireworkMotionEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BooleanSetting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

@Getter
@Accessors(fluent = true)
@ModuleInfo(name = "ElytraBooster", category = Category.MOVEMENT, desc = "Ускорение полета на элитрах")
public class ElytraBooster extends Module {

    private final SliderSetting fireworkSpeed = new SliderSetting(this, "Скорость буста", 1.60F, 1.50F, 2.0F, 0.01F).setVisible((() -> !getSmartSpeed().getValue()));
    private final BooleanSetting smartSpeed = new BooleanSetting(this, "Smart скорость", false);
    private final SliderSetting minBoost = new SliderSetting(this, "Мин. скорость", 1.60F, 1.50F, 2.0F, 0.01F).setVisible((() -> getSmartSpeed().getValue()));
    private final SliderSetting maxBoost = new SliderSetting(this, "Макс скорость", 1.60F, 1.50F, 2.5F, 0.01F).setVisible((() -> getSmartSpeed().getValue()));
    private final BooleanSetting disableInterpolation = new BooleanSetting(this, "Отключить интерполяцию", false);


    @EventHandler
    public void onEvent(FireworkMotionEvent fireworkMotion) {
        final LivingEntity entity = fireworkMotion.getEntity();
        if (!(entity instanceof ClientPlayerEntity player)) return;

        boolean disableInterpolationBoolean = disableInterpolation.getValue();
        double speed = smartSpeed.getValue() ? getBoost() : fireworkSpeed.getValue();
        speed += (disableInterpolationBoolean ? 0.1F : 0.0F);
        fireworkMotion.setSpeed(speed);
        if (disableInterpolationBoolean) {
            fireworkMotion.setInterpolation(1.0D);
        }
    }


    private static final float BASE_PITCH_BOOST = 1.94f;
    private static final float PITCH_BOOST_INCREMENT = 0.05f;
    private static final float YAW_BOOST_FACTOR = 0.56f;
    private static final float ADD_YAW_BOOST_FACTOR = 0.1f;

    private static final int[] YAW_VECTORS = {-45, 45, 135, -135};
    private static final int[] ADDITIONAL_YAW_VECTORS = {-90, 90, 180, -180, 0};
    private static final int[] PITCH_VECTORS = {-45, 45};


    public double getBoost() {
        float boost = fireworkSpeed.getValue();

        float lastYaw = mc.player.rotationYaw;
        float lastPitch = mc.player.rotationPitch;

        boost = adjustBoostForYaw(lastYaw, boost);

        boost = adjustBoostForPitch(lastPitch, boost);

        return Math.min(boost, maxBoost.getValue());
    }

    private float adjustBoostForYaw(float lastYaw, float boost) {
        int closestYawIndex = findClosestVector(lastYaw, YAW_VECTORS);
        if (closestYawIndex == -1) {
            return fireworkSpeed.getValue();
        }

        float yawDistance = Math.abs(MathHelper.wrapDegrees(lastYaw) - YAW_VECTORS[closestYawIndex]);
        boost = 2.06f - yawDistance * YAW_BOOST_FACTOR / 45f;

        int closestAddYawIndex = findClosestVector(lastYaw, ADDITIONAL_YAW_VECTORS);
        float addYawDistance = Math.abs(MathHelper.wrapDegrees(lastYaw) - ADDITIONAL_YAW_VECTORS[closestAddYawIndex]);
        if (addYawDistance < 10) {
            boost += ADD_YAW_BOOST_FACTOR * (1 - addYawDistance / 10f);
        }

        return boost;
    }

    private float adjustBoostForPitch(float lastPitch, float boost) {
        int closestPitchIndex = findClosestVector(lastPitch, PITCH_VECTORS);
        float pitchDistance = Math.abs(Math.abs(lastPitch) - Math.abs(PITCH_VECTORS[closestPitchIndex]));

        if (pitchDistance < 26) {
            boost = Math.max(BASE_PITCH_BOOST, boost);
            boost += PITCH_BOOST_INCREMENT * (1 - pitchDistance / 26f);
        }

        if (lastPitch > -55 && lastPitch < -19) {
            return 1.91f;
        } else if (lastPitch < -55 || lastPitch > 55) {
            return minBoost.getValue();
        } else if (lastPitch > 19 && lastPitch < 55) {
            return 1.8f;
        }

        return boost;
    }

    private static int findClosestVector(float angle, int[] vectors) {
        int minDistIndex = -1;
        float minDist = Float.MAX_VALUE;

        for (int i = 0; i < vectors.length; i++) {
            float dist = Math.abs(MathHelper.wrapDegrees(angle) - vectors[i]);
            if (dist < minDist) {
                minDist = dist;
                minDistIndex = i;
            }
        }

        return minDistIndex;
    }

    public BooleanSetting getSmartSpeed() {
        return smartSpeed;
    }
}



