# Partial ID Autocomplete Minecraft Mod

This Minecraft Fabric mod extends the autocompletion for id arguments in commands
for completing ids only up to a certain segment.

For example, if you have many functions under `namespace:folder_name/`, you can now
complete only the `namespace:folder_name/` part instead of having to directly complete the
full function id.

## Download

You can download this mod on Modrinth (https://modrinth.com/mod/partialidautocomplete),
Curseforge (https://www.curseforge.com/minecraft/mc-mods/partialidautocomplete)
or from the Github Release (https://github.com/Papierkorb2292/PartialIdAutocomplete/releases/latest)

Alternatively, you can build the mod yourself by downloading this repository
and running `./gradlew build` in the root directory. The built mod will appear in `build/libs`.

## Config

The mod has a config file located at `config/partial_id_autocomple.properties` in your Minecraft
directory. The following config options are available, which can also be accessed through Mod Menu:
- `id-segment-separator-regex`: The regex that is used to split an id into its segments.
- `collapse-single-child-nodes`: If enabled, nodes with only one child will not be suggested and their child will be suggested instead (even when "Only Suggest Next Segments" is enabled)
- `only-suggest-next-segments`: If enabled, only the next segment of the id will be suggested (based on the segments that have already been entered). If "Collapse Single Child Nodes" is enabled as well, deeper segments are also suggested, in case they are the only child of their parent.
- `id-validator-regex`: The regex that is used to determine whether a suggestion is an id. Partial ids will only be suggested if all suggestions match this regex

## License

This mod is available under the CC0 license.
