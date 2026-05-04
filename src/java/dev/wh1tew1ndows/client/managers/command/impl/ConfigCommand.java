package dev.wh1tew1ndows.client.managers.command.impl;

import dev.wh1tew1ndows.client.api.client.Constants;
import dev.wh1tew1ndows.client.managers.command.CommandException;
import dev.wh1tew1ndows.client.managers.command.api.*;
import dev.wh1tew1ndows.client.managers.module.impl.render.ClickGuiModule;
import dev.wh1tew1ndows.client.managers.other.config.ConfigFile;
import dev.wh1tew1ndows.client.managers.other.config.ConfigManager;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigCommand implements Command, CommandWithAdvice, MultiNamedCommand {

    final ConfigManager configManager;
    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");
        configManager.update();
        switch (commandType) {
            case "load" -> loadConfig(parameters);
            case "save" -> saveConfig(parameters);
            case "list" -> configList();
            case "dir" -> getDirectory();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " load, save, list, dir");
        }
    }

    @Override
    public String name() {
        return "config";
    }

    @Override
    public String description() {
        return "Позволяет взаимодействовать с конфигами в чите";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();

        return List.of(commandPrefix + name() + " load <config> - Загрузить конфиг",
                commandPrefix + name() + " save <config> - Сохранить конфиг",
                commandPrefix + name() + " list - Получить список конфигов",
                commandPrefix + name() + " dir - Открыть папку с конфигами",
                "Пример: " + TextFormatting.RED + commandPrefix + "cfg save myConfig",
                "Пример: " + TextFormatting.RED + commandPrefix + "cfg load myConfig"

        );
    }

    @Override
    public List<String> aliases() {
        return List.of("cfg");
    }

    private void loadConfig(Parameters parameters) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига!"));

        if (new File(ConfigManager.CONFIG_DIRECTORY, configName + Constants.FILE_FORMAT).exists()) {
            final ConfigFile config = configManager.get(configName);
            if (config != null) {
                CompletableFuture.runAsync(() -> {
                    if (config.read()) {
                        configManager.set();
                        logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " загружена!");
                        if (ClickGuiModule.getInstance().getKey() == Keyboard.KEY_NONE.getKey()) {
                            ClickGuiModule.getInstance().setKey(Keyboard.KEY_RIGHT_SHIFT.getKey());
                        }
                    } else {
                        logger.log(TextFormatting.RED + "Конфигурация " + TextFormatting.GRAY + configName + TextFormatting.RED + " не найдена!");
                    }
                });
            } else {
                logger.log(TextFormatting.RED + "Конфигурация " + TextFormatting.GRAY + configName + TextFormatting.RED + " не найдена!");
            }
        } else {
            logger.log(TextFormatting.RED + "Конфигурация " + TextFormatting.GRAY + configName + TextFormatting.RED + " не найдена!");
        }
    }

    private void saveConfig(Parameters parameters) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига!"));

        CompletableFuture.runAsync(() -> {
            configManager.set(configName);
            configManager.set();

            logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " сохранена!");
        });
    }

    private void configList() {
        if (configManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список конфигураций пустой");
            return;
        }
        logger.log(TextFormatting.GRAY + "Список конфигов:");

        configManager.set();
        configManager.update();
        configManager.forEach(configFile -> {
            final String configName = configFile.getFile().getName().replace(Constants.FILE_FORMAT, "");
            final String configCommand = ".cfg load " + configName;
            final String color = String.valueOf(TextFormatting.GREEN);

            final StringTextComponent chatText = new StringTextComponent(color + "> " + configName);
            final StringTextComponent hoverText = new StringTextComponent(TextFormatting.RED + "Конфиг: " + configName);

            chatText.setStyle(Style.EMPTY.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, configCommand)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));

            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(chatText);
        });
    }

    private void getDirectory() {
        Util.getOSType().openFile(ConfigManager.CONFIG_DIRECTORY);
        configManager.set();
        logger.log(TextFormatting.RED + "Папка с конфигами открыта");
    }
}
