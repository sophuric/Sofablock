package me.sophur.sofablock;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.include.com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static me.sophur.sofablock.SofablockClient.MOD_ID;

public class Config {
    private Config() {
    }

    private Config(Config config) {
        set(config);
    }

    public int
        currentMithrilPowder, spentMithrilPowder,
        currentGemstonePowder, spentGemstonePowder,
        currentGlacitePowder, spentGlacitePowder,
        currentForestWhispers, spentForestWhispers;

    private void set(Config config) {
        currentMithrilPowder = config.currentMithrilPowder;
        spentMithrilPowder = config.spentMithrilPowder;
        currentGemstonePowder = config.currentGemstonePowder;
        spentGemstonePowder = config.spentGemstonePowder;
        currentGlacitePowder = config.currentGlacitePowder;
        spentGlacitePowder = config.spentGlacitePowder;
        currentForestWhispers = config.currentForestWhispers;
        spentForestWhispers = config.spentForestWhispers;
    }

    public static Config INSTANCE = new Config();

    private transient final Gson gson = new Gson();

    private File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json5").toFile();
    }

    public boolean load() throws IOException {
        try {
            try (FileReader fileReader = new FileReader(getConfigFile(), StandardCharsets.UTF_8)) {
                set(gson.fromJson(fileReader, Config.class));
            }
            return true;
        } catch (FileNotFoundException ignored) {
            return false;
        }
    }

    public void save() throws IOException {
        try (FileWriter fileWriter = new FileWriter(getConfigFile(), StandardCharsets.UTF_8)) {
            gson.toJson(this, Config.class, fileWriter);
        }
    }
}
