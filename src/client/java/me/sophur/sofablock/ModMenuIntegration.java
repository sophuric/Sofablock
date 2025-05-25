package me.sophur.sofablock;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.text.Text;

import java.awt.Color;

public class ModMenuIntegration implements ModMenuApi {
    private final static Config defaultConfig = new Config();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> {
            if (!Config.HANDLER.load()) throw new RuntimeException("Failed to load config");
            Config tempConfig = new Config(Config.HANDLER.instance());
            return YetAnotherConfigLib.createBuilder()
                    .title(Text.translatable("config.sofablock.title"))
                    .category(ConfigCategory.createBuilder()
                            .name(Text.translatable("config.sofablock.border.name"))
                            .tooltip(Text.translatable("config.sofablock.border.tooltip"))

                            .group(OptionGroup.createBuilder()
                                    .name(Text.translatable("config.sofablock.areas.name"))
                                    .description(OptionDescription.of(Text.translatable("config.sofablock.areas.description")))
                                    .option(Option.<Boolean>createBuilder()
                                            .name(Text.translatable("config.sofablock.area.crystal_hollows.name"))
                                            .description(OptionDescription.of(Text.translatable("config.sofablock.area.crystal_hollows.description")))
                                            .binding(defaultConfig.crystalHollowsEnabled, () -> tempConfig.crystalHollowsEnabled, v -> tempConfig.crystalHollowsEnabled = v)
                                            .controller(TickBoxControllerBuilder::create).build())
                                    .build())

                            .group(OptionGroup.createBuilder()
                                    .name(Text.translatable("config.sofablock.options.name"))
                                    .description(OptionDescription.of(Text.translatable("config.sofablock.options.description")))

                                    .option(Option.<Color>createBuilder()
                                            .name(Text.translatable("config.sofablock.color.name"))
                                            .description(OptionDescription.of(Text.translatable("config.sofablock.color.description")))
                                            .binding(defaultConfig.color, () -> tempConfig.color, v -> tempConfig.color = v)
                                            .controller(option -> ColorControllerBuilder.create(option).allowAlpha(true)).build())

                                    .option(Option.<Float>createBuilder()
                                            .name(Text.translatable("config.sofablock.fade_start.name"))
                                            .description(OptionDescription.of(Text.translatable("config.sofablock.fade_start.description")))
                                            .binding(defaultConfig.fadeStart, () -> tempConfig.fadeStart, v -> tempConfig.fadeStart = v)
                                            .controller(option -> FloatSliderControllerBuilder.create(option)
                                                    .range(0f, 128f).step(0.5f).formatValue(Config.blocksFormatter)
                                            ).build())
                                    .option(Option.<Float>createBuilder()
                                            .name(Text.translatable("config.sofablock.fade_end.name"))
                                            .description(OptionDescription.of(Text.translatable("config.sofablock.fade_end.description")))
                                            .binding(defaultConfig.fadeEnd, () -> tempConfig.fadeEnd, v -> tempConfig.fadeEnd = v)
                                            .controller(option -> FloatSliderControllerBuilder.create(option)
                                                    .range(1f, 128f).step(0.5f).formatValue(Config.blocksFormatter)
                                            ).build())
                                    .build())
                            .build())
                    .save(() -> {
                        Config.HANDLER.instance().set(tempConfig);
                        Config.HANDLER.save();
                    }).build().generateScreen(parentScreen);
        };
    }
}
