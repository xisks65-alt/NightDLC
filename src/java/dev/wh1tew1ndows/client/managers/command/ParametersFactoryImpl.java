package dev.wh1tew1ndows.client.managers.command;

import dev.wh1tew1ndows.client.managers.command.api.Parameters;
import dev.wh1tew1ndows.client.managers.command.api.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
