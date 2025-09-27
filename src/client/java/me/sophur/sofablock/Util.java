package me.sophur.sofablock;

import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.Formatting.*;

public class Util {
    private Util() {
    }

    // I love boilerplate

    public static <T> Holder<T> createHolder(Supplier<T> getter, Consumer<T> setter) {
        return new Holder<>() {
            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T t) {
                setter.accept(t);
            }
        };
    }

    public static abstract class Holder<T> {
        public abstract T get();

        public abstract void set(T t);
    }

    public static Optional<Slot> getSlotByName(AbstractList<Slot> slots, String name) {
        return slots.stream().filter(slot -> slot.getStack().getName().getString().equals(name)).findFirst();
    }

    public static Matcher matchTexts(List<Text> texts, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        return texts.stream().map(text -> regex.matcher(text.getString())).filter(Matcher::find).findFirst().orElse(null);
    }

    public static boolean setAmountFromString(String string, Holder<Integer> amount, PowderType powder) {
        string = string.replaceAll(",", "");
        try {
            amount.set(Integer.parseInt(string));
            powder.rate.updateValue();
            return true;
        } catch (NumberFormatException ignored) {
            SofablockClient.LOGGER.error("Failed to parse amount: {}", string);
            return false;
        }
    }

    public static MutableText literal(String string, Formatting formatting) {
        return Text.literal(string).formatted(formatting);
    }
}