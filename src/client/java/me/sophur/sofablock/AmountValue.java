package me.sophur.sofablock;

public class AmountValue {
    public int current, spent;

    public transient final RateMeasurer rate;

    public AmountValue() {
        current = 0;
        spent = 0;
        rate = new RateMeasurer(this::getTotal);
    }

    public int getTotal() {
        return current + spent;
    }
    
    public static class AmountGoalValue extends AmountValue {
        public int goal;
        
        public AmountGoalValue() {
            super();
            goal = 0;
        }
    }
}
