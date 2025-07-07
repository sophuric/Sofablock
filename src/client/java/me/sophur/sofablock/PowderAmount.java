package me.sophur.sofablock;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.sophur.sofablock.Util.Holder;

import static me.sophur.sofablock.Util.createHolder;
import static me.sophur.sofablock.Config.INSTANCE;
import static net.minecraft.util.Formatting.*;

public record PowderAmount(String id, String name, List<String> areas, Formatting color, int xpMax, int hypermax,
                           Holder<Integer> current, Holder<Integer> spent) {
    private static final DecimalFormat decimalFormat = new DecimalFormat();

    static {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    public MutableText nameWithColor() {
        return Text.literal(name).formatted(color);
    }

    public int getTotal() {
        return current.get() + spent.get();
    }

    private MutableText appendMaxText(MutableText text, int total, int max) {
        text = text.append(Text.literal(decimalFormat.format(max).formatted(GRAY)));
        if (total < max)
            text = text.append(Text.literal(" (").formatted(GRAY))
                .append(Text.literal(Long.toString((long) total * 100 / max).formatted(GRAY)))
                .append(Text.literal("%)").formatted(GRAY));
        return text;
    }

    private boolean shouldShow() {
        return SofablockClient.onSkyblock() && areas.contains(SofablockClient.getMode());
    }

    public Text getText() {
        if (!shouldShow()) return null;

        var total = getTotal();
        var text = Text.empty().append(nameWithColor())
            .append(Text.literal(": ").formatted(GRAY))
            .append(Text.literal(decimalFormat.format(total).formatted(WHITE)));
        if (total < hypermax) appendMaxText(text.append(Text.literal(" / ").formatted(GRAY)), total, hypermax);

        return text;
    }

    public List<Text> getHoverText() {
        if (!shouldShow()) return null;

        var total = getTotal();
        var texts = new ArrayList<Text>();
        texts.add(nameWithColor());
        texts.add(Text.empty().append(Text.literal("Current: ").formatted(GRAY))
            .append(Text.literal(decimalFormat.format(current.get())).formatted(WHITE)));
        texts.add(Text.empty().append(Text.literal("Spent: ").formatted(GRAY))
            .append(Text.literal(decimalFormat.format(spent.get())).formatted(WHITE)));
        texts.add(Text.empty().append(Text.literal("Total: ").formatted(GRAY))
            .append(Text.literal(decimalFormat.format(total)).formatted(WHITE)));
        texts.add(appendMaxText(Text.empty().append(Text.literal("Max for Skyblock XP: ").formatted(GRAY)), total, xpMax));
        texts.add(appendMaxText(Text.empty().append(Text.literal("Hypermax: ").formatted(GRAY)), total, hypermax));

        return texts;
    }

    private static final List<String> miningAreas = List.of("mining_3", "crystal_hollows", "mineshaft");

    public static PowderAmount getPowder(String id) {
        return POWDERS.stream().filter(powder -> powder.id.equals(id)).findFirst().orElseThrow();
    }

    public static final List<PowderAmount> POWDERS = List.of(
        new PowderAmount("mithril", "Mithril Powder", miningAreas, DARK_GREEN, 12_500_000, 12_658_220,
            createHolder(() -> INSTANCE.currentMithrilPowder, v -> INSTANCE.currentMithrilPowder = v),
            createHolder(() -> INSTANCE.spentMithrilPowder, v -> INSTANCE.spentMithrilPowder = v)
        ),
        new PowderAmount("gemstone", "Gemstone Powder", miningAreas, LIGHT_PURPLE, 20_000_000, 31_150_666,
            createHolder(() -> INSTANCE.currentGemstonePowder, v -> INSTANCE.currentGemstonePowder = v),
            createHolder(() -> INSTANCE.spentGemstonePowder, v -> INSTANCE.spentGemstonePowder = v)
        ),
        new PowderAmount("glacite", "Glacite Powder", miningAreas, AQUA, 20_000_000, 34_479_533,
            createHolder(() -> INSTANCE.currentGlacitePowder, v -> INSTANCE.currentGlacitePowder = v),
            createHolder(() -> INSTANCE.spentGlacitePowder, v -> INSTANCE.spentGlacitePowder = v)
        ),
        new PowderAmount("forest", "Forest Whispers", List.of("foraging_1", "foraging_2"), DARK_AQUA, 1, 1,
            createHolder(() -> INSTANCE.currentForestWhispers, v -> INSTANCE.currentForestWhispers = v),
            createHolder(() -> INSTANCE.spentForestWhispers, v -> INSTANCE.spentForestWhispers = v)
        )
    );
}