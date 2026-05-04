package dev.wh1tew1ndows.client.managers.module.legacyModule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.AxisAlignedBB;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Parkour", category = Category.PLAYER, desc = "Помощник для паркура и прыжков")
public class Parkour extends Module {
    public static Parkour getInstance() {
        return Instance.get(Parkour.class);
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (isBlockUnder() && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    public boolean isBlockUnder() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(0, -0.1, 0);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
}