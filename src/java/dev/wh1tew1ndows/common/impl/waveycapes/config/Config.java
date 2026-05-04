package dev.wh1tew1ndows.common.impl.waveycapes.config;


import dev.wh1tew1ndows.common.impl.waveycapes.enums.CapeMovement;
import dev.wh1tew1ndows.common.impl.waveycapes.enums.CapeStyle;
import dev.wh1tew1ndows.common.impl.waveycapes.enums.WindMode;

public class Config {
    public WindMode windMode = WindMode.WAVES;
    public CapeStyle capeStyle = CapeStyle.SMOOTH;
    public CapeMovement capeMovement = CapeMovement.BASIC_SIMULATION;
    public int gravity = 25;
    public int heightMul = 5;
    public int straveMul = 5;
}
