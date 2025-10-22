package me.sophur.sofablock.itemdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.sophur.sofablock.itemdata.Stack.ItemAmountOutOfRangeException.validateInRange;

public class CraftingRecipe extends Recipe {
    private CraftingRecipe() {
        super();
    }

    private final static int INPUT_LENGTH = 9;

    private static RecordCodecBuilder<CraftingRecipe, Optional<Stack>> getIngredient(String field, int index) {
        return Stack.REPO_OPTIONAL_CODEC.fieldOf(field).forGetter((CraftingRecipe o) -> o.inputs.get(index));
    }

    public static final MapCodec<CraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(recipe -> recipe.group(
        Codec.INT.optionalFieldOf("count", 1).forGetter(o -> o.count),
        // there must be a better way to do this
        getIngredient("A1", 0),
        getIngredient("A2", 1),
        getIngredient("A3", 2),
        getIngredient("B1", 3),
        getIngredient("B2", 4),
        getIngredient("B3", 5),
        getIngredient("C1", 6),
        getIngredient("C2", 7),
        getIngredient("C3", 8)
    ).apply(recipe, (count, a1, a2, a3, b1, b2, b3, c1, c2, c3) -> {
        var r = new CraftingRecipe();
        r.count = count;
        validateInRange(r.count);
        r.inputs.addAll(List.of(a1, a2, a3, b1, b2, b3, c1, c2, c3));
        return r;
    }));

    private int count;
    private final List<Optional<Stack>> inputs = new ArrayList<>();

    public int getCount() {
        return count;
    }

    @Override
    public List<Stack> getUnorderedInputs() {
        return getInputs().stream().filter(Optional::isPresent).map(Optional::get).toList();
    }

    public List<Optional<Stack>> getInputs() {
        return inputs;
    }

    @Override
    public Type<?> getType() {
        return TYPE;
    }

    public static Type<CraftingRecipe> TYPE = new Type<>(MAP_CODEC, "crafting");
}
