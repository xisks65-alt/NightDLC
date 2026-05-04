package dev.wh1tew1ndows.common.impl.fastping;

import com.google.common.net.InetAddresses;
import lombok.experimental.UtilityClass;
import dev.wh1tew1ndows.client.Zetrix;

import java.net.InetAddress;
import java.net.UnknownHostException;

@UtilityClass
public class InetAddressPatcher {
    @SuppressWarnings("UnstableApiUsage")
    public InetAddress patch(String hostName, InetAddress addr) throws UnknownHostException {
        if (InetAddresses.isInetAddress(hostName)) {
            InetAddress patched = InetAddress.getByAddress(addr.getHostAddress(), addr.getAddress());
            Zetrix.log("Patching ip-only InetAddresses from " + addr + " to " + patched);
            addr = patched;
        }
        return addr;
    }
}