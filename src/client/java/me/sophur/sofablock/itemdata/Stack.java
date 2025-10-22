package me.sophur.sofablock.itemdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.sophur.sofablock.SkyblockItem;

import java.util.Optional;

import static me.sophur.sofablock.itemdata.Stack.ItemAmountOutOfRangeException.validateInRange;

public class Stack {
    public static final Codec<Optional<Stack>> REPO_OPTIONAL_CODEC = Codec.STRING.comapFlatMap((String s) -> {
        if (s.isEmpty()) return DataResult.success(Optional.empty());
        var i = s.lastIndexOf(':');
        // we store the ID instead, since we cannot decode the
        // recipe item before all items have been loaded
        // e.g, BLOODBADGE has a recipe that requires COVEN_SEAL,
        // but that item has not been loaded yet
        String id = i == -1 ? s : s.substring(0, i);
        int amount = 1;
        if (i != -1)
            try {
                String amountText = s.substring(i + 1);
                amount = (int) Math.ceil(Double.parseDouble(amountText));
            } catch (NumberFormatException e) {
                return DataResult.error(e::toString);
            }
        return DataResult.success(Optional.of(new Stack(id, amount)));
    }, (Optional<Stack> s) -> (s.map(stack -> stack.id + ":" + stack.amount).orElse("")));

    public static final Codec<Stack> REPO_CODEC = REPO_OPTIONAL_CODEC.flatXmap(e ->
        e.map(DataResult::success).orElse(DataResult.error(() -> "Empty string")), s -> DataResult.success(Optional.of(s)));

    public Stack(String id, int amount) {
        setAmount(amount);
        setID(id);
    }

    private String id;
    private int amount;

    public String getID() {
        return id;
    }
    
    public int getAmount() {
        return amount;
    }

    public static class ItemAmountOutOfRangeException extends RuntimeException {
        public ItemAmountOutOfRangeException(String message) {
            super(message);
        }

        public static void validateInRange(int amount) throws ItemAmountOutOfRangeException {
            if (amount <= 0)
                throw new ItemAmountOutOfRangeException("Amount " + amount + " is out of range");
        }
    }

    private void setAmount(int amount) {
        validateInRange(amount);
        this.amount = amount;
    }

    public SkyblockItem getItem() {
        return SkyblockItem.getItem(id);
    }

    private void setID(String id) {
        this.id = id;
    }
}
