package me.sophur.sofablock.tracker;

import com.mojang.serialization.Codec;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

import static net.minecraft.util.Formatting.*;

public enum PowderType implements StringIdentifiable {
    MITHRIL("Mithril", "Mithril Powder", "SKYBLOCK_POWDER_MITHRIL", List.of("mining_3", "crystal_hollows"/*, "mineshaft"*/), DARK_GREEN,
        12_500_000, 12_658_220),
    GEMSTONE("Gemstone", "Gemstone Powder", "SKYBLOCK_POWDER_GEMSTONE", MITHRIL.areas, LIGHT_PURPLE,
        20_000_000, 31_150_666),
    GLACITE("Glacite", "Glacite Powder", "SKYBLOCK_POWDER_GLACITE", MITHRIL.areas, AQUA,
        20_000_000, 34_479_533),
    WHISPERS("Whispers", "Forest Whispers", "SKYBLOCK_FOREST_WHISPERS", List.of("foraging_1", "foraging_2"), DARK_AQUA,
        1, 1);

    public static final Codec<PowderType> CODEC = StringIdentifiable.createCodec(PowderType::values);

    public final String tabName;
    public final String displayName;
    public final String itemRepoName;
    public final List<String> areas;
    public final Formatting color;
    public final int xpMax, hypermax;

    PowderType(String tabName, String displayName, String itemRepoName, List<String> areas, Formatting color,
               int xpMax, int hypermax) {
        this.tabName = tabName;
        this.displayName = displayName;
        this.itemRepoName = itemRepoName;
        this.areas = areas;
        this.color = color;
        this.xpMax = xpMax;
        this.hypermax = hypermax;
    }

    @Override
    public String asString() {
        return name();
    }
}