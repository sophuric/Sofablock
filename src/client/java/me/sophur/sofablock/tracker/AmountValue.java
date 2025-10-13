package me.sophur.sofablock.tracker;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AmountValue {
    public int current, spent;

    public static <T extends AmountValue> Products.P2<RecordCodecBuilder.Mu<T>, Integer, Integer> fillAmountValueFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
            Codec.INT.fieldOf("current").forGetter(c -> c.current),
            Codec.INT.fieldOf("spent").forGetter(c -> c.spent)
        );
    }

    public static Codec<AmountValue> CODEC = RecordCodecBuilder.create(instance -> fillAmountValueFields(instance).apply(instance, AmountValue::new));

    public transient final RateMeasurer rate;

    public AmountValue() {
        this(0, 0);
    }

    public AmountValue(int current, int spent) {
        this.current = current;
        this.spent = spent;
        rate = new RateMeasurer(this::getTotal);
    }

    public int getTotal() {
        return current + spent;
    }

    public static class AmountGoalValue extends AmountValue {
        public int goal;

        public AmountGoalValue() {
            this(0, 0, 0);
        }

        public AmountGoalValue(int current, int spent, int goal) {
            super(current, spent);
            this.goal = goal;
        }

        // I hate Java
        public static <T extends AmountGoalValue> Products.P3<RecordCodecBuilder.Mu<T>, Integer, Integer, Integer> fillAmountGoalValueFields(RecordCodecBuilder.Instance<T> instance) {
            return fillAmountValueFields(instance).and(
                Codec.INT.fieldOf("goal").forGetter(c -> c.goal)
            );
        }

        public static Codec<AmountGoalValue> CODEC = RecordCodecBuilder.create(instance -> fillAmountGoalValueFields(instance).apply(instance, AmountGoalValue::new));

    }

    public static class ItemAmount extends AmountGoalValue {
        public int inventory;

        public ItemAmount() {
            this(0, 0, 0, 0);
        }

        private ItemAmount(int current, int spent, int goal) {
            super(current, spent, goal);
        }

        public ItemAmount(int current, int inventory, int spent, int goal) {
            this(current, spent, goal);
            this.inventory = inventory;
        }
        
        @Override
        public int getTotal() {
            return current + inventory + spent;
        }

        public static Codec<ItemAmount> CODEC = RecordCodecBuilder.create(instance -> fillAmountGoalValueFields(instance).apply(instance, ItemAmount::new));
    }
}
