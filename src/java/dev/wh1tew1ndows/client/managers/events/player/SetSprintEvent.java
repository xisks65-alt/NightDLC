package dev.wh1tew1ndows.client.managers.events.player;

import dev.wh1tew1ndows.client.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.LivingEntity;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SetSprintEvent extends Event {
    private LivingEntity living;
    private boolean state;
}
