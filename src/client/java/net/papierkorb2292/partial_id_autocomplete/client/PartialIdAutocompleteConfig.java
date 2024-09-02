package net.papierkorb2292.partial_id_autocomplete.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Properties;

public class PartialIdAutocompleteConfig {
    public static final Path DEFAULT_CONFIG_PATH = Path.of("config/partial_id_autocomplete.properties");

    private static final String ID_SEGMENT_SEPARATOR_REGEX_NAME = "id-segment-separator-regex";
    private static final String COLLAPSE_SINGLE_CHILD_NODES_NAME = "collapse-single-child-nodes";
    private static final String ONLY_SUGGEST_NEXT_SEGMENTS_NAME = "only-suggest-next-segments";
    private static final Properties configDefaults = new Properties();
    static {
        configDefaults.setProperty(ID_SEGMENT_SEPARATOR_REGEX_NAME, "[/:.]");
        configDefaults.setProperty(COLLAPSE_SINGLE_CHILD_NODES_NAME, "true");
        configDefaults.setProperty(ONLY_SUGGEST_NEXT_SEGMENTS_NAME, "true");
    }

    private String idSegmentSeparatorRegex;
    private boolean collapseSingleChildNodes;
    private boolean onlySuggestNextSegments;

    private String modVersion;

    private final Path configPath;

    private PartialIdAutocompleteConfig(Properties properties, Path configPath, @Nullable String configVersion, String modVersion) {
        idSegmentSeparatorRegex = properties.getProperty(ID_SEGMENT_SEPARATOR_REGEX_NAME);
        collapseSingleChildNodes = Boolean.parseBoolean(properties.getProperty(COLLAPSE_SINGLE_CHILD_NODES_NAME));
        onlySuggestNextSegments = Boolean.parseBoolean(properties.getProperty(ONLY_SUGGEST_NEXT_SEGMENTS_NAME));
        this.configPath = configPath;
        this.modVersion = modVersion;
        if(configVersion != null && !configVersion.equals(modVersion)) {
            saveToFile(configPath);
        }
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

    public boolean getOnlySuggestNextSegments() {
        return onlySuggestNextSegments;
    }
    public void setOnlySuggestNextSegments(boolean onlySuggestNextSegments) {
        this.onlySuggestNextSegments = onlySuggestNextSegments;
        saveToFile(configPath);
    }

    public SimpleOption<?>[] asOptions() {
        return new SimpleOption<?>[] {
            new SimpleOption<>(
                    convertNameToTranslationKey(ID_SEGMENT_SEPARATOR_REGEX_NAME),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(ID_SEGMENT_SEPARATOR_REGEX_NAME) + ".description")),
                    (option, value) -> Text.literal(value),
                    SimpleOptionStringCallbacks.INSTANCE,
                    getIdSegmentSeparatorRegex(),
                    this::setIdSegmentSeparatorRegex
            ),
            SimpleOption.ofBoolean(
                    convertNameToTranslationKey(COLLAPSE_SINGLE_CHILD_NODES_NAME),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(COLLAPSE_SINGLE_CHILD_NODES_NAME) + ".description")),
                    getCollapseSingleChildNodes(),
                    this::setCollapseSingleChildNodes
            ),
            SimpleOption.ofBoolean(
                    convertNameToTranslationKey(ONLY_SUGGEST_NEXT_SEGMENTS_NAME),
                    value -> Tooltip.of(Text.translatable(convertNameToTranslationKey(ONLY_SUGGEST_NEXT_SEGMENTS_NAME) + ".description")),
                    getOnlySuggestNextSegments(),
                    this::setOnlySuggestNextSegments
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
        properties.setProperty(ONLY_SUGGEST_NEXT_SEGMENTS_NAME, Boolean.toString(onlySuggestNextSegments));
        try(var writer = new java.io.FileWriter(path.toFile())) {
            writer.append('v').append(modVersion).append('\n');
            properties.store(writer, "Partial ID Autocomplete Config");
        } catch (java.io.IOException e) {
            PartialIdAutocomplete.LOGGER.error("Failed to save config file", e);
        }
    }

    public static PartialIdAutocompleteConfig loadFromFile(Path path, String modVersion) {
        final var file = path.toFile();
        if(!file.exists()) {
            final var config = new PartialIdAutocompleteConfig(configDefaults, path, null, modVersion);
            config.saveToFile(path);
            return config;
        }
        try(var reader = new BufferedReader(new FileReader(file))) {
            reader.mark(16);
            final var firstChar = reader.read();
            final String version;
            if(firstChar == 'v') {
                //Read version info
                version = reader.readLine();
            } else {
                version = "1.0.0";
                reader.reset();
            }
            final var properties = new Properties(configDefaults);
            properties.load(reader);
            return new PartialIdAutocompleteConfig(properties, path, version, modVersion);
        } catch (java.io.IOException e) {
            PartialIdAutocomplete.LOGGER.error("Failed to load config file", e);
            return new PartialIdAutocompleteConfig(configDefaults, path, null, modVersion);
        }
    }
}
