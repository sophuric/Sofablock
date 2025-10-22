package me.sophur.sofablock;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.sophur.sofablock.tracker.Amount;
import me.sophur.sofablock.tracker.PowderType;
import net.minecraft.util.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static me.sophur.sofablock.Util.gson;
import static me.sophur.sofablock.tracker.Amount.*;

public class ItemStorage {
    public static ItemStorage INSTANCE = new ItemStorage();

    private ItemStorage() {
    }

    public final Map<PowderType, Amount> powders = new HashMap<>();

    {
        for (PowderType powderType : PowderType.values()) {
            powders.put(powderType, new Amount());
        }
    }

    private final Map<String, ItemAmount> items = new HashMap<>();

    public static final Codec<ItemStorage> CODEC = RecordCodecBuilder.create(d -> d.group(
        Codec.unboundedMap(PowderType.CODEC, Amount.CODEC).fieldOf("powders").forGetter(c -> c.powders),
        Codec.unboundedMap(SkyblockItem.ID_CODEC, ItemAmount.CODEC).fieldOf("items").forGetter(c -> c.items)
    ).apply(d, (powders, items) -> {
        var i = new ItemStorage();
        i.powders.putAll(powders);
        i.items.putAll(items);
        return i;
    }));

    public static void openConfigFile() {
        Util.getOperatingSystem().open(getPath());
    }

    private static Path getPath() {
        return SofablockClient.getModDirectory().resolve("item.json");
    }

    public static boolean load() throws IOException {
        SkyblockItem.loadItems();
        JsonElement json;
        try (FileReader fileReader = new FileReader(getPath().toFile(), StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(fileReader);
        } catch (FileNotFoundException ignored) {
            return false;
        }
        INSTANCE = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        return true;
    }

    public static void save() throws IOException {
        try (FileWriter fileWriter = new FileWriter(getPath().toFile(), StandardCharsets.UTF_8)) {
            gson.toJson(CODEC.encodeStart(JsonOps.INSTANCE, INSTANCE).getOrThrow(), fileWriter);
        }
    }

    private void checkValidItem(String itemID) throws NullPointerException {
        SkyblockItem.getItem(itemID);
    }

    public void setItemSackCount(String itemID, int count) throws NullPointerException {
        checkValidItem(itemID);
        var entry = items.getOrDefault(itemID, new ItemAmount());
        entry.current = count;
        entry.rate.updateValue();
        items.put(itemID, entry);
    }

    public int getItemSackCount(String itemID) throws NullPointerException {
        checkValidItem(itemID);
        var entry = items.getOrDefault(itemID, new ItemAmount());
        return entry.current;
    }

    public int addItemSackCount(String itemID, int count) throws NullPointerException {
        count += getItemSackCount(itemID);
        setItemSackCount(itemID, count);
        return count;
    }

    public void setItemInventoryCount(String itemID, int count) throws NullPointerException {
        checkValidItem(itemID);
        var entry = items.getOrDefault(itemID, new ItemAmount());
        entry.inventory = count;
        entry.rate.updateValue();
        items.put(itemID, entry);
    }

    public int getItemInventoryCount(String itemID) throws NullPointerException {
        checkValidItem(itemID);
        var entry = items.getOrDefault(itemID, new ItemAmount());
        return entry.inventory;
    }

    public int addItemInventoryCount(String itemID, int count) throws NullPointerException {
        count += getItemInventoryCount(itemID);
        setItemInventoryCount(itemID, count);
        return count;
    }

    public void clearItemInventoryCounts() {
        items.keySet().forEach(key -> items.get(key).inventory = 0);
    }

    public Set<String> getItemKeys() {
        return items.keySet();
    }

    public ItemAmount getItemAmount(String itemID) {
        // convert non-items to item amount
        for (PowderType powderType : PowderType.values()) {
            if (powderType.itemRepoName.equals(itemID)) {
                var powder = powders.get(powderType);
                return new ItemAmount(powder.current, 0, powder.spent, powderType.hypermax);
            }
        }
        // To-Do: *_CRYSTAL, SKYBLOCK_COIN, etc.
        var item = items.get(itemID);
        if (item != null) item = new ItemAmount(item);
        return item;
    }
}
