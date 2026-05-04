package dev.wh1tew1ndows.common.impl.globals;

import by.radioegor146.nativeobfuscator.NotNative;
import dev.wh1tew1ndows.client.api.client.Constants;
import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author ConeTin
 * @since 13 июн. 2024 г.
 */


@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerAPI implements IMinecraft {

    Socket socket;
    OutputStream output;
    InputStream input;
    PrintWriter writer;
    BufferedReader reader;

    public void init() {
        try {
            socket = new Socket("109.107.181.89", 25565);
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            writer = new PrintWriter(output, true);
            reader = new BufferedReader(new InputStreamReader(input));

            writer.println(mc.getSession().getUsername() + "/" + Constants.NAME.toLowerCase());

        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
    }

    @NotNative
    public void finish() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }

    @NotNative
    public void updateName() {
        if (writer != null)
            writer.println(mc.getSession().getUsername() + "/" + Constants.NAME.toLowerCase());
    }

    @NotNative
    public String getClients() {
        writer.println("/getClients");
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

}
