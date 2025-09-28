package me.sophur.sofablock;

import me.sophur.sofablock.AmountValue.AmountGoalValue;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

public class Util {
    private Util() {
    }

    public static Optional<Slot> getSlotByName(AbstractList<Slot> slots, String name) {
        return slots.stream().filter(slot -> slot.getStack().getName().getString().equals(name)).findFirst();
    }

    public static Matcher matchTexts(List<Text> texts, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        return texts.stream().map(text -> regex.matcher(text.getString())).filter(Matcher::find).findFirst().orElse(null);
    }

    public static int parseAmount(String string) throws NumberFormatException {
        return Integer.parseInt(string.replaceAll(",", ""));
    }

    public static MutableText literal(String string, Formatting formatting) {
        return Text.literal(string).formatted(formatting);
    }

    private static final DecimalFormat decimalFormat = new DecimalFormat();

    static {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    private static MutableText appendMaxText(MutableText text, int total, int max, Formatting formatting) {
        text.append(literal(decimalFormat.format(max), formatting));
        if (total < max)
            text.append(literal(" (", GRAY)
                .append(literal(Long.toString((long) total * 100 / max), formatting))
                .append(literal("%)", GRAY)));
        return text;
    }

    public static boolean inArea(List<String> areas) {
        return SofablockClient.onSkyblock() && areas.contains(SofablockClient.getMode());
    }

    public static Text getAmountText(String name, Formatting color, List<String> areas, int total, int hypermax, RateMeasurer rate) {
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

    public static List<Text> getPowderHoverText(PowderType type, AmountValue amount) {
        if (!inArea(type.areas)) return null;

        var texts = new ArrayList<Text>();
        texts.add(literal(type.displayName, type.color));
        texts.add(literal("Current: ", GRAY)
            .append(literal(decimalFormat.format(amount.current), WHITE)));
        texts.add(literal("Spent: ", GRAY)
            .append(literal(decimalFormat.format(amount.spent), WHITE)));
        var total = amount.getTotal();
        texts.add(literal("Total: ", GRAY)
            .append(literal(decimalFormat.format(total), WHITE)));
        texts.add(appendMaxText(literal("Max for Skyblock XP: ", GRAY), total, type.xpMax, WHITE));
        texts.add(appendMaxText(literal("Hypermax: ", GRAY), total, type.hypermax, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(amount.rate.getAmountGained()), WHITE)));

        return texts;
    }

    public static List<Text> getItemHoverText(ItemType type, AmountGoalValue amount) {
        if (!inArea(type.areas)) return null;

        var texts = new ArrayList<Text>();
        texts.add(literal(type.displayName, type.color));
        texts.add(literal("Current: ", GRAY)
            .append(literal(decimalFormat.format(amount.current), WHITE)));
        texts.add(literal("Spent: ", GRAY)
            .append(literal(decimalFormat.format(amount.spent), WHITE)));
        var total = amount.getTotal();
        texts.add(literal("Total: ", GRAY)
            .append(literal(decimalFormat.format(total), WHITE)));
        texts.add(appendMaxText(literal("Progress: ", GRAY), total, amount.goal, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(amount.rate.getAmountGained()), WHITE)));

        return texts;
    }
}