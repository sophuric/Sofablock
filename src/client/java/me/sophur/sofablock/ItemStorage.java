package me.sophur.sofablock;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.sophur.sofablock.tracker.Amount;
import me.sophur.sofablock.tracker.PowderType;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static me.sophur.sofablock.SkyblockItem.assertValidItem;
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

    private final Set<String> openedItems = new HashSet<>();

    private static final Codec<ItemStorage> CODEC = RecordCodecBuilder.create(d -> d.group(
        Codec.unboundedMap(PowderType.CODEC, Amount.CODEC)
            .optionalFieldOf("powders", Map.of()).forGetter(c -> c.powders),
        Codec.unboundedMap(SkyblockItem.ID_CODEC, ItemAmount.CODEC)
            .optionalFieldOf("items", Map.of()).forGetter(c -> c.items),
        Codec.list(Codec.STRING)
            .optionalFieldOf("opened", List.of()).forGetter(c -> c.openedItems.stream().toList())
    ).apply(d, (powders, items, expandedItems) -> {
        var i = new ItemStorage();
        i.powders.putAll(powders);
        i.items.putAll(items);
        i.openedItems.addAll(expandedItems);
        return i;
    }));

    private static Path getPath() {
        return SofablockClient.getModDirectory().resolve("item.json");
    }

    public static boolean load() throws IOException {
        SkyblockItem.loadItems();
        JsonElement json = Util.readJson(getPath().toFile());
        if (json == null) return false;
        INSTANCE = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        return true;
    }

    public static void save() throws IOException {
        var json = CODEC.encodeStart(JsonOps.INSTANCE, INSTANCE).getOrThrow();
        Util.writeJson(getPath().toFile(), json);
    }

    public void clearItemInventoryCounts() {
        items.keySet().forEach(key -> items.get(key).inventory = 0);
    }

    public List<String> getItemKeys() {
        return items.keySet().stream().toList();
    }

    public ItemAmount getItemAmount(String itemID) {
        // convert non-items to item amount
        for (PowderType powderType : PowderType.values()) {
            if (powderType.itemRepoName.equals(itemID)) {
                var powder = powders.get(powderType);
                return new ItemAmount(powder.current, powder.spent, powderType.hypermax, 0, 0);
            }
        }
        assertValidItem(itemID);
        // TODO: *_CRYSTAL, SKYBLOCK_COIN, etc.
        var item = items.computeIfAbsent(itemID, k -> new ItemAmount());
        items.put(itemID, item);
        return item;
    }

    public boolean getItemOpened(List<String> itemID) {
        if (itemID.isEmpty()) throw new IllegalArgumentException("Item ID list is empty");
        itemID.forEach(SkyblockItem::assertValidItem);
        String s = String.join(".", itemID);
        return openedItems.contains(s);
    }

    public void setItemOpened(List<String> itemID, boolean isOpen) {
        if (itemID.isEmpty()) throw new IllegalArgumentException("Item ID list is empty");
        itemID.forEach(SkyblockItem::assertValidItem);
        String s = String.join(".", itemID);
        if (isOpen)
            openedItems.add(s);
        else
            openedItems.remove(s);
    }
}
