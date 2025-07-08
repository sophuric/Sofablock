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
            SofablockClient.LOGGER.error("Failed to parse powder amount: {}", string);
            return false;
        }
    }

    public static MutableText literal(String string, Formatting formatting) {
        return Text.literal(string).formatted(formatting);
    }

    public static long calculateETA(int current, int destination, int gained, long period) {
        if (gained == 0) return -1;
        // current + gained * time/period = destination;
        return (destination - current) * period / gained;
    }

    public static MutableText formatDurationText(long nano) {
        if (nano < 0) return literal("N/A", RED);

        MutableText text = Text.empty();

        double seconds = (nano / 1_000_000) / 1000d;
        String c = "s";
        if (seconds >= 60) {
            seconds /= 60;
            c = "m";
            if (seconds >= 60) {
                seconds /= 60;
                c = "h";
                if (seconds >= 24) {
                    seconds /= 24;
                    c = "d";
                    if (seconds >= 7) {
                        seconds /= 7;
                        c = "w";
                    }
                }
            }
        }
        return text.append(literal(toStringRound(seconds, 2), WHITE)).append(literal(c, GRAY));
    }

    public static String toStringRound(double number, int digits) {
        double multiplier = 1;
        for (int i = 0; i < digits; ++i) multiplier *= 10;
        number *= multiplier;
        number = Math.round(number);
        number /= multiplier;
        return Double.toString(number);
    }

    public static String formatDuration(long nano) {
        return formatDurationText(nano).getString();
    }
}