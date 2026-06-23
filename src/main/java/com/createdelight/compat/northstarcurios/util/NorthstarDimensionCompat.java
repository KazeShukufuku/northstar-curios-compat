package com.createdelight.compat.northstarcurios.util;

import com.lightning.northstar.accessor.NorthstarLevel;
import net.minecraft.world.level.Level;

public final class NorthstarDimensionCompat {

    private static final double EARTH_GRAVITY_SCALE_THRESHOLD = 0.999D;

    private NorthstarDimensionCompat() {
    }

    public static double gravityScale(Level level) {
        return ((NorthstarLevel) level).northstar$gravityScale();
    }

    public static boolean hasNormalGravity(Level level) {
        return gravityScale(level) >= EARTH_GRAVITY_SCALE_THRESHOLD;
    }
}
