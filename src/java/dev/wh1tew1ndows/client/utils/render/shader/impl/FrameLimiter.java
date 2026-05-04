package dev.wh1tew1ndows.client.utils.render.shader.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public class FrameLimiter {
    private long lastHookTime;
    private int accumulatedCalls;
    private final boolean useMCFrameRate;
    private int currentFps = 0;
    private long hookIntervalNS = 0;

    public FrameLimiter(boolean useMCFrameRate) {
        this.lastHookTime = Util.nanoTime();
        this.useMCFrameRate = useMCFrameRate;
        this.accumulatedCalls = 0;
    }

    public void execute(int fps, IFrameCall... calls) {
        if (currentFps != fps) {
            hookIntervalNS = 1_000_000_000L / fps;
            currentFps = fps;
        }

        long nanoTime = Util.nanoTime();
        long elapsed = nanoTime - lastHookTime;

        accumulatedCalls += (int) (elapsed / hookIntervalNS);
        lastHookTime += (accumulatedCalls * hookIntervalNS);

        accumulatedCalls = Math.min(accumulatedCalls, useMCFrameRate ? Math.min(currentFps, Minecraft.getDebugFPS()) : currentFps);

        while (accumulatedCalls > 0) {
            for (IFrameCall call : calls) {
                call.execute();
            }
            accumulatedCalls--;
        }
    }

}
