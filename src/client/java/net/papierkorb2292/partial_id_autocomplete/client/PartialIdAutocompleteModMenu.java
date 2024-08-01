package net.papierkorb2292.partial_id_autocomplete.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class PartialIdAutocompleteModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PartialIdAutocompleteConfigScreen::new;
    }
}
