package me.sophur.sofablock;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.sophur.sofablock.tracker.AmountValue;
import me.sophur.sofablock.tracker.PowderType;
import net.minecraft.util.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static me.sophur.sofablock.Util.gson;
import static me.sophur.sofablock.tracker.AmountValue.*;

public class ItemStorage {
    public static ItemStorage INSTANCE = new ItemStorage();

    private ItemStorage() {
    }

    public final Map<PowderType, AmountValue> powders = new HashMap<>();

    {
        for (PowderType powderType : PowderType.values()) {
            powders.put(powderType, new AmountValue());
        }
    }

    public final Map<String, ItemAmount> items = new HashMap<>();

    public static final Codec<ItemStorage> CODEC = RecordCodecBuilder.create(d -> d.group(
        Codec.unboundedMap(PowderType.CODEC, AmountValue.CODEC).fieldOf("powders").forGetter(c -> c.powders),
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
        try (FileReader fileReader = new FileReader(getPath().toFile(), StandardCharsets.UTF_8)) {
            SkyblockItem.loadItems();
            INSTANCE = CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(fileReader)).getOrThrow();
            return true;
        } catch (FileNotFoundException ignored) {
            return false;
        }
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
}
