package dev.wh1tew1ndows.client.managers.events.other;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@RequiredArgsConstructor
public class EntityRemoveEvent extends Event {
    private final Entity entity;
}