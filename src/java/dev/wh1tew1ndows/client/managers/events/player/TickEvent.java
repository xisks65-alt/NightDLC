package dev.wh1tew1ndows.client.managers.events.player;

public interface TickEvent {
    default boolean isCancelled() {
        return false;
    }

    default void setCancelled(boolean cancelled) {

    }
}