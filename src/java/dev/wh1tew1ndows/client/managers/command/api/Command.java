package dev.wh1tew1ndows.client.managers.command.api;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();
}
