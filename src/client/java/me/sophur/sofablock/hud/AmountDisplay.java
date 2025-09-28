package me.sophur.sofablock.hud;

import me.sophur.sofablock.Config;
import me.sophur.sofablock.ItemType;
import me.sophur.sofablock.PowderType;
import me.sophur.sofablock.Util;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static me.sophur.sofablock.SofablockHUD.*;

public class AmountDisplay implements TextDisplay {
    public List<Pair<Text, List<Text>>> GetTextLines() {
        ArrayList<Pair<Text, List<Text>>> textLines = new ArrayList<>();

        for (PowderType powderType : PowderType.values()) {
            var amount = Config.INSTANCE.powders.get(powderType);

            Text text = Util.getAmountText(powderType.displayName, powderType.color, powderType.areas, amount.getTotal(), powderType.hypermax, amount.rate);
            if (text == null) continue;
            List<Text> hoverText = Util.getPowderHoverText(powderType, amount);
            textLines.add(new Pair<>(text, hoverText));
        }

        for (ItemType itemType : ItemType.values()) {
            var amount = Config.INSTANCE.items.get(itemType);

            Text text = Util.getAmountText(itemType.displayName, itemType.color, itemType.areas, amount.getTotal(), amount.goal, amount.rate);
            if (text == null) continue;
            List<Text> hoverText = Util.getItemHoverText(itemType, amount);
            textLines.add(new Pair<>(text, hoverText));
        }

        return textLines;
    }

    @Override
    public int GetX() {
        return 0;
    }

    @Override
    public int GetY() {
        return 0;
    }
}
