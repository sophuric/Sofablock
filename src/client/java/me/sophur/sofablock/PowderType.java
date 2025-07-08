package me.sophur.sofablock;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.sophur.sofablock.Config.INSTANCE;
import static me.sophur.sofablock.Util.*;
import static net.minecraft.util.Formatting.*;

public enum PowderType {
    MITHRIL("Mithril", "Mithril Powder", List.of("mining_3", "crystal_hollows"/*, "mineshaft"*/), DARK_GREEN, 12_500_000, 12_658_220,
        createHolder(() -> INSTANCE.currentMithrilPowder, v -> INSTANCE.currentMithrilPowder = v),
        createHolder(() -> INSTANCE.spentMithrilPowder, v -> INSTANCE.spentMithrilPowder = v)
    ),
    GEMSTONE("Gemstone", "Gemstone Powder", MITHRIL.areas, LIGHT_PURPLE, 20_000_000, 31_150_666,
        createHolder(() -> INSTANCE.currentGemstonePowder, v -> INSTANCE.currentGemstonePowder = v),
        createHolder(() -> INSTANCE.spentGemstonePowder, v -> INSTANCE.spentGemstonePowder = v)
    ),
    GLACITE("Glacite", "Glacite Powder", MITHRIL.areas, AQUA, 20_000_000, 34_479_533,
        createHolder(() -> INSTANCE.currentGlacitePowder, v -> INSTANCE.currentGlacitePowder = v),
        createHolder(() -> INSTANCE.spentGlacitePowder, v -> INSTANCE.spentGlacitePowder = v)
    ),
    WHISPERS("Whispers", "Forest Whispers", List.of("foraging_1", "foraging_2"), DARK_AQUA, 1, 1,
        createHolder(() -> INSTANCE.currentForestWhispers, v -> INSTANCE.currentForestWhispers = v),
        createHolder(() -> INSTANCE.spentForestWhispers, v -> INSTANCE.spentForestWhispers = v)
    );

    public final String tabName;
    public final String displayName;
    private final List<String> areas;
    public final Formatting color;
    public final int xpMax;
    public final int hypermax;
    public final Holder<Integer> current;
    public final Holder<Integer> spent;
    public final RateMeasurer rate;

    PowderType(String tabName, String displayName, List<String> areas, Formatting color, int xpMax, int hypermax,
               Holder<Integer> current, Holder<Integer> spent) {
        this.tabName = tabName;
        this.displayName = displayName;
        this.areas = areas;
        this.color = color;
        this.xpMax = xpMax;
        this.hypermax = hypermax;
        this.current = current;
        this.spent = spent;
        this.rate = new RateMeasurer(this::getTotal);
    }

    private static final DecimalFormat decimalFormat = new DecimalFormat();

    static {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    public MutableText nameWithColor() {
        return Text.literal(displayName).formatted(color);
    }

    public int getTotal() {
        return current.get() + spent.get();
    }

    private MutableText appendMaxText(MutableText text, int total, int max, Formatting formatting) {
        text.append(literal(decimalFormat.format(max), formatting));
        if (total < max)
            text.append(literal(" (", GRAY)
                .append(literal(Long.toString((long) total * 100 / max), formatting))
                .append(literal("%)", GRAY)));
        return text;
    }

    private boolean shouldShow() {
        return SofablockClient.onSkyblock() && areas.contains(SofablockClient.getMode());
    }

    public Text getText() {
        if (!shouldShow()) return null;

        var total = getTotal();
        if (total >= hypermax) return null;

        var text = Text.empty().append(nameWithColor())
            .append(Text.literal(": ").formatted(GRAY))
            .append(Text.literal(decimalFormat.format(total).formatted(WHITE)));
        appendMaxText(text.append(Text.literal(" / ").formatted(GRAY)), total, hypermax, GRAY);
        int gained = rate.getAmountGained();
        if (gained > 0)
            text.append(literal(" - " + gained + "/min", GRAY)
                .append(literal(" - " + Util.formatDuration(rate.calculateETA(hypermax)) + " ETA", GRAY)));

        return text;
    }

    public List<Text> getHoverText() {
        if (!shouldShow()) return null;

        var total = getTotal();
        var texts = new ArrayList<Text>();
        texts.add(nameWithColor());
        texts.add(literal("Current: ", GRAY)
            .append(literal(decimalFormat.format(current.get()), WHITE)));
        texts.add(literal("Spent: ", GRAY)
            .append(literal(decimalFormat.format(spent.get()), WHITE)));
        texts.add(literal("Total: ", GRAY)
            .append(literal(decimalFormat.format(total), WHITE)));
        texts.add(appendMaxText(literal("Max for Skyblock XP: ", GRAY), total, xpMax, WHITE));
        texts.add(appendMaxText(literal("Hypermax: ", GRAY), total, hypermax, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(rate.getAmountGained()), WHITE)));
        texts.add(literal("ETA to max (XP): ", GRAY)
            .append(formatDurationText(rate.calculateETA(xpMax))));
        texts.add(literal("ETA to hypermax: ", GRAY)
            .append(formatDurationText(rate.calculateETA(hypermax))));

        return texts;
    }
}