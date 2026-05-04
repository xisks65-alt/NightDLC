package dev.wh1tew1ndows.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.ScoreObjective;
import net.mojang.blaze3d.matrix.MatrixStack;
import dev.wh1tew1ndows.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class RenderScoreBoardEvent extends CancellableEvent {
    private MatrixStack matrix;
    private ScoreObjective objective;
    private FontRenderer fontRenderer;
    private int scaledWidth;
    private int scaledHeight;
}
