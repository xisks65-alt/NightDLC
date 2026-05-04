package dev.wh1tew1ndows.client.managers.component;


import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.component.impl.ClientComponent;
import dev.wh1tew1ndows.client.managers.component.impl.aura.AuraComponent;
import dev.wh1tew1ndows.client.managers.component.impl.drag.DragComponent;
import dev.wh1tew1ndows.client.managers.component.impl.inventory.HandComponent;
import dev.wh1tew1ndows.client.managers.component.impl.inventory.InvComponent;
import dev.wh1tew1ndows.client.managers.component.impl.other.ConnectionComponent;
import dev.wh1tew1ndows.client.managers.component.impl.other.SyncFixComponent;
import dev.wh1tew1ndows.client.managers.component.impl.other.TpsCalculateComponent;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.FreeLookComponent;
import dev.wh1tew1ndows.client.managers.component.impl.rotation.RotationComponent;
import dev.wh1tew1ndows.client.managers.component.impl.target.TargetComponent;

import java.util.HashMap;

public final class ComponentManager extends HashMap<Class<? extends Component>, Component> {


    public void init() {
        add(
                new SyncFixComponent(),
                new AuraComponent(),
                new TargetComponent(),
                new DragComponent(),
                new FreeLookComponent(),
                new RotationComponent(),

                new HandComponent(),
                new InvComponent(),
                new ConnectionComponent(),
                new ClientComponent(),
        
                new TpsCalculateComponent()
        );

        this.values().forEach(component -> Zetrix.eventHandler().subscribe(component));
    }

    public void add(Component... components) {
        for (Component component : components) {
            this.put(component.getClass(), component);
        }
    }

    public void unregister(Component... components) {
        for (Component component : components) {
            Zetrix.eventHandler().unsubscribe(component);
            this.remove(component.getClass());
        }
    }

    public <T extends Component> T get(final Class<T> clazz) {
        return this.values()
                .stream()
                .filter(component -> component.getClass() == clazz)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
}