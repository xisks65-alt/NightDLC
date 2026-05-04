package dev.wh1tew1ndows.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.api.Command;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.managers.command.api.MultiNamedCommand;
import dev.wh1tew1ndows.client.managers.command.api.Parameters;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaritoneDownloadCommand implements Command, MultiNamedCommand, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        logger.log("Baritone download is not supported.");
    }

    @Override
    public String name() {
        return "baritonedownload";
    }

    @Override
    public String description() {
        return "Загружает Baritone.";
    }

    @Override
    public List<String> aliases() {
        return List.of("bd");
    }
}
