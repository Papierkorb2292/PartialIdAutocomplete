package net.papierkorb2292.partial_id_autocomplete.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.papierkorb2292.partial_id_autocomplete.PartialIdAutocomplete;
import net.papierkorb2292.partial_id_autocomplete.PartialIdAutocompleteConfig;

public class PartialIdAutocompleteConfigScreen extends OptionsSubScreen {
    public PartialIdAutocompleteConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("partial_id_autocomplete.config.title"));
    }

    @Override
    protected void addOptions() {
        if(list != null) {
            final var config = PartialIdAutocomplete.config;
            list.addBig(new OptionInstance<>(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_SEGMENT_SEPARATOR_REGEX_NAME),
                    value -> Tooltip.create(Component.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_SEGMENT_SEPARATOR_REGEX_NAME) + ".description")),
                    (option, value) -> Component.literal(value),
                    SimpleOptionStringCallbacks.INSTANCE,
                    config.getIdSegmentSeparatorRegex(),
                    config::setIdSegmentSeparatorRegex
            ));
            list.addBig(new OptionInstance<>(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_VALIDATOR_REGEX),
                    value -> Tooltip.create(Component.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.ID_VALIDATOR_REGEX) + ".description")),
                    (option, value) -> Component.literal(value),
                    SimpleOptionStringCallbacks.INSTANCE,
                    config.getIdValidatorRegex(),
                    config::setIdValidatorRegex
            ));
            list.addBig(OptionInstance.createBoolean(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.COLLAPSE_SINGLE_CHILD_NODES_NAME),
                    value -> Tooltip.create(Component.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.COLLAPSE_SINGLE_CHILD_NODES_NAME) + ".description")),
                    config.getCollapseSingleChildNodes(),
                    config::setCollapseSingleChildNodes
            ));
            list.addBig(OptionInstance.createBoolean(
                    convertNameToTranslationKey(PartialIdAutocompleteConfig.ONLY_SUGGEST_NEXT_SEGMENTS_NAME),
                    value -> Tooltip.create(Component.translatable(convertNameToTranslationKey(PartialIdAutocompleteConfig.ONLY_SUGGEST_NEXT_SEGMENTS_NAME) + ".description")),
                    config.getOnlySuggestNextSegments(),
                    config::setOnlySuggestNextSegments
            ));
        }
    }

    private static String convertNameToTranslationKey(String name) {
        return "partial_id_autocomplete.config." + name.replace("-", "_");
    }
}
