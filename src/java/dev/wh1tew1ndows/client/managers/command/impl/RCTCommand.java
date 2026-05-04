package dev.wh1tew1ndows.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.play.client.CPlayerPacket;
import dev.wh1tew1ndows.client.api.events.Handler;
import dev.wh1tew1ndows.client.api.events.orbit.EventHandler;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.managers.command.api.Command;
import dev.wh1tew1ndows.client.managers.command.api.Logger;
import dev.wh1tew1ndows.client.managers.command.api.MultiNamedCommand;
import dev.wh1tew1ndows.client.managers.command.api.Parameters;
import dev.wh1tew1ndows.client.managers.events.render.Render2DEvent;
import dev.wh1tew1ndows.client.utils.chat.ChatUtil;
import dev.wh1tew1ndows.client.utils.player.PlayerUtil;
import dev.wh1tew1ndows.common.impl.taskript.Script;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RCTCommand extends Handler implements Command, MultiNamedCommand, IMinecraft {
    private final Script script = new Script();

    final Logger logger;
    @NonFinal
    boolean reconnecting = false;

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (reconnecting && mc.ingameGUI.overlayPlayerList.header != null && mc.ingameGUI.overlayPlayerList.header.getString().contains("Режим: Хаб #")) {
            script.update();
        }
    }

    @Override
    public void execute(Parameters parameters) {
        if (PlayerUtil.isPvp()) {
            logger.log("Вы не можете перезаходить на анархию в режиме PVP");
            //    return;
        }

        int server = PlayerUtil.getAnarchy();

        if (server == -1) {
            logger.log("Не удалось получить номер анархии.");
            return;
        }

        reconnecting = true;
        ChatUtil.sendText("/hub");
        script.cleanup().addStep(1000, () -> {
            ChatUtil.sendText("/an" + server);
        }).addStep(2000, () -> {
            for (int i = 0; i < 10; i++)
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.isOnGround()));
            mc.player.respawnPlayer();
            for (int i = 0; i < 10; i++)
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.isOnGround()));
            ChatUtil.addText("1");
            reconnecting = false;
        });
    }

    @Override
    public String name() {
        return "rct";
    }

    @Override
    public String description() {
        return "Перезаходит на анархию";
    }


    @Override
    public List<String> aliases() {
        return Collections.singletonList("reconnect");
    }
}
