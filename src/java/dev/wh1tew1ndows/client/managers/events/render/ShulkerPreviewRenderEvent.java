package dev.wh1tew1ndows.client.managers.events.render;


import dev.wh1tew1ndows.client.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.mojang.blaze3d.matrix.MatrixStack;

@Getter
@Setter
@AllArgsConstructor
public class ShulkerPreviewRenderEvent extends Event {
    private MatrixStack matrixStack;
    private int x;
    private int y;
    private Slot hoveredSlot;
    private ContainerScreen<?> screen;
}
