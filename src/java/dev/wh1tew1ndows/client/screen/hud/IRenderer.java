package dev.wh1tew1ndows.client.screen.hud;


import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.managers.module.impl.render.InterFace;

public interface IRenderer extends IMinecraft {


    float fontSize = 7.5F;
    float sizeFont = 6.7F;

    void render(Render2DEvent event);

    default InterFace theme() {
        return InterFace.getInstance();
    }


}
