package me.sophur.sofablock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import static me.sophur.sofablock.Util.getSlotByName;
import static me.sophur.sofablock.Util.matchTexts;

public class HOTMParser {
    private HOTMParser() {
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
        if (client.currentScreen instanceof HandledScreen<?> screen) {
            Text title = screen.getTitle();
            if (title == null) return;
            String titleString = title.getString();
            ScreenHandler handler = screen.getScreenHandler();
            HeartType type = Arrays.stream(HeartType.values()).filter(t -> titleString.equals(t.name)).findFirst().orElse(null);
            if (type == null) return;
            Optional<Slot> heartItem = getSlotByName(handler.slots, type.name);
            if (heartItem.isEmpty()) return;
            Optional<Slot> resetItem = getSlotByName(handler.slots, "Reset " + type.name);
            if (resetItem.isEmpty()) return;
            LoreComponent heartLore = heartItem.get().getStack().get(DataComponentTypes.LORE);
            if (heartLore == null) return;
            LoreComponent resetLore = resetItem.get().getStack().get(DataComponentTypes.LORE);
            if (resetLore == null) return;
            for (PowderType powderType : PowderType.values()) {
                Matcher currentPowderMatch = matchTexts(heartLore.lines(), "^" + powderType.displayName + ": ([0-9,]+)$");
                Matcher spentPowderMatch = matchTexts(resetLore.lines(), "^ *- *([0-9,]+) " + powderType.displayName + "$");
                var amount = Config.INSTANCE.powders.get(powderType);
                try {
                    if (currentPowderMatch != null) amount.current = Util.parseAmount(currentPowderMatch.group(1));
                } catch (NumberFormatException e) {
                    SofablockClient.LOGGER.error("Failed to parse current powder for {}", powderType.displayName, e);
                }
                try {
                    if (spentPowderMatch != null) amount.spent = Util.parseAmount(spentPowderMatch.group(1));
                } catch (NumberFormatException e) {
                    SofablockClient.LOGGER.error("Failed to parse spent powder for {}", powderType.displayName, e);
                }
            }
        }
    }
}
