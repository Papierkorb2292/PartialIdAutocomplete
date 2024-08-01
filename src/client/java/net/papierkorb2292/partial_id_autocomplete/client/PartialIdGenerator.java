package net.papierkorb2292.partial_id_autocomplete.client;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.datafixers.util.Either;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PartialIdGenerator {

    private final Suggestions originalSuggestions;

    public PartialIdGenerator(Suggestions originalSuggestions) {
        this.originalSuggestions = originalSuggestions;
    }

    private List<String> getPotentialPartialIds(String suggestion) {
        final var segmentNames = suggestion.splitWithDelimiters(PartialIdAutocomplete.config.getIdSegmentSeparatorRegex(), 0);
        final var segmentCount = segmentNames.length / 2 + (segmentNames.length & 1);
        final var parts = new String[segmentCount];
        for (int i = 0; i < segmentCount; i++) {
            final var partIndex = i * 2;
            if(partIndex == segmentNames.length - 1)
                parts[i] = segmentNames[partIndex];
            else
                parts[i] = segmentNames[partIndex] + segmentNames[partIndex + 1];
        }
        return IntStream.range(1, parts.length)
                .mapToObj(i ->
                        Arrays.stream(parts)
                            .limit(i)
                            .collect(Collectors.joining()))
                .collect(Collectors.toList());
    }

    public Suggestions getCompleteSuggestions(String currentInput) {
        final var completeSuggestionList = new ArrayList<>(originalSuggestions.getList());
        final Collection<Suggestion> partialSuggestions;
        if(PartialIdAutocomplete.config.getOnlySuggestNextSegments()) {
            partialSuggestions = getPartialIdsOnlyNextSegment(currentInput);
        } else {
            partialSuggestions = getPartialIdsAllSegments();
        }
        for(final var partialSuggestion : partialSuggestions) {
            ((IsPartialIdSuggestionContainer)partialSuggestion).partial_id_autocomplete$setIsPartialIdSuggestion(true);
        }
        completeSuggestionList.addAll(0, partialSuggestions);
        return new Suggestions(originalSuggestions.getRange(), completeSuggestionList);
    }

    private Collection<Suggestion> getPartialIdsOnlyNextSegment(String currentInput) {
        if(originalSuggestions.getList().size() <= 1)
            return Collections.emptyList();

        final var inputSegmentCount = Math.max(
                1,
                Math.ceilDiv(
                        currentInput.splitWithDelimiters(PartialIdAutocomplete.config.getIdSegmentSeparatorRegex(), 0).length + 1,
                        2
                )
        );

        final List<List<String>> potentialPartialIdsList = new ArrayList<>();
        final var onlyChildMapper = new OnlyChildMapper();

        for(final var idSuggestion : originalSuggestions.getList()) {
            final var potentialPartialIds = getPotentialPartialIds(idSuggestion.getText());
            potentialPartialIdsList.add(potentialPartialIds);

            if(PartialIdAutocomplete.config.getCollapseSingleChildNodes()) {
                onlyChildMapper.addPotentialPartialIds(potentialPartialIds);
            }
        }

        final var result = new LinkedHashSet<Suggestion>();
        for(final var potentialPartialIds : potentialPartialIdsList) {
            if(potentialPartialIds.size() < inputSegmentCount)
                continue;
            final var potentialPartialId = potentialPartialIds.get(inputSegmentCount - 1);
            final var onlyChildMapperResolved = onlyChildMapper.getOnlyChildOrSelf(potentialPartialId);
            if(onlyChildMapperResolved == null) continue;
            result.add(new Suggestion(
                    originalSuggestions.getRange(),
                    onlyChildMapperResolved
            ));
        }
        return result;
    }

    private Collection<Suggestion> getPartialIdsAllSegments() {
        final var partialIds = new LinkedHashSet<String>();
        final var onlyChildMapper = new OnlyChildMapper();
        for(final var idSuggestion : originalSuggestions.getList()) {
            final var potentialPartialIds = getPotentialPartialIds(idSuggestion.getText());
            if(PartialIdAutocomplete.config.getCollapseSingleChildNodes()) {
                onlyChildMapper.addPotentialPartialIds(potentialPartialIds);
            }
            partialIds.addAll(potentialPartialIds);
        }

        final var result = new ArrayList<Suggestion>();
        for(final var partialId : partialIds) {
            var onlyChildMapperResolved = onlyChildMapper.getOnlyChildOrSelf(partialId);
            if(onlyChildMapperResolved == null) continue;
            result.add(new Suggestion(
                    originalSuggestions.getRange(),
                    onlyChildMapperResolved
            ));
        }
        return result;
    }

    public static boolean areSuggestionsIds(Suggestions suggestions) {
        return suggestions.getList().stream().allMatch(suggestion -> {
           final var colonIndex = suggestion.getText().indexOf(':');
           if(colonIndex == -1)
               return false;
           return Identifier.isNamespaceValid(suggestion.getText().substring(0, colonIndex)) && Identifier.isPathValid(suggestion.getText().substring(colonIndex + 1));
        });
    }

    private static class OnlyChildMapper {
        private final Map<String, Either<ParentState, String>> onlyChildMap = new HashMap<>();

        public void addPotentialPartialIds(List<String> potentialPartialIds) {
            final var last = potentialPartialIds.getLast();
            if(!onlyChildMap.containsKey(last))
                onlyChildMap.put(last, Either.left(ParentState.HIDE));
            else
                onlyChildMap.put(last, Either.left(ParentState.SHOW));
            for (int i = potentialPartialIds.size() - 2; i >= 0; i--) {
                final var potentialPartialId = potentialPartialIds.get(i);
                if(onlyChildMap.containsKey(potentialPartialId)) {
                    onlyChildMap.put(potentialPartialId, Either.left(ParentState.SHOW));
                    break;
                }
                onlyChildMap.put(potentialPartialId, Either.right(potentialPartialIds.get(i + 1)));
            }
        }

        @Nullable
        public String getOnlyChildOrSelf(String potentialPartialId) {
            var onlyChild = onlyChildMap.get(potentialPartialId);
            if(onlyChild == null) return potentialPartialId;
            return onlyChild.map(
                    parentState -> {
                        if(parentState == ParentState.HIDE)
                            return null;
                        return potentialPartialId;
                    },
                    this::getOnlyChildOrSelf
            );
        }

        private enum ParentState {
            SHOW, HIDE
        }
    }
}
