package dev.wh1tew1ndows.client.managers.events.render;

import dev.wh1tew1ndows.client.api.events.Event;
import lombok.Getter;
import lombok.Setter;
import net.mojang.blaze3d.matrix.MatrixStack;

@Getter
@Setter
public class EventRenderer3D extends Event {
    private MatrixStack stack;
    private float partialTicks;

    public EventRenderer3D(MatrixStack stack, float partialTicks) {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }


}

