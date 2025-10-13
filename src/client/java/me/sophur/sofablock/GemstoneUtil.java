package me.sophur.sofablock;

import java.util.Arrays;
import java.util.regex.Pattern;

public class GemstoneUtil {
    private GemstoneUtil() {
    }

    public enum GemstoneTier {
        ROUGH,
        FLAWED,
        FINE,
        FLAWLESS,
        PERFECT;

        public String getID(String gemstone) {
            return name() + "_" + gemstone.toUpperCase() + "_GEM";
        }
    }

    private static final Pattern GEMSTONE_ID_REGEX = Pattern.compile(
        "^(?:" + String.join("|", Arrays.stream(GemstoneTier.values()).map(Enum::name).toList()) + ")_([A-Z]+)_GEM$");

    public static boolean isGemstone(String id) {
        return GEMSTONE_ID_REGEX.matcher(id).matches();
    }

    public static String getGemstone(String id) {
        var match = GEMSTONE_ID_REGEX.matcher(id);
        if (!match.matches()) return null;
        return match.group(1);
    }
}
