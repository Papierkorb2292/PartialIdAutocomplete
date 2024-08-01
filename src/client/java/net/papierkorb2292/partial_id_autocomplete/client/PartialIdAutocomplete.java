package net.papierkorb2292.partial_id_autocomplete.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class PartialIdAutocomplete implements ModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static PartialIdAutocompleteConfig config;

    @Override
    public void onInitialize() {
        config = PartialIdAutocompleteConfig.loadFromFile(PartialIdAutocompleteConfig.DEFAULT_CONFIG_PATH);
    }
}
