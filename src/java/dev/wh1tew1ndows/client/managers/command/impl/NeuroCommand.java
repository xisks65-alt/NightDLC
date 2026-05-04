package dev.wh1tew1ndows.client.managers.command.impl;

import dev.wh1tew1ndows.client.managers.command.CommandException;
import dev.wh1tew1ndows.client.managers.command.api.*;
import dev.wh1tew1ndows.client.managers.neuro.NeuroManager;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Команда для управления нейро-аурой.
 * .neuro record <название> — начать запись
 * .neuro stop              — остановить запись и обучить модель
 * .neuro reload            — перезагрузить модели с диска
 * .neuro list              — список доступных моделей
 */
public class NeuroCommand implements Command, CommandWithAdvice {

    private final Prefix prefix;
    private final Logger logger;

    public NeuroCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
    }

    @Override
    public void execute(Parameters parameters) {
        String sub = parameters.asString(0)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажи подкоманду: record, stop, reload, list"));

        switch (sub.toLowerCase()) {
            case "record" -> {
                String name = parameters.asString(1)
                        .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажи название модели: .neuro record <название>"));
                NeuroManager.getInstance().startRecording(name);
            }
            case "stop" -> NeuroManager.getInstance().stopRecording();
            case "reload" -> {
                NeuroManager.getInstance().reloadModels();
                logger.log(TextFormatting.GREEN + "[Нейро] Модели перезагружены.");
            }
            case "list" -> {
                List<String> names = NeuroManager.getInstance().getModelNames();
                logger.log(TextFormatting.AQUA + "[Нейро] Доступные модели:");
                for (String n : names) logger.log(TextFormatting.GRAY + "  - " + n);
            }
            default -> throw new CommandException(TextFormatting.RED + "Неизвестная подкоманда: " + sub);
        }
    }

    @Override
    public String name() { return "neuro"; }

    @Override
    public String description() { return "Управление нейро-аурой"; }

    @Override
    public List<String> adviceMessage() {
        String p = prefix.get();
        return List.of(
                p + "neuro record <название> — начать запись ПВП сессии",
                p + "neuro stop              — остановить запись и обучить модель",
                p + "neuro reload            — перезагрузить модели с диска",
                p + "neuro list              — список доступных моделей"
        );
    }
}
