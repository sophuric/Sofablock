package me.sophur.sofablock.tracker;

import me.sophur.sofablock.ItemStorage;
import me.sophur.sofablock.SofablockClient;
import me.sophur.sofablock.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.sophur.sofablock.Util.getStackByName;
import static me.sophur.sofablock.Util.matchTexts;

public class HotmGuiParser {
    private HotmGuiParser() {
    }
    
    public enum HeartType {
        HOTM("Heart of the Mountain", List.of(PowderType.MITHRIL, PowderType.GEMSTONE, PowderType.GLACITE)),
        HOTF("Heart of the Forest", List.of(PowderType.WHISPERS));

        HeartType(String name, List<PowderType> amountTypes) {
            this.name = name;
            this.amountTypes = amountTypes;
        }

        public final String name;
        public final List<PowderType> amountTypes;
    }

    public static void handleTick(MinecraftClient client) {
        if (!SofablockClient.onSkyblock()) return;
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;
        
        Text title = screen.getTitle();
        if (title == null) return;
        String titleString = title.getString();
        
        HeartType type = Arrays.stream(HeartType.values()).filter(t -> titleString.equals(t.name)).findFirst().orElse(null);
        if (type == null) return;
        
        DefaultedList<ItemStack> stacks = screen.getScreenHandler().getStacks();
        
        Optional<ItemStack> heartItem = getStackByName(stacks, type.name);
        if (heartItem.isEmpty()) return;
        Optional<ItemStack> resetItem = getStackByName(stacks, "Reset " + type.name);
        if (resetItem.isEmpty()) return;
        LoreComponent heartLore = heartItem.get().get(DataComponentTypes.LORE);
        if (heartLore == null) return;
        LoreComponent resetLore = resetItem.get().get(DataComponentTypes.LORE);
        if (resetLore == null) return;
        
        for (PowderType powderType : PowderType.values()) {
            Matcher currentPowderMatch = matchTexts(heartLore.lines(), Pattern.compile("^" + powderType.displayName + ": ([0-9,]+)$"));
            Matcher spentPowderMatch = matchTexts(resetLore.lines(), Pattern.compile("^ *- *([0-9,]+) " + powderType.displayName + "$"));
            var amount = ItemStorage.INSTANCE.powders.get(powderType);
            try {
                if (currentPowderMatch != null) amount.current = Util.parseAmount(currentPowderMatch.group(1));
                if (spentPowderMatch != null) amount.spent = Util.parseAmount(spentPowderMatch.group(1));
            } catch (NumberFormatException e) {
                SofablockClient.LOGGER.error("Failed to parse powder for {}", powderType.displayName, e);
            }
        }
    }
}
