package dev.wh1tew1ndows.client.managers.component.impl.other;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.component.Component;
import dev.wh1tew1ndows.common.impl.fastping.InetAddressPatcher;

import java.net.InetAddress;

public final class ConnectionComponent extends Component {
    public static String ip = "localhost";
    public static int port = 0;

    @SneakyThrows
    public static void connectToServer(String ip, int port, GameProfile profile) {
        ConnectionComponent.ip = ip;
        ConnectionComponent.port = port;

        InetAddress inetaddress = InetAddress.getByName(ip);
        inetaddress = InetAddressPatcher.patch(ip, inetaddress);
        NetworkManager networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
        networkManager.setNetHandler(new ClientLoginNetHandler(networkManager, mc, mc.currentScreen, (x) -> Zetrix.log(x.getString())));
        networkManager.sendPacket(new CHandshakePacket(ip, port, ProtocolType.LOGIN));
        networkManager.sendPacket(new CLoginStartPacket(profile));
    }
}