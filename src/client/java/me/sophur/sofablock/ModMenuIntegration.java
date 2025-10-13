package me.sophur.sofablock;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.io.IOException;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            try {
                ItemStorage.save();
            } catch (IOException e) {
                SofablockClient.LOGGER.error("Failed to save config", e);
                return null;
            }
            ItemStorage.openConfigFile();
            return null;
        };
    }
}
