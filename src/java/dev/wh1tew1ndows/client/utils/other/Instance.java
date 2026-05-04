package dev.wh1tew1ndows.client.utils.other;

import lombok.experimental.UtilityClass;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@UtilityClass
public class Instance {
    private final ConcurrentMap<Class<? extends Module>, Module> instances = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends Component>, Component> componentInstances = new ConcurrentHashMap<>();

    public <T extends Module> T get(Class<T> clazz) {
        return clazz.cast(instances.computeIfAbsent(clazz, instance -> Zetrix.inst().moduleManager().get(instance)));
    }

    public <T extends Component> T getComponent(Class<T> clazz) {
        return clazz.cast(componentInstances.computeIfAbsent(clazz, instance -> Zetrix.inst().componentManager().get(instance)));
    }

    public <T extends Module> Supplier<T> getSupplier(Class<T> clazz) {
        return () -> clazz.cast(instances.computeIfAbsent(clazz, instance -> Zetrix.inst().moduleManager().get(instance)));
    }

    public <T extends Module> T get(final String module) {
        return Zetrix.inst().moduleManager().get(module);
    }

    public List<Module> get(final Category category) {
        return Zetrix.inst().moduleManager().get(category);
    }
}
