package me.sophur.sofablock.itemdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

public class RecipeCodec {
    private RecipeCodec() {
    }

    public static final MapCodec<Recipe> REPO_CODEC;

    private static Recipe.Type<Recipe> UNKNOWN_TYPE = null;

    static {
        UNKNOWN_TYPE = new Recipe.Type<>(MapCodec.unit(() -> new Recipe() {
            @Override
            public Type<?> getType() {
                return UNKNOWN_TYPE;
            }

            @Override
            public int getCount() {
                return 0;
            }

            @Override
            protected List<Stack> getUnorderedInputs() {
                return List.of();
            }
        }), "unknown");
    }

    static {
        /// see {@link TextCodecs#createCodec(Codec)}
        Recipe.Type<?>[] recipeTypes = new Recipe.Type<?>[]{CraftingRecipe.TYPE, ForgeRecipe.TYPE};
        REPO_CODEC =
            StringIdentifiable.createBasicCodec(() -> recipeTypes)
                .orElseGet(() -> UNKNOWN_TYPE) // fallback for recipes we have not defined yet
                .dispatchMap("type", Recipe::getType, Recipe.Type::codec);

    }
}
