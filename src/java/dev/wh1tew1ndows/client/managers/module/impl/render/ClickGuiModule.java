package dev.wh1tew1ndows.client.managers.module.impl.render;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.annotations.Client;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.input.EventKeyboardMouse;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Client
@ModuleInfo(name = "ClickGui", category = Category.RENDER, autoEnabled = true, allowDisable = false, key = GLFW.GLFW_KEY_RIGHT_SHIFT, desc = "Графический интерфейс для управления модулями")
public class ClickGuiModule extends Module {
    public static ClickGuiModule getInstance() {
        return Instance.get(ClickGuiModule.class);
    }

    @EventHandler
    public void onKey(EventKeyboardMouse event) {
        if (event.getKey() == (getKey())) {
            Minecraft.getInstance().displayScreen(Zetrix.inst().clickGui());
        }
    }
}