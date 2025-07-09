package me.sophur.sofablock;

import net.minecraft.util.Util;

import java.util.*;
import java.util.function.Supplier;

public class RateMeasurer {
    private final Supplier<Integer> getter;

    public RateMeasurer(Supplier<Integer> getter) {
        this.getter = getter;
        updateValue();
    }

    public static final long PERIOD = 60_000_000_000L;

    private static long getTime() {
        return Util.getMeasuringTimeNano();
    }

    public record PowderTimestamp(long timestamp, int powder) {
        public PowderTimestamp(int powder) {
            this(getTime(), powder);
        }
    }

    private final LinkedList<PowderTimestamp> timestamps = new LinkedList<>();

    public void updateValue() {
        int newValue = getter.get();
        if (!timestamps.isEmpty() && newValue == timestamps.getLast().powder) return;
        timestamps.add(new PowderTimestamp(newValue));
        removeOldValues();
    }

    private void removeOldValues() {
        long minTime = getTime() - PERIOD;
        int removeUntil = 0;
        for (int i = 0; i < timestamps.size(); i++) {
            PowderTimestamp timestamp = timestamps.get(i);
            // store the index of the first powder amount that hasn't expired
            if (timestamp.timestamp >= minTime) {
                removeUntil = i;
                break;
            }
        }
        --removeUntil;
        if (removeUntil <= 0) return;
        // remove every item before that index, so keep only one expired amount,
        // so that we can get the powder amount from exactly 60 seconds ago
        for (int i = 0; i < removeUntil; i++) {
            timestamps.removeFirst();
        }
    }

    public int getAmountGained() {
        removeOldValues();
        if (timestamps.isEmpty()) return 0;
        return timestamps.getLast().powder - timestamps.getFirst().powder;
    }
}
