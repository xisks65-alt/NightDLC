package dev.wh1tew1ndows.client.managers.module.settings;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.utils.animation.Animation;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class Setting<Value> implements ISetting, IMinecraft {
    private Runnable onAction;
    private Runnable onSetVisible;
    private Value value;
    @Getter
    public final String name;
    private Supplier<Boolean> visible = () -> true;
    private Module parent;
    private final Animation animation = new Animation();

    public Setting(String name, Value value) {
        this.name = name;
        this.value = value;
    }

    public Setting(Module parent, String name, Value value) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        parent.getSettings().add(this);
    }

    public Setting<?> set(Value value) {
        this.value = value;
        if (mc.world != null && mc.player != null && onAction != null) {
            onAction.run();
        }
        return this;
    }


    @Override
    public Setting<?> setVisible(Supplier<Boolean> value) {
        visible = value;
        if (mc.world != null && mc.player != null && onSetVisible != null) {
            onSetVisible.run();
        }
        return this;
    }

    public Setting<?> onAction(Runnable action) {
        this.onAction = action;
        return this;
    }

    public Setting<?> onSetVisible(Runnable action) {
        this.onSetVisible = action;
        return this;
    }
}