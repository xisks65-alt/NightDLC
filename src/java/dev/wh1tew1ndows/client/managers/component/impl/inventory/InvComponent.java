package dev.wh1tew1ndows.client.managers.component.impl.inventory;

import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.events.orbit.EventPriority;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.events.player.UpdateEvent;
import dev.wh1tew1ndows.client.utils.other.Instance;
import dev.wh1tew1ndows.common.impl.taskript.Script;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.settings.KeyBinding;

@Getter
@Accessors(fluent = true)
public class InvComponent extends Component {
    private final Script script = new Script();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(UpdateEvent event) {
        script.update();
    }

    public static void addTask(Runnable task, int priority) {
        addTask(task, priority, true);
    }

    public static void addTask(Runnable task, int priority, boolean cleanup) {
        Script script = getInstance().script();
        if (cleanup) script.cleanup();
        script.addTickStep(2, KeyBinding::unPressAllMoveKeys, priority)
                .addTickStep(2, () -> {
                    task.run();
                    KeyBinding.updateKeyBindState();
                }, priority);
    }

    public static InvComponent getInstance() {
        return Instance.getComponent(InvComponent.class);
    }
}
