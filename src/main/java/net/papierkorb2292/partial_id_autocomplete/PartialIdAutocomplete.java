package net.papierkorb2292.partial_id_autocomplete;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public class PartialIdAutocomplete implements ModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static PartialIdAutocompleteConfig config;
    public static String version;

    @Override
    public void onInitialize() {
        version = FabricLoader.getInstance()
            .getModContainer("partial_id_autocomplete")
            .orElseThrow()
            .getMetadata()
            .getVersion()
            .getFriendlyString();
        config = PartialIdAutocompleteConfig.loadFromFile(PartialIdAutocompleteConfig.DEFAULT_CONFIG_PATH, version);
    }
}
