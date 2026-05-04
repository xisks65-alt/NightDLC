package dev.wh1tew1ndows.client.managers.command;

import dev.wh1tew1ndows.client.managers.command.api.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommand implements Command {

    final CommandProvider commandProvider;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandName = parameters.asString(0).orElseThrow(() -> new CommandException(TextFormatting.RED + "Вы не указали имя команды"));
        Command command = commandProvider.command(commandName);

        if (commandName.isEmpty()) {
            throw new CommandException(TextFormatting.RED + "Вы не указали имя команды");
        }

        if (!(command instanceof CommandWithAdvice commandWithAdvice)) {
            throw new CommandException(TextFormatting.RED + "К данной команде нет советов!");
        }

        logger.log(TextFormatting.WHITE + "Пример использования команды:");
        for (String advice : commandWithAdvice.adviceMessage()) {
            logger.log(TextFormatting.GRAY + advice);
        }
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "null";
    }
}
