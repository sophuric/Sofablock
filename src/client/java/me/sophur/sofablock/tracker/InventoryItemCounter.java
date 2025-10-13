package me.sophur.sofablock.tracker;

import me.sophur.sofablock.ItemStorage;
import me.sophur.sofablock.SkyblockItem;
import me.sophur.sofablock.SofablockClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class InventoryItemCounter {
    private InventoryItemCounter() {
    }

    public static void handleTick(MinecraftClient client) {
        if (!SofablockClient.onSkyblock()) return;

        if (client.player == null) return;

        ItemStorage.INSTANCE.clearItemInventoryCounts();

        for (ItemStack stack : client.player.getInventory()) {
            SkyblockItem item = SkyblockItem.getFromStack(stack);
            if (item == null) continue;

            ItemStorage.INSTANCE.addItemInventoryCount(item.getID(), stack.getCount());
        }
    }
}
