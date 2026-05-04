package net.optifine.util;

import lombok.Getter;
import dev.wh1tew1ndows.common.impl.fastrandom.FastRandom;

import java.util.Random;

public class RandomUtils {
    @Getter
    private static final Random random = new FastRandom();

    public static int getRandomInt(int bound) {
        return random.nextInt(bound);
    }
}
