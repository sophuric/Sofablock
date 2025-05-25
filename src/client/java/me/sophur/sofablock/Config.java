package me.sophur.sofablock;

import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.Color;

import static me.sophur.sofablock.SofablockClient.MOD_ID;

public class Config {
    public static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of(MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json5"))
                    .setJson5(true).build()
            )
            .build();

    public Config() {

    }

    public Config(Config config) {
        set(config);
    }

    public void set(Config config) {
        crystalHollowsEnabled = config.crystalHollowsEnabled;
        color = config.color;
        fadeStart = config.fadeStart;
        fadeEnd = config.fadeEnd;
    }

    @SerialEntry
    public boolean crystalHollowsEnabled = true;

    @SerialEntry
    public Color color = new Color(0xfff5c2e7);

    public static ValueFormatter<Float> blocksFormatter = value -> Text.translatable("config.sofablock.blocks_value", value);

    @SerialEntry
    public float fadeStart = 16, fadeEnd = 32;
}
