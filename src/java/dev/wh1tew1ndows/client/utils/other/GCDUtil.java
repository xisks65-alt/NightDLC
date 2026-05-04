package dev.wh1tew1ndows.client.utils.other;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;

import static java.lang.Math.round;

public class GCDUtil implements IMinecraft {
    public static float getSensitivity(float rot) {
        return getDeltaMouse(rot) * getGCDValue();
    }

    public static float getGCDValue() {
        return (float) (getGCD() * 0.15);
    }

    public static float getGCD() {
        float f1;
        return (f1 = (float) (mc.gameSettings.mouseSensitivity * 0.6 + 0.2)) * f1 * f1 * 8;
    }

    public static float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

}