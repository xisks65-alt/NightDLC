//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.wh1tew1ndows.client.utils.time;

import lombok.Generated;
import lombok.Getter;

public class StopWatch {
    public long lastMS = System.currentTimeMillis();

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }

    public boolean isReached(long var1) {
        return System.currentTimeMillis() - this.lastMS > var1;
    }

    public void setLastMS(long var1) {
        this.lastMS = System.currentTimeMillis() + var1;
    }

    public void setTime(long var1) {
        this.lastMS = var1;
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public boolean isRunning() {
        return System.currentTimeMillis() - this.lastMS <= 0L;
    }

    public boolean hasTimeElapsed() {
        return this.lastMS < System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long var1, boolean var3) {
        if (System.currentTimeMillis() - this.lastMS > var1) {
            if (var3) {
                this.reset();
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean hasTimeElapsed2(long var1) {
        return System.currentTimeMillis() - this.lastMS > var1;
    }

    public boolean finished(int var1) {
        return false;
    }

    @Generated
    public long getLastMS() {
        return this.lastMS;
    }

    public static class StopWatch2 {
        @Getter
        public long lastMS = System.currentTimeMillis();

        public void reset() {
            lastMS = System.currentTimeMillis();
        }

        public boolean isReached(long time) {
            return System.currentTimeMillis() - lastMS > time;
        }

        public void setLastMS(long newValue) {
            lastMS = System.currentTimeMillis() + newValue;
        }

        public void setTime(long time) {
            lastMS = time;
        }

        public long getTime() {
            return System.currentTimeMillis() - lastMS;
        }

        public boolean isRunning() {
            return System.currentTimeMillis() - lastMS <= 0;
        }

        public boolean hasTimeElapsed() {
            return lastMS < System.currentTimeMillis();
        }
    }

}
