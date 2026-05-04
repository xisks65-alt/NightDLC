package dev.wh1tew1ndows.client.screen.hud;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.events.render.RenderScoreBoardEvent;

public interface IScoreBoardRenerer extends IMinecraft {
    void renderScoreBoard(RenderScoreBoardEvent event);
}
