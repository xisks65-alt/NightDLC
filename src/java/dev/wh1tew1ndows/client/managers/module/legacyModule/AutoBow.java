package dev.wh1tew1ndows.client.managers.module.legacyModule;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.Rotation;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.managers.module.settings.impl.SliderSetting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector2f;

import java.util.Comparator;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "BowHelper", category = Category.COMBAT, desc = "Автоматическая стрельба из лука")
public class AutoBow extends Module {
    private final SliderSetting distance = new SliderSetting(this, "Дистанция", 10, 6, 20, 1);
    private final SliderSetting delay = new SliderSetting(this, "Задержка", 6, 3, 15, 1);
    PlayerEntity target;

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mc.player.getActiveItemStack().getItem() == Items.BOW) {
            target = findTarget();
            if (target == null) return;

            Vector2f rotation = calculateRotation(target);
            RotationComponent.update(new Rotation(rotation.x, rotation.y), 45, 45, 0, 30);
        }
    }

    public PlayerEntity findTarget() {
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && mc.player.canEntityBeSeen(p) && mc.player.getDistance(p) < distance.getValue())
                .min(Comparator.comparingDouble(p -> mc.player.getDistance(p)))
                .orElse(null);
    }

    public Vector2f calculateRotation(PlayerEntity target) {
        // сила натяжения лука
        float drawTime = (mc.player.getItemInUseMaxCount() - mc.player.getItemInUseCount()) / 20.0F;
        drawTime = (drawTime * drawTime + drawTime * 2.0f) / 3.0f;
        if (drawTime > 1.0f) drawTime = 1.0f;

        // скорость стрелы (макс ~3.0)
        float velocity = drawTime * 3.0f;

        // предикт движения цели
        double dx = target.getPosX() - target.prevPosX;
        double dz = target.getPosZ() - target.prevPosZ;

        double distance = mc.player.getDistance(target);
        double predictTicks = distance / velocity; // через сколько тиков долетит
        double predictedX = target.getPosX() + dx * predictTicks;
        double predictedZ = target.getPosZ() + dz * predictTicks;
        double predictedY = target.getPosY() + target.getEyeHeight(target.getPose());

        // дельты
        double xDiff = predictedX - mc.player.getPosX();
        double zDiff = predictedZ - mc.player.getPosZ();
        double yDiff = predictedY - (mc.player.getPosY() + mc.player.getEyeHeight(mc.player.getPose()));

        double flatDist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

        // расчёт yaw
        float yaw = (float) (Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0F);

        // расчёт pitch с учётом гравитации
        float g = 0.01f; // сила гравитации для стрелы
        double v2 = velocity * velocity;

        // формула баллистики: θ = atan((v² ± sqrt(v⁴ - g(gx² + 2yv²))) / (gx))
        double part = v2 * v2 - g * (g * flatDist * flatDist + 2 * yDiff * v2);

        if (part < 0) {
            // цель слишком далеко — берём "прямой" угол, чтобы не крашнуло
            return new Vector2f(yaw, -45f);
        }

        double pitch = Math.atan((v2 - Math.sqrt(part)) / (g * flatDist));
        pitch = -Math.toDegrees(pitch);

        return new Vector2f(yaw, (float) pitch);
    }

}
