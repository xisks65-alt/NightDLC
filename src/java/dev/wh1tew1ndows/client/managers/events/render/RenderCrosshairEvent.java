package dev.wh1tew1ndows.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.mojang.blaze3d.matrix.MatrixStack;
import dev.wh1tew1ndows.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public final class RenderCrosshairEvent extends CancellableEvent {
    private MatrixStack matrix;
    private float partialTicks;
    private double centerX, centerY;
}