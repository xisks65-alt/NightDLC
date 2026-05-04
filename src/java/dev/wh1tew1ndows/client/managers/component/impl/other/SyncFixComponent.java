package dev.wh1tew1ndows.client.managers.component.impl.other;

import net.minecraft.client.settings.KeyBinding;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.world.WorldLoadEvent;

public class SyncFixComponent extends Component {
    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        KeyBinding.unPressAllKeys();
        for (KeyBinding binding : mc.gameSettings.keyBindings) {
            binding.setPressed(false);
        }
    }
}
