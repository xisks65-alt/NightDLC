package dev.wh1tew1ndows.client.managers.events.render;

import dev.wh1tew1ndows.client.api.events.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.mojang.blaze3d.matrix.MatrixStack;

@Getter
@AllArgsConstructor
public class RenderHotbarEvent extends CancellableEvent {
    private final MatrixStack matrix;
    private final float partialTicks;
}