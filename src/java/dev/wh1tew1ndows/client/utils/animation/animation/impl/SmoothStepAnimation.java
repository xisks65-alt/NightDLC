package dev.wh1tew1ndows.client.utils.animation.animation.impl;


import dev.wh1tew1ndows.client.utils.animation.animation.Animation;
import dev.wh1tew1ndows.client.utils.animation.animation.Direction;

public class SmoothStepAnimation extends Animation {

    public SmoothStepAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / (double) duration; //Used to force input to range from 0 to 1
        return -2 * Math.pow(x1, 3) + (3 * Math.pow(x1, 2));
    }

}
