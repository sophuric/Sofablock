package me.sophur.sofablock.hud;

import me.sophur.sofablock.ItemStorage;
import me.sophur.sofablock.SkyblockItem;
import me.sophur.sofablock.tracker.AmountValue;
import me.sophur.sofablock.tracker.PowderType;
import me.sophur.sofablock.tracker.RateMeasurer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static me.sophur.sofablock.SofablockHud.*;
import static me.sophur.sofablock.Util.*;
import static me.sophur.sofablock.tracker.AmountValue.*;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

public class AmountDisplay implements TextDisplay {
    public List<Pair<Text, List<Text>>> GetTextLines() {
        ArrayList<Pair<Text, List<Text>>> textLines = new ArrayList<>();

        for (PowderType powderType : PowderType.values()) {
            AmountValue amount = ItemStorage.INSTANCE.powders.get(powderType);

            Text text = getAmountText(powderType.displayName, powderType.color, powderType.areas, amount.getTotal(), powderType.hypermax, amount.rate);
            if (text == null) continue;
            List<Text> hoverText = getPowderHoverText(powderType, amount);
            textLines.add(new Pair<>(text, hoverText));
        }

        for (String itemID : ItemStorage.INSTANCE.items.keySet()) {
            SkyblockItem item = SkyblockItem.getItem(itemID);
            
            if (item == null) continue;

            AmountGoalValue amount = ItemStorage.INSTANCE.items.get(itemID);

            Text text = getAmountText(item.getDisplayName(), Formatting.RESET, null, amount.getTotal(), amount.goal, amount.rate);
            if (text == null) continue;
            List<Text> hoverText = getItemHoverText(item.getDisplayName(), amount);
            textLines.add(new Pair<>(text, hoverText));
        }

        return textLines;
    }

    @Override
    public int GetX(int width) {
        return 16;
    }

    @Override
    public int GetY(int height) {
        return 16;
    }


    private static MutableText appendMaxText(MutableText text, int total, int max, Formatting formatting) {
        text.append(literal(decimalFormat.format(max), formatting));
        if (total < max)
            text.append(literal(" (", GRAY)
                .append(literal(Long.toString((long) total * 100 / max), formatting))
                .append(literal("%)", GRAY)));
        return text;
    }

    private static Text getAmountText(String name, Formatting color, List<String> areas, int total, int hypermax, RateMeasurer rate) {
        if (!inArea(areas)) return null;

        if (total >= hypermax) return null;

        var text = Text.empty().append(literal(name, color))
            .append(literal(": ", GRAY))
            .append(literal(decimalFormat.format(total), WHITE));
        appendMaxText(text.append(Text.literal(" / ").formatted(GRAY)), total, hypermax, GRAY);
        if (rate != null) {
            int gained = rate.getAmountGained();
            if (gained > 0)
                text.append(literal(" - " + gained + "/min", GRAY));
        }

        return text;
    }

    private static List<Text> getGenericHoverText(String displayName, Formatting color, AmountValue amount) {
        var texts = new ArrayList<Text>();
        texts.add(literal(displayName, color));
        texts.add(literal("Current: ", GRAY)
            .append(literal(decimalFormat.format(amount.current), WHITE)));
        texts.add(literal("Spent: ", GRAY)
            .append(literal(decimalFormat.format(amount.spent), WHITE)));
        texts.add(literal("Total: ", GRAY)
            .append(literal(decimalFormat.format(amount.getTotal()), WHITE)));

        return texts;
    }

    private static List<Text> getPowderHoverText(PowderType type, AmountValue amount) {
        List<Text> texts = getGenericHoverText(type.displayName, type.color, amount);

        var total = amount.getTotal();
        texts.add(appendMaxText(literal("Max for Skyblock XP: ", GRAY), total, type.xpMax, WHITE));
        texts.add(appendMaxText(literal("Hypermax: ", GRAY), total, type.hypermax, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(amount.rate.getAmountGained()), WHITE)));

        return texts;
    }

    private static List<Text> getItemHoverText(String name, AmountGoalValue amount) {
        List<Text> texts = getGenericHoverText(name, Formatting.RESET, amount);

        var total = amount.getTotal();
        texts.add(appendMaxText(literal("Progress: ", GRAY), total, amount.goal, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(amount.rate.getAmountGained()), WHITE)));

        return texts;
    }
}
