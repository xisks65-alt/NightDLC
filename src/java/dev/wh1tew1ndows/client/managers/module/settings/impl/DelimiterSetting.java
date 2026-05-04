package dev.wh1tew1ndows.client.managers.module.settings.impl;

import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;

import java.util.function.Supplier;

public class DelimiterSetting extends Setting<String> {

    public DelimiterSetting(Module parent, String name) {
        super(parent, name, "");
    }

    @Override
    public DelimiterSetting setVisible(Supplier<Boolean> value) {
        return (DelimiterSetting) super.setVisible(value);
    }

    @Override
    public DelimiterSetting onAction(Runnable action) {
        return (DelimiterSetting) super.onAction(action);
    }

    @Override
    public DelimiterSetting onSetVisible(Runnable action) {
        return (DelimiterSetting) super.onSetVisible(action);
    }

    @Override
    public String getValue() {
        return super.getValue();
    }
}
