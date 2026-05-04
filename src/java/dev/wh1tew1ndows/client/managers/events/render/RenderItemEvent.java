package dev.wh1tew1ndows.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.mojang.blaze3d.matrix.MatrixStack;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class RenderItemEvent extends Event {
    private MatrixStack matrix;
    private final Hand hand;
    private final HandSide handSide;
    private float swingProgress;
    private float equippedProgress;
}