package me.sophur.sofablock.tracker;

import me.sophur.sofablock.ItemStorage;
import me.sophur.sofablock.SkyblockItem;
import me.sophur.sofablock.SofablockClient;
import me.sophur.sofablock.Util;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

import static me.sophur.sofablock.Util.NUMBER_REGEX;
import static me.sophur.sofablock.Util.parseAmount;

public class SackPickupChatParser {
    private static final Pattern SACK_REGEX = Pattern.compile("^\\[Sacks] \\+" + NUMBER_REGEX + " items\\. \\(Last " + NUMBER_REGEX + "s\\.\\)$");
    private static final Pattern ITEM_REGEX = Pattern.compile("^ {2}\\+(" + NUMBER_REGEX + ") (.*) \\(.*\\)$");

    private SackPickupChatParser() {
    }

    public static boolean handleGame(Text text, boolean overlay) {
        if (overlay) return true;
        if (!SofablockClient.onSkyblock()) return true;

        // find message like `[Sacks] +2,591 items. (Last 11s.)`
        if (!SACK_REGEX.matcher(text.getString()).matches()) return true;
        var hoverOpt = text.getSiblings().stream().filter(t -> t.getStyle().getHoverEvent() != null).findFirst();
        if (hoverOpt.isEmpty()) return true;

        // get hover text from message
        HoverEvent hover = hoverOpt.get().getStyle().getHoverEvent();
        if (hover == null) return true; // shouldn't happen
        if (!(hover instanceof HoverEvent.ShowText(Text hoverText))) return true;

        // split lines in hover text
        String[] lines = Util.NEWLINE.split(hoverText.getString());
        // loop items
        for (String line : lines) {
            var match = ITEM_REGEX.matcher(line);
            if (!match.matches()) continue;

            int amount;
            String str = match.group(1);
            try {
                amount = parseAmount(match.group(1));
            } catch (NumberFormatException e) {
                SofablockClient.LOGGER.error("Failed to parse sack pickup amount, line: {}, parsing: {}", line, str, e);
                continue;
            }

            String itemName = match.group(2);
            var item = SkyblockItem.getItemByDisplayName(itemName);
            if (item == null) {
                SofablockClient.LOGGER.error("Failed to get item by name: {}", itemName);
                continue;
            }

            int newAmount = ItemStorage.INSTANCE.addItemSackCount(item.getID(), amount);
            SofablockClient.LOGGER.info("DEBUG: increase {} by {} to {}", item.getID(), amount, newAmount);
        }
        return true;
    }
}
