package net.papierkorb2292.partial_id_autocomplete.client.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.papierkorb2292.partial_id_autocomplete.IsPartialIdSuggestionContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Suggestion.class)
public class SuggestionMixin implements IsPartialIdSuggestionContainer {
    @Unique
    private boolean partial_id_autocomplete$isPartialIdSuggestion = false;

    public boolean partial_id_autocomplete$isPartialIdSuggestion() {
        return partial_id_autocomplete$isPartialIdSuggestion;
    }

    public void partial_id_autocomplete$setIsPartialIdSuggestion(boolean isPartialIdSuggestion) {
        partial_id_autocomplete$isPartialIdSuggestion = isPartialIdSuggestion;
    }
}
