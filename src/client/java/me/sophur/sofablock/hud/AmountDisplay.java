package me.sophur.sofablock.hud;

import me.sophur.sofablock.ItemStorage;
import me.sophur.sofablock.SkyblockItem;
import me.sophur.sofablock.SofablockClient;
import me.sophur.sofablock.itemdata.Stack;
import me.sophur.sofablock.tracker.Amount;
import me.sophur.sofablock.tracker.PowderType;
import me.sophur.sofablock.tracker.RateMeasurer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.sophur.sofablock.SofablockHud.*;
import static me.sophur.sofablock.Util.*;
import static me.sophur.sofablock.tracker.Amount.*;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.WHITE;

public class AmountDisplay implements TextDisplay {
    private void recurseItemRecipeTree(String id, @Nullable SkyblockItem item, ItemAmount amount, int indent, Consumer<Pair<Text, List<Text>>> addTextLine, List<String> previousIDs) {
        int total = amount.getTotal();

        MutableText text = Text.empty().append(literal("  ".repeat(indent), GRAY));

        String displayName = item == null ? "Unknown Item: " + id : item.getDisplayName();

        text = getAmountText(text, displayName, item == null ? Formatting.RED : Formatting.RESET, total, amount.goal, amount.rate);
        List<Text> hoverText = getItemHoverText(displayName, amount);

        int remaining = amount.goal - total;
        var recipe = item == null ? null : item.getRecipes().stream().findFirst().orElse(null);

        if (remaining <= 0 || recipe == null) {
            addTextLine.accept(new Pair<>(text, hoverText));
            return;
        }

        int count = recipe.getCount();
        remaining = (remaining + count - 1) / count; // e.g. if the recipe outputs 2 items, but we need 3, we will need ceil(3/2)=2 of the output

        text = text.append(literal(" (" + recipe.getType().id() + " x" + remaining + ")", GRAY));
        addTextLine.accept(new Pair<>(text, hoverText));

        for (Stack stack : recipe.getCommonInputs()) {
            var inputAmount = stack.getAmount() * remaining;
            var inputID = stack.getID();

            if (previousIDs.contains(inputID)) continue;

            var modifiedAmount = ItemStorage.INSTANCE.getItemAmount(inputID);
            if (modifiedAmount == null) modifiedAmount = new ItemAmount();
            modifiedAmount.goal = inputAmount;

            var inputItem = SkyblockItem.getItem(inputID);

            // keep track of previous item IDs to prevent circular dependencies
            var newIDs = new ArrayList<>(previousIDs);
            newIDs.add(id);

            recurseItemRecipeTree(inputID, inputItem, modifiedAmount, indent + 1, addTextLine, newIDs);
        }
    }

    public List<Pair<Text, List<Text>>> GetTextLines() {
        if (!SofablockClient.onSkyblock()) return null;

        ArrayList<Pair<Text, List<Text>>> textLines = new ArrayList<>();

        for (PowderType powderType : PowderType.values()) {
            Amount amount = ItemStorage.INSTANCE.powders.get(powderType);

            if (!inArea(powderType.areas)) continue;

            int total = amount.getTotal();
            if (total >= powderType.hypermax) continue;

            Text text = getAmountText(Text.empty(), powderType.displayName, powderType.color, total, powderType.hypermax, amount.rate);
            List<Text> hoverText = getPowderHoverText(powderType, amount);
            textLines.add(new Pair<>(text, hoverText));
        }

        for (String itemID : ItemStorage.INSTANCE.getItemKeys()) {
            SkyblockItem item = SkyblockItem.getItem(itemID);
            ItemAmount amount = ItemStorage.INSTANCE.getItemAmount(itemID);
            if (amount.goal > 0)
                recurseItemRecipeTree(itemID, item, amount, 0, textLines::add, List.of());
        }

        return textLines;
    }

    @Override
    public float GetX(float width, float height) {
        return 16;
    }

    @Override
    public float GetY(float width, float height) {
        return 16;
    }
    
    @Override
    public float GetScale(){
        return 0.5f;
    }

    private static MutableText getMaxText(MutableText text, int total, int max, Formatting formatting) {
        text.append(literal(decimalFormat.format(max), formatting));
        if (total < max)
            text.append(literal(" (", GRAY)
                .append(literal(Long.toString((long) total * 100 / max), formatting))
                .append(literal("%)", GRAY)));
        return text;
    }

    private static MutableText getAmountText(MutableText text, String name, Formatting color, int total, int hypermax, RateMeasurer rate) {
        text = text.append(literal(name, color))
            .append(literal(": ", GRAY))
            .append(literal(decimalFormat.format(total), WHITE));
        getMaxText(text.append(literal(" / ", GRAY)), total, hypermax, GRAY);
        if (rate != null) {
            int gained = rate.getAmountGained();
            if (gained > 0)
                text.append(literal(" - " + gained + "/min", GRAY));
        }

        return text;
    }

    private static List<Text> getGenericHoverText(String displayName, Formatting color, Amount amount) {
        var texts = new ArrayList<Text>();
        texts.add(literal(displayName, color));
        texts.add(literal("Current: ", GRAY)
            .append(literal(decimalFormat.format(amount.current), WHITE)));
        texts.add(literal("Spent: ", GRAY)
            .append(literal(decimalFormat.format(amount.spent), WHITE)));
        texts.add(literal("Total: ", GRAY)
            .append(literal(decimalFormat.format(amount.getTotal()), WHITE)));

        return texts;
    }

    private static List<Text> getPowderHoverText(PowderType type, Amount amount) {
        List<Text> texts = getGenericHoverText(type.displayName, type.color, amount);

        var total = amount.getTotal();
        texts.add(getMaxText(literal("Max for Skyblock XP: ", GRAY), total, type.xpMax, WHITE));
        texts.add(getMaxText(literal("Hypermax: ", GRAY), total, type.hypermax, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(amount.rate.getAmountGained()), WHITE)));

        return texts;
    }

    private static List<Text> getItemHoverText(String name, AmountWithGoal amount) {
        List<Text> texts = getGenericHoverText(name, Formatting.RESET, amount);

        var total = amount.getTotal();
        texts.add(getMaxText(literal("Progress: ", GRAY), total, amount.goal, WHITE));
        texts.add(Text.empty());
        texts.add(literal("In last minute: ", GRAY)
            .append(literal(decimalFormat.format(amount.rate.getAmountGained()), WHITE)));

        return texts;
    }
}
