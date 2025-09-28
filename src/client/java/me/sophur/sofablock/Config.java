package me.sophur.sofablock;

import me.sophur.sofablock.AmountValue.AmountGoalValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static me.sophur.sofablock.SofablockClient.MOD_ID;

public class Config {
    public static Config INSTANCE = new Config();

    private Config() {
    }

    public Map<PowderType, AmountValue> powders = new HashMap<>();

    {
        for (PowderType powderType : PowderType.values()) {
            powders.put(powderType, new AmountValue());
        }
    }

    public Map<ItemType, AmountGoalValue> items = new HashMap<>();

    {
        for (ItemType itemType : ItemType.values()) {
            items.put(itemType, new AmountGoalValue());
        }
    }

    public static void openConfigFile() {
        Util.getOperatingSystem().open(getConfigFile());
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json").toFile();
    }

    public static boolean load() throws IOException {
        try {
            try (FileReader fileReader = new FileReader(getConfigFile(), StandardCharsets.UTF_8)) {
                INSTANCE = gson.fromJson(fileReader, Config.class);
            }
            return true;
        } catch (FileNotFoundException ignored) {
            return false;
        }
    }

    public static void save() throws IOException {
        try (FileWriter fileWriter = new FileWriter(getConfigFile(), StandardCharsets.UTF_8)) {
            gson.toJson(INSTANCE, Config.class, fileWriter);
        }
    }
}
