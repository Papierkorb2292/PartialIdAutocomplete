package net.papierkorb2292.partial_id_autocomplete.client;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public final class PartialIdGenerator implements Iterable<Identifier> {

    private final Iterable<Identifier> originalIds;

    public PartialIdGenerator(Iterable<Identifier> originalIds) {
        this.originalIds = originalIds;
    }

    private Iterable<Identifier> getPotentialPartialIds(Identifier id) {
        return () -> new Iterator<>() {
            private final String[] parts = id.getPath().split("/");
            private int partIndex = 0;

            @Override
            public boolean hasNext() {
                return partIndex <= parts.length;
            }

            @Override
            public Identifier next() {
                final var isOnlyNamespace = partIndex == 0;
                final var joinedSegments = Arrays.stream(parts)
                        .limit(partIndex++)
                        .collect(Collectors.joining("/"));
                if (isOnlyNamespace) {
                    return Identifier.of(
                            id.getNamespace(),
                            joinedSegments
                    );
                }
                return Identifier.of(
                        id.getNamespace(),
                        joinedSegments + '/'
                );
            }
        };
    }

    @NotNull
    @Override
    public Iterator<Identifier> iterator() {
        final Set<Identifier> potentialPartialIds = new HashSet<>();
        final Set<Identifier> queuedPartialIds = new HashSet<>();
        final var originalIdsIterator = originalIds.iterator(); 
        return new Iterator<>() {
            @Nullable
            private Iterator<Identifier> partialIdSuggestions = null;
            
            private Iterator<Identifier> getOrCreatePartialIdSuggestions() {
                if(partialIdSuggestions == null)
                    partialIdSuggestions = queuedPartialIds.iterator();
                return partialIdSuggestions;
            }
            
            @Override
            public boolean hasNext() {
                if(originalIdsIterator.hasNext()) {
                    return true;
                }
                return getOrCreatePartialIdSuggestions().hasNext();
            }

            @Override
            public Identifier next() {
                if(originalIdsIterator.hasNext()) {
                    final var id = originalIdsIterator.next();
                    for(final var partialId : getPotentialPartialIds(id)) {
                        // A partial id needs to appear twice to be suggested, otherwise there's no
                        // point in partial completion when only one id uses the path
                        if(!potentialPartialIds.add(partialId)) {
                            queuedPartialIds.add(partialId);
                        }
                    }
                    return id;
                }
                return getOrCreatePartialIdSuggestions().next();
            }
        };
    }
}
