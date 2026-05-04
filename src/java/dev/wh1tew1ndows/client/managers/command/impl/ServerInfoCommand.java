package dev.wh1tew1ndows.client.managers.command.impl;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextFormatting;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.api.Command;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.managers.command.api.Parameters;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerInfoCommand implements Command, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        ServerData data = mc.getCurrentServerData();
        if (data == null || mc.isSingleplayer()) {
            logger.log(TextFormatting.GRAY + "Эта команда не работает в одиночном мире.");
            return;
        }

        ServerAddress serverAddress = ServerAddress.fromString(data.serverIP);

        logger.log(TextFormatting.GRAY + "Server info:");
        logger.log(TextFormatting.GRAY + "Name: " + TextFormatting.DARK_GRAY + data.serverName);
        logger.log(TextFormatting.GRAY + "IP: " + TextFormatting.DARK_GRAY + serverAddress.getIP() + ":" + serverAddress.getPort());
        logger.log(TextFormatting.GRAY + "Players: " + TextFormatting.DARK_GRAY + data.populationInfo.getString());
        logger.log(TextFormatting.GRAY + "MOTD: " + TextFormatting.DARK_GRAY + data.serverMOTD.getString());
        logger.log(TextFormatting.GRAY + "ServerVersion: " + TextFormatting.DARK_GRAY + data.gameVersion.getString());
        logger.log(TextFormatting.GRAY + "ProtocolVersion: " + TextFormatting.DARK_GRAY + data.version + " (" + ProtocolVersion.getProtocol(data.version).getName() + ")");
        logger.log(TextFormatting.GRAY + "Ping: " + TextFormatting.DARK_GRAY + data.pingToServer);
    }

    @Override
    public String name() {
        return "serverinfo";
    }

    @Override
    public String description() {
        return "Отображает информацию про сервер.";
    }
}
