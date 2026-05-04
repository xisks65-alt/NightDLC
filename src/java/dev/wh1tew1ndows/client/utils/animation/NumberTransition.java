package dev.wh1tew1ndows.client.utils.animation;

public class NumberTransition {
    public static float result(float start, float end) {
        return AnimationMath.fast(start, end, 4);
    }

    public static float resultspeed(float start, float end, float v) {
        return AnimationMath.fast(start, end, 10);
    }

    public static float resultspeed(float start, float end) {
        return AnimationMath.fast(start, end, 10);
    }
}
