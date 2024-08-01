package net.papierkorb2292.partial_id_autocomplete.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.papierkorb2292.partial_id_autocomplete.client.PartialIdGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {

    @Shadow @Final
    TextFieldWidget textField;

    @ModifyExpressionValue(
            method = "refresh",
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
            return new PartialIdGenerator(suggestions).getCompleteSuggestions(textField.getText().substring(suggestions.getRange().getStart()));
        });
    }
}
