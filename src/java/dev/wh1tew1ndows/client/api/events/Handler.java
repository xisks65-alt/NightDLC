package dev.wh1tew1ndows.client.api.events;


import dev.wh1tew1ndows.client.Zetrix;

public abstract class Handler {
    public Handler() {
        Zetrix.eventHandler().subscribe(this);
    }
}