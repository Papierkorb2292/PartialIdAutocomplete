package net.papierkorb2292.partial_id_autocomplete.client.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.papierkorb2292.partial_id_autocomplete.IsPartialIdSuggestionContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionWindowMixin {

    @Shadow @Final private List<Suggestion> suggestionList;
    @Shadow private int current;
    @Shadow @Final
    CommandSuggestions this$0;

    @Inject(
            method = "useSuggestion",
            at = @At("TAIL")
    )
    private void partial_id_autocomplete$refreshSuggestionOnPartialCompletion(CallbackInfo ci) {
        if(((IsPartialIdSuggestionContainer) suggestionList.get(current)).partial_id_autocomplete$isPartialIdSuggestion()) {
            this$0.updateCommandInfo();
        }
    }
}
