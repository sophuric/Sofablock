package me.sophur.sofablock;

import net.minecraft.util.math.MathHelper;

public class LoopUtil {
    private LoopUtil() {
    }

    @FunctionalInterface
    public interface LoopInPiecesCallback {
        boolean callback(float from, float to, float localFrom, float localTo, boolean first, boolean last);
    }

    public static boolean loopInPieces(float startValue, float endValue, float multiple, LoopInPiecesCallback callback) {
        if (multiple <= 0) throw new IllegalArgumentException("Multiple must be a positive number");
        if (endValue < startValue) throw new IllegalArgumentException("End value must not be greater than start value");
        if (startValue == endValue) return true;

        boolean first = true;

        float offset = MathHelper.floorMod(startValue, multiple); // initial offset
        for (float current = startValue; current < endValue; ) {
            float floor = current - offset; // rounded down to nearest multiple
            float next = floor + multiple; // next value for current
            if (next > endValue) next = endValue; // prevent from going past end

            if (!callback.callback(
                    current, next, // current value, next value in loop
                    offset, next - floor,
                    first, next >= endValue
            )) return false;

            first = false;
            offset = 0; // reset offset
            current = next;
        }

        return true;
    }
}
