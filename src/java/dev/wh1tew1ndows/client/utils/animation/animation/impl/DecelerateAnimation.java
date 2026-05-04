package dev.wh1tew1ndows.client.utils.animation.animation.impl;

import dev.wh1tew1ndows.client.utils.animation.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.animation.Direction;

public class DecelerateAnimation extends Animation {

    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / duration;
        return 1 - ((x1 - 1) * (x1 - 1));
    }
}
