package dev.wh1tew1ndows.client.managers.command.impl;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.command.CommandException;
import dev.wh1tew1ndows.client.managers.command.api.*;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.managers.module.impl.render.ClickGuiModule;
import dev.wh1tew1ndows.client.managers.module.settings.Setting;
import dev.wh1tew1ndows.client.managers.module.settings.impl.BindSetting;
import dev.wh1tew1ndows.client.utils.keyboard.Keyboard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BindCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addBindToModule(parameters, logger);
            case "remove" -> removeBindFromModule(parameters, logger);
            case "clear" -> clearAllBindings(logger);
            case "list" -> listBoundKeys(logger);
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear, list");
        }
    }

    @Override
    public String name() {
        return "bind";
    }

    @Override
    public String description() {
        return "Позволяет забиндить функцию на определенную клавишу";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "bind add <function> <key> - Добавить новый бинд",
                commandPrefix + "bind remove <function> <key> - Удалить бинд",
                commandPrefix + "bind list - Получить список биндов",
                commandPrefix + "bind clear - Очистить список биндов",
                "Пример: " + TextFormatting.RED + commandPrefix + "bind add KillAura R"
        );
    }

    private void addBindToModule(Parameters parameters, Logger logger) {
        String functionName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название функции!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        Module module = null;

        for (Module func : Zetrix.inst().moduleManager().values()) {
            if (func.getName().toLowerCase(Locale.ROOT).equals(functionName.toLowerCase(Locale.ROOT))) {
                module = func;
                break;
            }
        }

        int key = Keyboard.keyCode(keyName.toUpperCase());

        if (module == null) {
            logger.log(TextFormatting.RED + "Функция " + functionName + " не была найдена");
            return;
        }

        module.setKey(key);
        logger.log(TextFormatting.GREEN + "Бинд " + TextFormatting.RED
                + keyName.toUpperCase() + TextFormatting.GREEN
                + " был установлен для функции " + TextFormatting.RED + functionName);
    }

    private void removeBindFromModule(Parameters parameters, Logger logger) {
        String moduleName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название функции!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        Zetrix.inst().moduleManager().values().stream()
                .filter(module -> module.getName().equalsIgnoreCase(moduleName))
                .forEach(module -> {
                    module.setKey(Keyboard.KEY_NONE.getKey());
                    logger.log(TextFormatting.GREEN + "Клавиша " + TextFormatting.RED + keyName.toUpperCase()
                            + TextFormatting.GREEN + " была отвязана от функции " + TextFormatting.RED + module.getName());
                });
    }

    private void clearAllBindings(Logger logger) {
        Zetrix.inst().moduleManager().values().forEach(module -> {
            if (!(module instanceof ClickGuiModule)) {
                module.setKey(Keyboard.KEY_NONE.getKey());
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof BindSetting bindSetting) {
                        bindSetting.set(Keyboard.KEY_NONE.getKey());
                    }
                }
            }
        });
        logger.log(TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    private void listBoundKeys(Logger logger) {
        logger.log(TextFormatting.GRAY + "Список всех модулей с привязанными клавишами:");
        Zetrix.inst().moduleManager().values().stream()
                .filter(module -> module.getKey() != Keyboard.KEY_NONE.getKey())
                .map(module -> {
                    String keyName = Keyboard.keyName(module.getKey());
                    keyName = keyName != null ? keyName : "";
                    return String.format("%s [%s%s%s]", module.getName(), TextFormatting.GRAY, keyName, TextFormatting.WHITE);
                })
                .forEach(logger::log);
    }

}
