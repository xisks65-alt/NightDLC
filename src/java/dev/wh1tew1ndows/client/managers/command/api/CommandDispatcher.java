package dev.wh1tew1ndows.client.managers.command.api;


import dev.wh1tew1ndows.client.managers.command.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
