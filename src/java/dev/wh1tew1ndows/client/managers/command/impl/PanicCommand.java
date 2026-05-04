package dev.wh1tew1ndows.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.api.Command;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.managers.command.api.MultiNamedCommand;
import dev.wh1tew1ndows.client.managers.command.api.Parameters;
import dev.wh1tew1ndows.client.managers.module.Module;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PanicCommand implements Command, MultiNamedCommand, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        int disabledModulesCount = 0;
        for (Module module : Zetrix.inst().moduleManager().values()) {
            if (!module.isAutoEnabled() && module.isAllowDisable() && module.isEnabled()) {
                module.setEnabled(false, false);
                disabledModulesCount++;
            }
        }
        logger.log(TextFormatting.GRAY + "Все функции клиента (" + disabledModulesCount + " шт) были выключены.");
    }

    @Override
    public String name() {
        return "panic";
    }

    @Override
    public String description() {
        return "Выключает все функции клиента.";
    }

    @Override
    public List<String> aliases() {
        return List.of("p");
    }
}
