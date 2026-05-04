package dev.wh1tew1ndows.client.utils.math;

import dev.wh1tew1ndows.lib.util.time.StopWatch;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class PerfectDelay {
    private long delay = 0;
    private final StopWatch time = new StopWatch();

    public boolean cooldownComplete() {
        return cooldownComplete(this.delay);
    }

    public boolean cooldownComplete(long delay) {
        return time.finished(delay);
    }

    public void reset(long delay) {
        this.time.reset();
        this.delay = delay;
    }

}