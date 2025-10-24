package me.sophur.sofablock;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.sophur.sofablock.itemdata.CraftingRecipe;
import me.sophur.sofablock.itemdata.Recipe;
import me.sophur.sofablock.itemdata.RecipeCodec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.InvalidIdentifierException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static me.sophur.sofablock.Util.getJsonFilesInDir;

public class SkyblockItem {
    public static SkyblockItem getFromStack(ItemStack stack) {
        NbtComponent nbt = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return null;

        DataResult<SkyblockItem> result = SkyblockItem.INGAME_CODEC.parse(NbtOps.INSTANCE, nbt.copyNbt());
        if (result.isSuccess()) return result.getOrThrow();
        return null;
    }

    private String id;
    private String displayName;

    private final List<Recipe> recipes = new ArrayList<>();

    public String getID() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    private SkyblockItem(SkyblockItem item) {
        id = item.id;
        displayName = item.displayName;
        recipes.addAll(item.recipes);
    }

    public static void validateItemID(String id) throws InvalidIdentifierException {
        if (!ITEMS.containsKey(id)) throw new InvalidIdentifierException("Invalid Skyblock item ID \"" + id + "\"");
    }

    private static Map<String, SkyblockItem> ITEMS;

    public static final Codec<String> ID_CODEC = Codec.STRING.comapFlatMap(id -> {
        if (ITEMS.containsKey(id)) return DataResult.success(id);
        return DataResult.error(() -> "Invalid Skyblock item ID \"" + id + "\"");
    }, id -> id);

    public static final Codec<SkyblockItem> INGAME_CODEC = RecordCodecBuilder.create(item -> item.group(
        Codec.STRING.fieldOf("id").forGetter(o -> o.id)
    ).apply(item, (id) -> {
        // may use other fields for ingame items in the future
        //noinspection Convert2MethodRef
        return SkyblockItem.getItem(id);
    }));

    private static final Codec<SkyblockItem> REPO_CODEC = RecordCodecBuilder.create(item -> item.group(
        Codec.STRING.fieldOf("internalname").forGetter(o -> o.id.replace(':', '-')),
        Codec.STRING.fieldOf("displayname").forGetter(o -> o.displayName),
        Codec.list(RecipeCodec.REPO_CODEC.codec()).optionalFieldOf("recipes").forGetter(o -> Optional.of(o.recipes)),
        // why on Earth does NEU item repo do this?
        CraftingRecipe.MAP_CODEC.codec().optionalFieldOf("recipe")
            .forGetter(o -> {
                for (Recipe recipe : o.recipes)
                    if (recipe instanceof CraftingRecipe c)
                        return Optional.of(c);
                return Optional.empty();
            })
    ).apply(item, (id, displayName, optRecipes, optCraftingRecipe) -> {
        SkyblockItem i = new SkyblockItem();
        // NEU item repo uses dashes because Windows doesn't support them in file paths
        i.id = id.replace('-', ':');
        i.displayName = displayName;
        optCraftingRecipe.ifPresent(i.recipes::add);
        optRecipes.ifPresent(i.recipes::addAll);
        return i;
    }));

    private SkyblockItem() {
    }

    private static Path getRepoDirectory() throws RuntimeException {
        return FabricLoader.getInstance().getConfigDir().resolve("skyblocker").resolve("item-repo");
    }

    public static void loadItems() throws UncheckedIOException {
        if (ITEMS != null) return;
        var jsonFiles = getJsonFilesInDir(getRepoDirectory().resolve("items"));
        ITEMS = new HashMap<>();
        for (File jsonPath : jsonFiles) {
            JsonElement json;
            try (FileReader fileReader = new FileReader(jsonPath, StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(fileReader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            SkyblockItem item;
            var dataResult = REPO_CODEC.parse(JsonOps.INSTANCE, json);
            item = dataResult.getOrThrow();
            if (ITEMS.containsKey(item.id)) throw new RuntimeException("Item " + item.id + " already exists");
            ITEMS.put(item.id, item);
        }
    }

    public static SkyblockItem getItem(String id) {
        loadItems();
        var i = ITEMS.get(id);
        if (i == null) return null;
        return new SkyblockItem(i);
    }

    public static void assertValidItem(String itemID) throws AssertionError {
        assert SkyblockItem.getItem(itemID) != null;
    }

    public static SkyblockItem getItemByDisplayName(String displayName) {
        return ITEMS.values().stream().filter(i ->
            displayName.equals(i.displayName) || displayName.equals(Formatting.strip(i.displayName))
        ).findFirst().orElse(null);
    }

}
