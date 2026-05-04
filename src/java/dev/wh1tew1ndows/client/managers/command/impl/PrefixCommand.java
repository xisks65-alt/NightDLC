package dev.wh1tew1ndows.client.managers.command.impl;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.api.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrefixCommand implements Command, MultiNamedCommand, IMinecraft {
    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String newPrefix = parameters.asString(0).orElse("");

        if (newPrefix.isEmpty()) {
            // Если параметры не указаны - показываем текущий префикс
            logger.log(TextFormatting.GRAY + "Текущий префикс: " + TextFormatting.WHITE + prefix.get());
            logger.log(TextFormatting.GRAY + "Использование: " + TextFormatting.WHITE + prefix.get() + "prefix <новый префикс>");
            logger.log(TextFormatting.GRAY + "Пример: " + TextFormatting.WHITE + prefix.get() + "prefix !");
            return;
        }

        // Проверка на валидность префикса
        if (newPrefix.length() > 3) {
            logger.log(TextFormatting.RED + "Ошибка: Префикс должен быть не длиннее 3 символов!");
            return;
        }

        // Сохраняем старый префикс для вывода
        String oldPrefix = prefix.get();

        // Устанавливаем новый префикс
        prefix.set(newPrefix);

        logger.log(TextFormatting.GRAY + "Префикс изменён с " + TextFormatting.WHITE + oldPrefix +
                TextFormatting.GRAY + " на " + TextFormatting.WHITE + newPrefix);
        logger.log(TextFormatting.GRAY + "Теперь используйте: " + TextFormatting.WHITE + newPrefix + " help");
    }

    @Override
    public String name() {
        return "prefix";
    }

    @Override
    public String description() {
        return "Изменяет префикс команд клиента.";
    }

    @Override
    public List<String> aliases() {
        return List.of("п");
    }
}

