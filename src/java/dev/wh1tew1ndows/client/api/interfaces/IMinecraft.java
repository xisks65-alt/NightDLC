package dev.wh1tew1ndows.client.api.interfaces;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

public interface IMinecraft {
    Minecraft mc = Minecraft.getInstance();
    MainWindow mw = mc.getMainWindow();

}
