package net.papierkorb2292.partial_id_autocomplete.client.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.papierkorb2292.partial_id_autocomplete.IsPartialIdSuggestionContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public class SuggestionWindowMixin {

    @Shadow @Final private List<Suggestion> suggestions;
    @Shadow private int selection;
    @Shadow @Final ChatInputSuggestor field_21615;

    @Inject(
            method = "complete",
            at = @At("TAIL")
    )
    private void partial_id_autocomplete$refreshSuggestionOnPartialCompletion(CallbackInfo ci) {
        if(((IsPartialIdSuggestionContainer)suggestions.get(selection)).partial_id_autocomplete$isPartialIdSuggestion()) {
            field_21615.refresh();
        }
    }
}
