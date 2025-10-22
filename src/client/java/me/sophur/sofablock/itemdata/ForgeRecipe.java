package me.sophur.sofablock.itemdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class ForgeRecipe extends Recipe {
    private ForgeRecipe() {
        super();
    }

    public static final MapCodec<ForgeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(recipe -> recipe.group(
        Codec.INT.fieldOf("count").forGetter(o -> o.count),
        Codec.list(Stack.REPO_CODEC).fieldOf("inputs").forGetter(o -> o.inputs),
        Codec.INT.fieldOf("duration").forGetter(o -> o.duration)
    ).apply(recipe, (count, inputs, duration) -> {
        var r = new ForgeRecipe();
        r.count = count;
        r.inputs.addAll(inputs);
        r.duration = duration;
        return r;
    }));

    private int count;
    private final List<Stack> inputs = new ArrayList<>();
    private int duration;

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public List<Stack> getUnorderedInputs() {
        return inputs;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public Type<?> getType() {
        return TYPE;
    }

    public static Type<ForgeRecipe> TYPE = new Type<>(MAP_CODEC, "forge");
}
