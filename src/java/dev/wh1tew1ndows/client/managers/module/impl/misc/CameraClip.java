package dev.wh1tew1ndows.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.events.other.CameraClipEvent;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.ModuleInfo;
import dev.wh1tew1ndows.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CameraClip", category = Category.MISC, desc = "Позволяет камере проходить сквозь блоки")
public class CameraClip extends Module {
    public static CameraClip getInstance() {
        return Instance.get(CameraClip.class);
    }

    @EventHandler
    public void onEvent(CameraClipEvent event) {
        event.cancel();
    }
}