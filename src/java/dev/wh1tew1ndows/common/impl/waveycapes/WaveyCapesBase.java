package dev.wh1tew1ndows.common.impl.waveycapes;

import dev.wh1tew1ndows.common.impl.waveycapes.config.Config;

public class WaveyCapesBase {
    public static WaveyCapesBase INSTANCE;
    public static Config config;

    public void init() {
        INSTANCE = this;
        config = new Config();
    }
}
