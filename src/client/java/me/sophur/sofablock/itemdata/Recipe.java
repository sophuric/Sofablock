package me.sophur.sofablock.itemdata;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringIdentifiable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Recipe {
    /// see {@link net.minecraft.text.TextContent#getType}
    public abstract Type<?> getType();

    public abstract int getCount();

    protected abstract List<Stack> getUnorderedInputs();

    public final List<Stack> getCommonInputs() {
        // merge stacks with same item type into the same stacks
        Map<String, Integer> amounts = new HashMap<>();
        for (Stack input : getUnorderedInputs()) {
            var id = input.getID();
            var amount = amounts.getOrDefault(id, 0);
            amount += input.getAmount();
            amounts.put(id, amount);
        }
        return amounts.keySet().stream().map(e -> new Stack(e, amounts.get(e))).toList();
    }

    public record Type<T extends Recipe>(MapCodec<T> codec, String id) implements StringIdentifiable {
        public String asString() {
            return this.id;
        }
    }
}
