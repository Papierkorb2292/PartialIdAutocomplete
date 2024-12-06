package net.papierkorb2292.partial_id_autocomplete.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.papierkorb2292.partial_id_autocomplete.PartialIdAutocomplete;
import net.papierkorb2292.partial_id_autocomplete.PartialIdAutocompleteConfig;

public class PartialIdAutocompleteConfigScreen extends GameOptionsScreen {
    public PartialIdAutocompleteConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("partial_id_autocomplete.config.title"));
    }

    @Override
    protected void addOptions() {
        if(body != null) {
            final var config = PartialIdAutocomplete.config;
            body.addSingleOptionEntry(new SimpleOption<>(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_SEGMENT_SEPARATOR_REGEX_NAME),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_SEGMENT_SEPARATOR_REGEX_NAME) + ".description")),
                    (option, value) -> Text.literal(value),
                    SimpleOptionStringCallbacks.INSTANCE,
                    config.getIdSegmentSeparatorRegex(),
                    config::setIdSegmentSeparatorRegex
            ));
            body.addSingleOptionEntry(new SimpleOption<>(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_VALIDATOR_REGEX),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_VALIDATOR_REGEX) + ".description")),
                    (option, value) -> Text.literal(value),
                    SimpleOptionStringCallbacks.INSTANCE,
                    config.getIdValidatorRegex(),
                    config::setIdValidatorRegex
            ));
            body.addSingleOptionEntry(SimpleOption.ofBoolean(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.COLLAPSE_SINGLE_CHILD_NODES_NAME),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.COLLAPSE_SINGLE_CHILD_NODES_NAME) + ".description")),
                    config.getCollapseSingleChildNodes(),
                    config::setCollapseSingleChildNodes
            ));
            body.addSingleOptionEntry(SimpleOption.ofBoolean(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.ONLY_SUGGEST_NEXT_SEGMENTS_NAME),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.ONLY_SUGGEST_NEXT_SEGMENTS_NAME) + ".description")),
                    config.getOnlySuggestNextSegments(),
                    config::setOnlySuggestNextSegments
            ));
        }
    }

    private static String convertNameToTranslationKey(String name) {
        return "partial_id_autocomplete.config." + name.replace("-", "_");
    }
}
