package dev.wh1tew1ndows.client.utils.server;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Server implements IMinecraft {
	
	public boolean is(String serverName) {
		if (mc.getCurrentServerData() == null) {
			return false;
		}
		String serverIP = mc.getCurrentServerData().serverIP.toLowerCase();
		return serverIP.contains(serverName.toLowerCase());
	}
}



