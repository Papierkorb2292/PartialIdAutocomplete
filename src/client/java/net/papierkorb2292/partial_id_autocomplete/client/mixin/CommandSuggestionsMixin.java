package net.papierkorb2292.partial_id_autocomplete.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.papierkorb2292.partial_id_autocomplete.PartialIdGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {

    @Shadow @Final
    EditBox input;

    @ModifyExpressionValue(
            method = "updateCommandInfo",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;",
                    remap = false
            )
    )
    private CompletableFuture<Suggestions> partial_id_autocomplete$addPartialIdSuggestions(CompletableFuture<Suggestions> suggestionsFuture) {
        return suggestionsFuture.thenApply(suggestions -> {
            if(!PartialIdGenerator.areSuggestionsIds(suggestions))
                return suggestions;
            return new PartialIdGenerator(suggestions).getCompleteSuggestions(input.getValue().substring(suggestions.getRange().getStart()), true);
        });
    }
}
