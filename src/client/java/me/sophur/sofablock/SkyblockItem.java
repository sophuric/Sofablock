package me.sophur.sofablock;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

    public String getID() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    private SkyblockItem(SkyblockItem item) {
        id = item.id;
        displayName = item.displayName;
    }

    private static Map<String, SkyblockItem> ITEMS;

    public static final Codec<String> ID_CODEC = Codec.STRING.comapFlatMap(id -> {
        if (ITEMS.containsKey(id)) return DataResult.success(id);
        return DataResult.error(() -> "Invalid Skyblock item ID \"" + id + "\"");
    }, id -> id);

    public static final Codec<SkyblockItem> INGAME_CODEC = RecordCodecBuilder.create(item -> item.group(
        Codec.STRING.fieldOf("id").forGetter(o -> o.id)
    ).apply(item, (id) -> {
        //noinspection Convert2MethodRef
        return SkyblockItem.getItem(id);
    }));

    private static final Codec<SkyblockItem> REPO_CODEC = RecordCodecBuilder.create(item -> item.group(
        Codec.STRING.fieldOf("internalname").forGetter(o -> o.id.replace(':', '-')),
        Codec.STRING.fieldOf("displayname").forGetter(o -> o.displayName)
    ).apply(item, (id, displayName) -> {
        SkyblockItem i = new SkyblockItem();
        // NEU item repo uses dashes because Windows doesn't support them in file paths
        i.id = id.replace('-', ':');
        i.displayName = displayName;
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
            DataResult<SkyblockItem> itemDataResult;
            try (FileReader fileReader = new FileReader(jsonPath, StandardCharsets.UTF_8)) {
                itemDataResult = REPO_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(fileReader));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            var item = itemDataResult.getOrThrow();
            ITEMS.put(item.id, item);
        }
    }

    public static SkyblockItem getItem(String id) {
        loadItems();
        var i = ITEMS.get(id);
        if (i == null) return null;
        return new SkyblockItem(i);
    }

    public static SkyblockItem getItemByDisplayName(String displayName) {
        return ITEMS.values().stream().filter(i -> displayName.equals(i.displayName)).findFirst().orElse(null);
    }
}
