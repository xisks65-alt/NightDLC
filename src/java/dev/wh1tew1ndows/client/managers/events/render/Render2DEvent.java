package dev.wh1tew1ndows.client.managers.events.render;

import dev.wh1tew1ndows.client.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.mojang.blaze3d.matrix.MatrixStack;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class Render2DEvent extends Event {
    @Getter
    private static final Render2DEvent instance = new Render2DEvent();
    public MatrixStack matrixStack;
    private MatrixStack matrix;
    private ActiveRenderInfo activeRender;
    private MainWindow mainWindow;
    private float partialTicks;


    public void set(MatrixStack matrix, ActiveRenderInfo activeRender, MainWindow mainWindow, float partialTicks) {
        this.matrix = matrix;
        this.activeRender = activeRender;
        this.mainWindow = mainWindow;
        this.partialTicks = partialTicks;
    }
}