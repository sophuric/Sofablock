package me.sophur.sofablock.tracker;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Amount {
    public int current, spent;

    public int addCurrent(int delta) {
        return current += delta;
    }

    public int addSpent(int delta) {
        return spent += delta;
    }

    public static <T extends Amount> Products.P2<RecordCodecBuilder.Mu<T>, Integer, Integer> fillAmountValueFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
            Codec.INT.optionalFieldOf("current", 0).forGetter(c -> c.current),
            Codec.INT.optionalFieldOf("spent", 0).forGetter(c -> c.spent)
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
                Codec.INT.optionalFieldOf("goal", 0).forGetter(c -> c.goal)
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
        public int inventory, temp;

        public int addInventory(int delta) {
            return inventory += delta;
        }

        public int addTemp(int delta) {
            return temp += delta;
        }

        public ItemAmount() {
            this(0, 0, 0, 0, 0);
        }

        public ItemAmount(int current, int spent, int goal, int inventory, int temp) {
            super(current, spent, goal);
            this.inventory = inventory;
            this.temp = temp;
        }

        public ItemAmount(ItemAmount amount) {
            this(amount.current, amount.spent, amount.goal, amount.inventory, amount.temp);
        }

        @Override
        public int getTotal() {
            return current + inventory + spent + temp;
        }

        public static <T extends ItemAmount> Products.P5<RecordCodecBuilder.Mu<T>, Integer, Integer, Integer, Integer, Integer> fillItemAmountFields(RecordCodecBuilder.Instance<T> instance) {
            return fillAmountGoalValueFields(instance).and(
                Codec.INT.optionalFieldOf("inventory", 0).forGetter(c -> c.inventory)
            ).and(
                Codec.INT.optionalFieldOf("temp", 0).forGetter(c -> c.temp)
            );
        }

        public static Codec<ItemAmount> CODEC = RecordCodecBuilder.create(instance -> fillItemAmountFields(instance).apply(instance, ItemAmount::new));
    }
}
