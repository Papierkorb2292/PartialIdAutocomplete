package net.papierkorb2292.partial_id_autocomplete.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.text.Text;

public class PartialIdAutocompleteConfigScreen extends GameOptionsScreen {
    public PartialIdAutocompleteConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("partial_id_autocomplete.config.title"));
    }

    @Override
    protected void addOptions() {
        if(body != null) {
            final var options = PartialIdAutocomplete.config.asOptions();
            for(final var option : options) {
                body.addSingleOptionEntry(option);
            }
        }
    }
}
