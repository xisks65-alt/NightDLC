package dev.wh1tew1ndows.client.managers.module.impl.combat;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.impl.target.TargetComponent;
import dev.wh1tew1ndows.client.managers.events.other.EntityRayTraceEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoEntityTrace", category = Category.COMBAT, desc = "Oтключает хитбоксы энтити")
public class NoEntityTrace extends Module {
    public static NoEntityTrace getInstance() {
        return Instance.get(NoEntityTrace.class);
    }

    @EventHandler
    public void onEvent(EntityRayTraceEvent event) {
        if (event.getEntity() instanceof LivingEntity living && TargetComponent.getTargets(6, true).contains(living)) {
            event.cancel();
        }
    }
}