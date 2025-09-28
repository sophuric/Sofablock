package me.sophur.sofablock;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.sophur.sofablock.Util.*;
import static net.minecraft.util.Formatting.*;

public enum PowderType {
    MITHRIL("Mithril", "Mithril Powder", List.of("mining_3", "crystal_hollows"/*, "mineshaft"*/), DARK_GREEN,
            12_500_000, 12_658_220),
    GEMSTONE("Gemstone", "Gemstone Powder", MITHRIL.areas, LIGHT_PURPLE,
            20_000_000, 31_150_666),
    GLACITE("Glacite", "Glacite Powder", MITHRIL.areas, AQUA,
            20_000_000, 34_479_533),
    WHISPERS("Whispers", "Forest Whispers", List.of("foraging_1", "foraging_2"), DARK_AQUA,
            1, 1);

    public final String tabName;
    public final String displayName;
    public final List<String> areas;
    public final Formatting color;
    public final int xpMax, hypermax;

    PowderType(String tabName, String displayName, List<String> areas, Formatting color,
               int xpMax, int hypermax) {
        this.tabName = tabName;
        this.displayName = displayName;
        this.areas = areas;
        this.color = color;
        this.xpMax = xpMax;
        this.hypermax = hypermax;
    }

}