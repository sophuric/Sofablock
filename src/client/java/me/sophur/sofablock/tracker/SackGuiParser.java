package me.sophur.sofablock.tracker;

import me.sophur.sofablock.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static me.sophur.sofablock.GemstoneUtil.GemstoneTier.*;
import static me.sophur.sofablock.GemstoneUtil.getGemstone;
import static me.sophur.sofablock.Util.*;

public class SackGuiParser {
    private static final String PAREN_NUMBER = "(?: \\(" + NUMBER_REGEX + "\\))?";
    private static final Pattern ROUGH_REGEX = Pattern.compile("^ Rough: (" + NUMBER_REGEX + ")" + PAREN_NUMBER + "$");
    private static final Pattern FLAWED_REGEX = Pattern.compile("^ Flawed: (" + NUMBER_REGEX + ")" + PAREN_NUMBER + "$");
    private static final Pattern FINE_REGEX = Pattern.compile("^ Fine: (" + NUMBER_REGEX + ")" + PAREN_NUMBER + "$");
    private static final Pattern SPECIFIC_GEMSTONE_REGEX = Pattern.compile("^ Amount: (" + NUMBER_REGEX + ")$");
    private static final Pattern OTHER_ITEM_REGEX = Pattern.compile("^Stored: (" + NUMBER_REGEX + ")(?:/[\\d,k.]+)?$");

    private SackGuiParser() {
    }

    public static void handleTick(MinecraftClient client) {
        if (!SofablockClient.onSkyblock()) return;
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;

        Text title = screen.getTitle();
        if (title == null) return;
        String titleString = title.getString();
        if (!titleString.endsWith(" Sack")) return;

        List<ItemStack> stacks = getNonPlayerStacks(screen.getScreenHandler().slots);
        Optional<ItemStack> backItem = getStackByName(stacks, "Go Back");
        if (backItem.isEmpty() || backItem.get().getItem() != Items.ARROW) return;

        for (ItemStack stack : stacks) {
            SkyblockItem item = SkyblockItem.getFromStack(stack);
            if (item == null) continue;

            LoreComponent loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
            if (loreComponent == null) continue;
            List<Text> lore = loreComponent.lines();

            try {
                var gemstoneID = getGemstone(item.getID());
                if (gemstoneID != null) {
                    var rough = matchTexts(lore, ROUGH_REGEX);
                    var flawed = matchTexts(lore, FLAWED_REGEX);
                    var fine = matchTexts(lore, FINE_REGEX);
                    if (rough != null) {
                        ItemStorage.INSTANCE.setItemSackCount(ROUGH.getID(gemstoneID), parseAmount(rough.group(1)));
                        ItemStorage.INSTANCE.setItemSackCount(FLAWED.getID(gemstoneID), parseAmount(flawed.group(1)));
                        ItemStorage.INSTANCE.setItemSackCount(FINE.getID(gemstoneID), parseAmount(fine.group(1)));
                    } else {
                        var specific = matchTexts(lore, SPECIFIC_GEMSTONE_REGEX);
                        ItemStorage.INSTANCE.setItemSackCount(item.getID(), parseAmount(specific.group(1)));
                    }
                } else {
                    var otherItem = matchTexts(lore, OTHER_ITEM_REGEX);
                    ItemStorage.INSTANCE.setItemSackCount(item.getID(), parseAmount(otherItem.group(1)));
                }
            } catch (NumberFormatException e) {
                SofablockClient.LOGGER.error("Failed to parse item amount for {}", item.getID(), e);
            }
        }
    }
}
