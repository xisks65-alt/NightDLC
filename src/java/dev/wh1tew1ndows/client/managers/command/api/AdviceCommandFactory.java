package dev.wh1tew1ndows.client.managers.command.api;


import dev.wh1tew1ndows.client.managers.command.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
