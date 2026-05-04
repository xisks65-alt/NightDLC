package dev.wh1tew1ndows.client.managers.events.player;

import dev.wh1tew1ndows.client.api.events.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;


@AllArgsConstructor
@Getter
@Setter
public class FireworkMotionEvent extends CancellableEvent {
    private LivingEntity entity;
    private double speed;
    private double interpolation;
}
