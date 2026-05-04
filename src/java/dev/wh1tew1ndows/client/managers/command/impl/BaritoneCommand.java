package dev.wh1tew1ndows.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.api.Command;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.managers.command.api.MultiNamedCommand;
import dev.wh1tew1ndows.client.managers.command.api.Parameters;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaritoneCommand implements Command, MultiNamedCommand, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String input = parameters.collectMessage(0);
        if (input == null || input.isBlank()) {
            logger.log("Usage: .baritone <command>");
            return;
        }
        try {
            Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
            Object provider = apiClass.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object cmdManager = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            cmdManager.getClass().getMethod("execute", String.class).invoke(cmdManager, input);
        } catch (Exception e) {
            logger.log("Baritone not available: " + e.getMessage());
        }
    }

    @Override
    public String name() {
        return "baritone";
    }

    @Override
    public String description() {
        return "Отправляет команду в Baritone.";
    }

    @Override
    public List<String> aliases() {
        return List.of("b");
    }
}
