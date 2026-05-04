package dev.wh1tew1ndows.client.managers.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.managers.command.api.AdviceCommandFactory;
import dev.wh1tew1ndows.client.managers.command.api.CommandProvider;
import dev.wh1tew1ndows.client.managers.command.api.Logger;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;

    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}
