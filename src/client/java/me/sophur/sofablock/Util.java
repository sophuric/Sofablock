package me.sophur.sofablock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Util {
    private Util() {
    }

    public static final Pattern NUMBER_REGEX = Pattern.compile("[\\d,]+");
    public static final Pattern NEWLINE = Pattern.compile("\n");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonElement readJson(File file) throws IOException {
        JsonElement json;
        try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(fileReader);
        } catch (FileNotFoundException ignored) {
            return null;
        }
        return json;
    }

    public static void writeJson(File file, JsonElement json) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(json, fileWriter);
        }
    }

    public static <T> Codec<T> codecFromOptional(Codec<Optional<T>> codec, Supplier<String> error) {
        return codec.flatXmap(e ->
            e.map(DataResult::success).orElse(DataResult.error(error)), s -> DataResult.success(Optional.of(s)));
    }

    public static Stream<Slot> getNonPlayerSlots(Stream<Slot> slots) {
        return slots.filter(slot -> !(slot.inventory instanceof PlayerInventory));
    }

    public static Stream<ItemStack> toStacks(Stream<Slot> slots) {
        return slots.map(Slot::getStack).filter(stack -> stack != null && !stack.isEmpty());
    }

    public static Stream<ItemStack> getNonPlayerStacks(Stream<Slot> slots) {
        return toStacks(getNonPlayerSlots(slots));
    }

    public static List<Slot> getNonPlayerSlots(List<Slot> slots) {
        return getNonPlayerSlots(slots.stream()).toList();
    }

    public static List<ItemStack> toStacks(List<Slot> slots) {
        return toStacks(slots.stream()).toList();
    }

    public static List<ItemStack> getNonPlayerStacks(List<Slot> slots) {
        return getNonPlayerStacks(slots.stream()).toList();
    }

    public static Optional<ItemStack> getStackByName(List<ItemStack> stacks, String name) {
        return stacks.stream().filter(slot -> slot.getName().getString().equals(name)).findFirst();
    }

    public static Matcher matchStrings(Stream<String> strings, Pattern pattern) {
        return strings.map(pattern::matcher).filter(Matcher::matches).findFirst().orElse(null);
    }

    public static Matcher matchTexts(Stream<Text> texts, Pattern pattern) {
        return matchStrings(texts.map(Text::getString), pattern);
    }

    public static List<File> getJsonFilesInDir(Path dir) {
        return Arrays.stream(Objects.requireNonNull(dir.toFile().listFiles(f -> f.isFile() && f.getName().endsWith(".json")))).toList();
    }

    public static Matcher matchTexts(List<Text> texts, Pattern pattern) {
        return matchTexts(texts.stream(), pattern);
    }

    public static int parseAmount(String string) throws NumberFormatException {
        return Integer.parseInt(string.replaceAll(",", ""));
    }

    public static MutableText literal(String string, Formatting formatting) {
        return Text.literal(string).formatted(formatting);
    }

    public static final DecimalFormat decimalFormat = new DecimalFormat();

    static {
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    public static boolean inArea(List<String> areas) {
        return SofablockClient.onSkyblock() && (areas == null || areas.contains(SofablockClient.getMode()));
    }
}