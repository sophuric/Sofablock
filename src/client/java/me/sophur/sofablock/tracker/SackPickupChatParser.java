package me.sophur.sofablock.tracker;

import me.sophur.sofablock.ItemStorage;
import me.sophur.sofablock.SkyblockItem;
import me.sophur.sofablock.SofablockClient;
import me.sophur.sofablock.Util;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.sophur.sofablock.SofablockClient.*;
import static me.sophur.sofablock.Util.NUMBER_REGEX;
import static me.sophur.sofablock.Util.parseAmount;

public class SackPickupChatParser {
    private static final Pattern SACK_REGEX = Pattern.compile("^\\[Sacks] [+-]" + NUMBER_REGEX + " items?(?:, [+-]" + NUMBER_REGEX + " items?)?\\. \\(Last " + NUMBER_REGEX + "s\\.\\)$");
    private static final Pattern PRISTINE_REGEX = Pattern.compile("^PRISTINE! You found (.*) x(" + NUMBER_REGEX + ")!$");
    private static final Pattern ITEM_REGEX = Pattern.compile("^ {2}([+-]" + NUMBER_REGEX + ") (.*) \\(.*\\)$");

    private SackPickupChatParser() {
    }

    public static boolean handleGame(Text text, boolean overlay) {
        if (overlay) return true;
        if (!onSkyblock()) return true;
        var textString = text.getString();

        // find message like "[Sacks] +2,591 items. (Last 11s.)"
        if (SACK_REGEX.matcher(textString).matches()) {
            var hoverOpts = text.getSiblings().stream().filter(t -> t.getStyle().getHoverEvent() != null)
                    .filter(t ->t.getString().contains("item") ); // one text has the amount, one text says " items" or " item", both have hover text
            hoverOpts.forEach(hoverOpt -> {
                // get hover text from message
                HoverEvent hover = hoverOpt.getStyle().getHoverEvent();
                if (hover == null) return; // shouldn't happen
                if (!(hover instanceof HoverEvent.ShowText(Text hoverText))) return;

                // split lines in hover text
                String[] lines = Util.NEWLINE.split(hoverText.getString());
                // loop items
                for (String line : lines) {
                    // find line like "  +113 Mithril (Mining Sack)"
                    Matcher match = ITEM_REGEX.matcher(line);
                    if (!match.matches()) continue;

                    String amountString = match.group(1);
                    int amount;
                    try {
                        amount = parseAmount(amountString);
                    } catch (NumberFormatException e) {
                        LOGGER.error("Failed to parse sack item pickup amount: {}", amountString, e);
                        continue;
                    }

                    String itemDisplayName = match.group(2);
                    var item = SkyblockItem.getItemByDisplayName(itemDisplayName);
                    if (item == null) {
                        LOGGER.error("Failed to get item by display name: {}", itemDisplayName);
                        continue;
                    }

                    var itemAmount = ItemStorage.INSTANCE.getItemAmount(item.getID());
                    itemAmount.temp = 0;
                    itemAmount.addCurrent(amount);
                }
            });
            return true;
        } else {
            // instead find message like "PRISTINE! You found ✎ Flawed Sapphire Gemstone x38!"
            Matcher match = PRISTINE_REGEX.matcher(textString);
            if (match.matches()) {
                String amountString = match.group(2);
                int amount;
                try {
                    amount = parseAmount(amountString);
                } catch (NumberFormatException e) {
                    LOGGER.error("Failed to parse pristine item amount: {}", amountString, e);
                    return true;
                }

                String itemDisplayName = match.group(1);
                var item = SkyblockItem.getItemByDisplayName(itemDisplayName);
                if (item == null) {
                    LOGGER.error("Failed to get item by display name: {}", itemDisplayName);
                    return true;
                }

                var itemAmount = ItemStorage.INSTANCE.getItemAmount(item.getID());
                itemAmount.addTemp(amount);
                // pristine count seems to be inaccurate sometimes, I'm not sure why
                return true;
            }
        }
        return true;
    }
}
