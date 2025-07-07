package me.sophur.sofablock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class HOTMParser {
    private HOTMParser() {
    }

    public enum HeartType {
        HOTM("Heart of the Mountain", Stream.of("mithril", "gemstone", "glacite").map(PowderAmount::getPowder).toList()),
        HOTF("Heart of the Forest", List.of(PowderAmount.getPowder("forest")));

        HeartType(String name, List<PowderAmount> powders) {
            this.name = name;
            this.powders = powders;
        }

        public final String name;
        public final List<PowderAmount> powders;
    }

    private static Optional<Slot> getSlotByName(AbstractList<Slot> slots, String name) {
        return slots.stream().filter(slot -> slot.getStack().getName().getString().equals(name)).findFirst();
    }

    private static Matcher matchTexts(List<Text> texts, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        return texts.stream().map(text -> regex.matcher(text.getString())).filter(Matcher::find).findFirst().orElse(null);
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
            for (PowderAmount powder : type.powders) {
                powder.current().set(0);
                powder.spent().set(0);
                Matcher currentPowderMatch = matchTexts(heartLore.lines(), "^" + powder.name() + ": ([0-9,]+)$");
                Matcher spentPowderMatch = matchTexts(resetLore.lines(), "^ *- *([0-9,]+) " + powder.name() + "$");
                if (currentPowderMatch != null) {
                    try {
                        powder.current().set(Integer.parseInt(currentPowderMatch.group(1).replaceAll(",", "")));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (spentPowderMatch != null) {
                    try {
                        powder.spent().set(Integer.parseInt(spentPowderMatch.group(1).replaceAll(",", "")));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }
}
