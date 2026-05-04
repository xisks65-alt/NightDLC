package dev.wh1tew1ndows.client.managers.events.render;

import dev.wh1tew1ndows.client.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MainWindow;
import net.mojang.blaze3d.matrix.MatrixStack;

@Getter
@Setter
@AllArgsConstructor
public class RenderPre2DEvent extends Event {
    private MatrixStack matrix;
    private MainWindow mainWindow;
    private float partialTicks;
    private double mouseX, mouseY;
}