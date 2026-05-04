package dev.wh1tew1ndows.client.utils.rotation;

import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import lombok.experimental.UtilityClass;

@UtilityClass
/// sexutil
public class SensUtil implements IMinecraft {

    public float getSens(float rotation) {
        return getDeltaMouse(rotation) * getGCDValue();
    }

    public float getSensRandom(float rotation) {
        return getDeltaMouse(rotation) * getGCDValueR();
    }

    public float getGCDValue() {
        return (float) (getGCD() * 0.15D);
    }

    public float getGCDValueR() {
        return (float) (getGCDR() * 0.15D);
    }

    public float getGCDR() {
        double mouseSensitivity = mc.gameSettings.mouseSensitivity;
        return (float) (Math.pow(mouseSensitivity * 0.8F + 0.24F, 3.0D) * 7F);
    }

    public float getGCD() {
        double mouseSensitivity = mc.gameSettings.mouseSensitivity;
        return (float) (Math.pow(mouseSensitivity * 0.6F + 0.2F, 3.0D) * 8F);
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

}