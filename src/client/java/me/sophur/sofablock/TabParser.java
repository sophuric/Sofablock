package me.sophur.sofablock;

import me.sophur.sofablock.mixin.PlayerListHudMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabParser {
    private TabParser() {
    }

    public static void handleTick(MinecraftClient client) {
        if (!SofablockClient.onSkyblock()) return;
        if (client.player == null) return;

        PlayerListHud playerList = client.inGameHud.getPlayerListHud();
        if (playerList == null) return;

        List<PlayerListEntry> tabEntries = ((PlayerListHudMixin)playerList).invokeCollectPlayerEntries();

        boolean lastPowder = false;

        for (PlayerListEntry entry : tabEntries) {
            var name = entry.getDisplayName();
            if (name == null) continue;
            var nameString = name.getString();
            // The value for Forest Whispers is shortened tab (e.g. 5k) so an exact number cannot be retrieved from there
            if (nameString.equals("Powders:")) {
                lastPowder = true;
                continue;
            }
            if (lastPowder) {
                boolean foundPowder = false;
                for (PowderType powderType : HOTMParser.HeartType.HOTM.amountTypes) {
                    Matcher matcher = Pattern.compile("^ " + powderType.tabName + ": ([0-9,]+)$").matcher(nameString);
                    if (!matcher.find()) continue;
                    foundPowder = true;
                    try {
                        Config.INSTANCE.powders.get(powderType).current = Util.parseAmount(matcher.group(1));
                    } catch (NumberFormatException e) {
                        SofablockClient.LOGGER.error("Failed to parse current powder for {}", powderType.displayName, e);
                    }
                    break;
                }
                if (!foundPowder) lastPowder = false;
            }
        }
    }
}
