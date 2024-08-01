package net.papierkorb2292.partial_id_autocomplete.client.mixin;

import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;
import net.papierkorb2292.partial_id_autocomplete.client.PartialIdGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CommandSource.class)
public interface CommandSourceMixin {

    @ModifyVariable(
            method = {
                    "suggestIdentifiers(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
                    "suggestIdentifiers(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;"
            },
            at = @At("HEAD"),
            argsOnly = true
    )
    private static Iterable<Identifier> partialIdAutocomplete$addPartialIdSuggestions(Iterable<Identifier> ids) {
        return new PartialIdGenerator(ids);
    }
}
