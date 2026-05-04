package dev.wh1tew1ndows.client.managers.command.api.logger;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinecraftLogger implements Logger {
    @Override
    public void log(String message) {
        ChatUtil.addText(message);
    }
}
