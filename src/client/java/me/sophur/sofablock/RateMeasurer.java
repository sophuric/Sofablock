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

    public record AmountTimestamp(long timestamp, int amount) {
        public AmountTimestamp(int amount) {
            this(getTime(), amount);
        }
    }

    private final LinkedList<AmountTimestamp> timestamps = new LinkedList<>();

    public void updateValue() {
        int newValue = getter.get();
        if (!timestamps.isEmpty() && newValue == timestamps.getLast().amount) return;
        timestamps.add(new AmountTimestamp(newValue));
        removeOldValues();
    }

    private void removeOldValues() {
        long minTime = getTime() - PERIOD;
        int removeUntil = 0;
        for (int i = 0; i < timestamps.size(); i++) {
            AmountTimestamp timestamp = timestamps.get(i);
            // store the index of the first amount that hasn't expired
            if (timestamp.timestamp >= minTime) {
                removeUntil = i;
                break;
            }
        }
        --removeUntil;
        if (removeUntil <= 0) return;
        // remove every item before that index, so keep only one expired amount,
        // so that we can get the amount from exactly 60 seconds ago
        for (int i = 0; i < removeUntil; i++) {
            timestamps.removeFirst();
        }
    }

    public int getAmountGained() {
        removeOldValues();
        if (timestamps.isEmpty()) return 0;
        return timestamps.getLast().amount - timestamps.getFirst().amount;
    }
}
