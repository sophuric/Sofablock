package me.sophur.sofablock.tracker;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Amount {
    public int current, spent;

    public static <T extends Amount> Products.P2<RecordCodecBuilder.Mu<T>, Integer, Integer> fillAmountValueFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
            Codec.INT.fieldOf("current").forGetter(c -> c.current),
            Codec.INT.fieldOf("spent").forGetter(c -> c.spent)
        );
    }

    public static Codec<Amount> CODEC = RecordCodecBuilder.create(instance -> fillAmountValueFields(instance).apply(instance, Amount::new));

    public transient final RateMeasurer rate;

    public Amount() {
        this(0, 0);
    }

    public Amount(int current, int spent) {
        this.current = current;
        this.spent = spent;
        rate = new RateMeasurer(this::getTotal);
    }

    public Amount(Amount amount) {
        this(amount.current, amount.spent);
    }

    public int getTotal() {
        return current + spent;
    }

    public static class AmountWithGoal extends Amount {
        public int goal;

        public AmountWithGoal() {
            this(0, 0, 0);
        }

        public AmountWithGoal(int current, int spent, int goal) {
            super(current, spent);
            this.goal = goal;
        }

        public AmountWithGoal(AmountWithGoal amount) {
            this(amount.current, amount.spent, amount.goal);
        }

        // I hate Java
        public static <T extends AmountWithGoal> Products.P3<RecordCodecBuilder.Mu<T>, Integer, Integer, Integer> fillAmountGoalValueFields(RecordCodecBuilder.Instance<T> instance) {
            return fillAmountValueFields(instance).and(
                Codec.INT.fieldOf("goal").forGetter(c -> c.goal)
            );
        }

        public int getMissing() {
            int total = getTotal();
            if (total > goal) return 0;
            return goal - total;
        }

        public static Codec<AmountWithGoal> CODEC = RecordCodecBuilder.create(instance -> fillAmountGoalValueFields(instance).apply(instance, AmountWithGoal::new));

    }

    public static class ItemAmount extends AmountWithGoal {
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

        public ItemAmount(ItemAmount amount) {
            this(amount.current, amount.inventory, amount.spent, amount.goal);
        }

        @Override
        public int getTotal() {
            return current + inventory + spent;
        }

        public static Codec<ItemAmount> CODEC = RecordCodecBuilder.create(instance -> fillAmountGoalValueFields(instance).apply(instance, ItemAmount::new));
    }
}
