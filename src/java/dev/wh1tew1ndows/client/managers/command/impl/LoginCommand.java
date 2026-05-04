package dev.wh1tew1ndows.client.managers.command.impl;

import com.mojang.authlib.GameProfile;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.CommandException;
import dev.wh1tew1ndows.client.managers.command.api.Command;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.managers.command.api.MultiNamedCommand;
import dev.wh1tew1ndows.client.managers.command.api.Parameters;
import dev.wh1tew1ndows.client.managers.component.impl.other.ConnectionComponent;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.common.user.LoginManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginCommand implements Command, MultiNamedCommand, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String username = parameters.asString(0)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите никнейм для авторизации."));

        if (PlayerUtil.isInvalidName(username)) {
            logger.log(TextFormatting.RED + "Недопустимое имя.");
            return;
        }

        mc.session = new Session(username);
        LoginManager.saveUsername(username);
        //   Zetrix.inst().accountManager().addAccount(new Account(LocalDateTime.now(), username));
        GameProfile gameProfile = mc.session.getProfile();

        logger.log(TextFormatting.GRAY + "Ваш никнейм изменён на - " + TextFormatting.WHITE + username);
        if (!mc.isSingleplayer()) {
            mc.world.sendQuittingDisconnectingPacket();
            ConnectionComponent.connectToServer(ConnectionComponent.ip, ConnectionComponent.port, gameProfile);
        }

    }

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String description() {
        return "Авторизирует вас под ником, который вы ввели";
    }

    @Override
    public List<String> aliases() {
        return List.of("l");
    }
}
