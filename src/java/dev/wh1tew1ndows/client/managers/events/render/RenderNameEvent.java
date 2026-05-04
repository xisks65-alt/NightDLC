package dev.wh1tew1ndows.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import dev.wh1tew1ndows.client.api.events.CancellableEvent;

@Getter
@AllArgsConstructor
public final class RenderNameEvent extends CancellableEvent {
    private Entity entity;
}