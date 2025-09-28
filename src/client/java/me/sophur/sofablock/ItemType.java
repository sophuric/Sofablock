package me.sophur.sofablock;

import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

import static me.sophur.sofablock.PowderType.MITHRIL;
import static net.minecraft.util.Formatting.*;

public enum ItemType {
    JADE_GEMSTONE("Jade Gemstone", MITHRIL.areas, GREEN, getGemstones("JADE")),
    AMBER_GEMSTONE("Amber Gemstone", MITHRIL.areas, GOLD, getGemstones("AMBER")),
    TOPAZ_GEMSTONE("Topaz Gemstone", MITHRIL.areas, YELLOW, getGemstones("TOPAZ")),
    SAPPHIRE_GEMSTONE("Sapphire Gemstone", MITHRIL.areas, AQUA, getGemstones("SAPPHIRE")),
    AMETHYST_GEMSTONE("Amethyst Gemstone", MITHRIL.areas, DARK_PURPLE, getGemstones("AMETHYST")),
    JASPER_GEMSTONE("Jasper Gemstone", MITHRIL.areas, LIGHT_PURPLE, getGemstones("JASPER")),
    RUBY_GEMSTONE("Ruby Gemstone", MITHRIL.areas, RED, getGemstones("RUBY")),
    OPAL_GEMSTONE("Opal Gemstone", MITHRIL.areas, WHITE, getGemstones("OPAL")),
    ONYX_GEMSTONE("Onyx Gemstone", MITHRIL.areas, DARK_GRAY, getGemstones("ONYX")),
    AQUAMARINE_GEMSTONE("Aquamarine Gemstone", MITHRIL.areas, BLUE, getGemstones("AQUAMARINE")),
    CITRINE_GEMSTONE("Citrine Gemstone", MITHRIL.areas, DARK_RED, getGemstones("CITRINE")),
    PERIDOT_GEMSTONE("Peridot Gemstone", MITHRIL.areas, DARK_GREEN, getGemstones("PERIDOT")) ;

    private static Map<String, Integer> getGemstones(String id) {
        id = id.toUpperCase();
        return Map.of(
                "ROUGH_%s_GEM".formatted(id), 1,
                "FLAWED_%s_GEM".formatted(id), 80,
                "FINE_%s_GEM".formatted(id), 80 * 80,
                "FLAWLESS_%s_GEM".formatted(id), 80 * 80 * 80,
                "PERFECT_%s_GEM".formatted(id), 80 * 80 * 80 * 5);
    }

    public final String displayName;
    public final List<String> areas;
    public final Formatting color;
    public final Map<String, Integer> itemCount;

    ItemType(String displayName, List<String> areas, Formatting color, Map<String, Integer> itemCount) {
        this.displayName = displayName;
        this.areas = areas;
        this.color = color;
        this.itemCount = itemCount;
    }
}