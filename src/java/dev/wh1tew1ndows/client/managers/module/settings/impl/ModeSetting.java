package dev.wh1tew1ndows.client.managers.module.settings.impl;


import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;

import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {

    public List<String> values;
    private String cachedValue;
    private java.util.function.Supplier<List<String>> dynamicValues = null;

    public ModeSetting(Module parent, String name, String... values) {
        super(parent, name, values[0]);
        this.values = new java.util.ArrayList<>(List.of(values));
        this.set(values[0]);
        cachedValue = values[0];
    }

    /** Устанавливает динамический поставщик списка значений */
    public ModeSetting setDynamicValues(java.util.function.Supplier<List<String>> supplier) {
        this.dynamicValues = supplier;
        return this;
    }

    /** Возвращает актуальный список значений */
    public List<String> getValues() {
        if (dynamicValues != null) {
            return dynamicValues.get();
        }
        return values;
    }

    public boolean hasDynamicValues() {
        return dynamicValues != null;
    }

    public int getIndex() {
        List<String> list = getValues();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(getValue())) return i;
        }
        return 0;
    }

    public boolean is(String value) {
        return getValue().equalsIgnoreCase(value) && getVisible().get();
    }

    @Override
    public ModeSetting set(String value) {
        ModeSetting set = (ModeSetting) super.set(value);
        this.cachedValue = super.getValue();
        return set;
    }

    @Override
    public ModeSetting setVisible(Supplier<Boolean> value) {
        return (ModeSetting) super.setVisible(value);
    }

    @Override
    public ModeSetting onAction(Runnable action) {
        return (ModeSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public ModeSetting onSetVisible(Runnable action) {
        return (ModeSetting) super.onSetVisible(action);
    }

    @Override
    public String getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        if (dynamicValues != null) {
            List<String> current = getValues();
            if (!current.contains(cachedValue)) {
                cachedValue = current.isEmpty() ? "Нет" : current.get(0);
            }
        }
        return cachedValue;
    }
}