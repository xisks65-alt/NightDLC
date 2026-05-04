package dev.wh1tew1ndows.client.managers.command.api.logger;


import dev.wh1tew1ndows.client.managers.command.api.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}
