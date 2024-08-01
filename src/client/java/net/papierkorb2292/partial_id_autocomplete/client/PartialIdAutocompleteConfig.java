package net.papierkorb2292.partial_id_autocomplete.client;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.Properties;

public class PartialIdAutocompleteConfig {
    public static final Path DEFAULT_CONFIG_PATH = Path.of("config/partial_id_autocomplete.properties");

    private static final String ID_SEGMENT_SEPARATOR_REGEX_NAME = "id-segment-separator-regex";
    private static final String COLLAPSE_SINGLE_CHILD_NODES_NAME = "collapse-single-child-nodes";
    private static final String ONLY_SUGGEST_NEXT_SIGNIFICANT_SEGMENTS_NAME = "only-suggest-next-significant-segments";
    private static final Properties configDefaults = new Properties();
    static {
        configDefaults.setProperty(ID_SEGMENT_SEPARATOR_REGEX_NAME, "[/:.]");
        configDefaults.setProperty(COLLAPSE_SINGLE_CHILD_NODES_NAME, "true");
        configDefaults.setProperty(ONLY_SUGGEST_NEXT_SIGNIFICANT_SEGMENTS_NAME, "true");
    }

    private String idSegmentSeparatorRegex;
    private boolean collapseSingleChildNodes;
    private boolean onlySuggestNextSignificantSegments;

    private final Path configPath;

    private PartialIdAutocompleteConfig(Properties properties, Path configPath) {
        idSegmentSeparatorRegex = properties.getProperty(ID_SEGMENT_SEPARATOR_REGEX_NAME);
        collapseSingleChildNodes = Boolean.parseBoolean(properties.getProperty(COLLAPSE_SINGLE_CHILD_NODES_NAME));
        onlySuggestNextSignificantSegments = Boolean.parseBoolean(properties.getProperty(ONLY_SUGGEST_NEXT_SIGNIFICANT_SEGMENTS_NAME));
        this.configPath = configPath;
    }

    public String getIdSegmentSeparatorRegex() {
        return idSegmentSeparatorRegex;
    }
    public void setIdSegmentSeparatorRegex(String idSegmentSeparatorRegex) {
        this.idSegmentSeparatorRegex = idSegmentSeparatorRegex;
        saveToFile(configPath);
    }

    public boolean getCollapseSingleChildNodes() {
        return collapseSingleChildNodes;
    }
    public void setCollapseSingleChildNodes(boolean collapseSingleChildNodes) {
        this.collapseSingleChildNodes = collapseSingleChildNodes;
        saveToFile(configPath);
    }

    public boolean getOnlySuggestNextSignificantSegments() {
        return onlySuggestNextSignificantSegments;
    }
    public void setOnlySuggestNextSignificantSegments(boolean onlySuggestNextSignificantSegments) {
        this.onlySuggestNextSignificantSegments = onlySuggestNextSignificantSegments;
        saveToFile(configPath);
    }

    public SimpleOption<?>[] asOptions() {
        return new SimpleOption<?>[] {
            new SimpleOption<>(
                    convertNameToTranslationKey(ID_SEGMENT_SEPARATOR_REGEX_NAME),
                    SimpleOption.emptyTooltip(),
                    (option, value) -> Text.literal(value),
                    SimpleOptionStringCallbacks.INSTANCE,
                    getIdSegmentSeparatorRegex(),
                    this::setIdSegmentSeparatorRegex
            ),
            SimpleOption.ofBoolean(
                    convertNameToTranslationKey(COLLAPSE_SINGLE_CHILD_NODES_NAME),
                    getCollapseSingleChildNodes(),
                    this::setCollapseSingleChildNodes
            ),
            SimpleOption.ofBoolean(
                    convertNameToTranslationKey(ONLY_SUGGEST_NEXT_SIGNIFICANT_SEGMENTS_NAME),
                    getOnlySuggestNextSignificantSegments(),
                    this::setOnlySuggestNextSignificantSegments
            )
        };
    }

    private static String convertNameToTranslationKey(String name) {
        return "partial_id_autocomplete.config." + name.replace("-", "_");
    }

    public void saveToFile(Path path) {
        final var properties = new Properties();
        properties.setProperty(ID_SEGMENT_SEPARATOR_REGEX_NAME, idSegmentSeparatorRegex);
        properties.setProperty(COLLAPSE_SINGLE_CHILD_NODES_NAME, Boolean.toString(collapseSingleChildNodes));
        properties.setProperty(ONLY_SUGGEST_NEXT_SIGNIFICANT_SEGMENTS_NAME, Boolean.toString(onlySuggestNextSignificantSegments));
        try(var writer = new java.io.FileWriter(path.toFile())) {
            properties.store(writer, "Partial ID Autocomplete Config");
        } catch (java.io.IOException e) {
            PartialIdAutocomplete.LOGGER.error("Failed to save config file", e);
        }
    }

    public static PartialIdAutocompleteConfig loadFromFile(Path path) {
        final var file = path.toFile();
        if(!file.exists())
            return new PartialIdAutocompleteConfig(configDefaults, path);
        try(var reader = new FileReader(file)) {
            final var properties = new Properties(configDefaults);
            properties.load(reader);
            return new PartialIdAutocompleteConfig(properties, path);
        } catch (java.io.IOException e) {
            PartialIdAutocomplete.LOGGER.error("Failed to load config file", e);
            return new PartialIdAutocompleteConfig(configDefaults, path);
        }
    }
}
