package net.papierkorb2292.partial_id_autocomplete;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.datafixers.util.Either;
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
        final var segmentNames = suggestion.splitWithDelimiters(PartialIdAutocomplete.config.getIdSegmentSeparatorRegex(), -1);
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
        return getCompleteSuggestions(currentInput, false);
    }

    public Suggestions getCompleteSuggestions(String currentInput, boolean sortOnAllSegments) {
        final var completeSuggestionList = new ArrayList<>(originalSuggestions.getList());
        if(PartialIdAutocomplete.config.getOnlySuggestNextSegments()) {
            final var partialSuggestions = getPartialIdsOnlyNextSegment(currentInput);
            markAsPartialIdSuggestions(partialSuggestions);
            completeSuggestionList.addAll(0, partialSuggestions);
        } else {
            final var partialSuggestions = getPartialIdsAllSegments(currentInput);
            markAsPartialIdSuggestions(partialSuggestions);
            if(sortOnAllSegments)
                addSuggestionsSorted(completeSuggestionList, partialSuggestions);
            else
                completeSuggestionList.addAll(0, partialSuggestions);
        }
        return new Suggestions(originalSuggestions.getRange(), completeSuggestionList);
    }

    private void markAsPartialIdSuggestions(Collection<Suggestion> suggestions) {
        for(final var partialSuggestion : suggestions) {
            markAsPartialIdSuggestion(partialSuggestion);
        }
    }

    private void markAsPartialIdSuggestion(Suggestion suggestion) {
        ((IsPartialIdSuggestionContainer)suggestion).partial_id_autocomplete$setIsPartialIdSuggestion(true);
    }

    private boolean shouldSuggestionHidePotentialChild(Suggestion suggestion, Suggestion potentialChild, String currentInput) {
        return potentialChild.getText().startsWith(suggestion.getText()) && currentInput.length() < suggestion.getText().length();
    }

    private Collection<Suggestion> getPartialIdsOnlyNextSegment(String currentInput) {
        final var suggestionsList = originalSuggestions.getList();
        if(suggestionsList.size() <= 1)
            return Collections.emptyList();

        final var noNamespaceInput = !currentInput.contains(":");

        final var inputSegmentCount = Math.max(
                1,
                Math.ceilDiv(
                        currentInput.splitWithDelimiters(PartialIdAutocomplete.config.getIdSegmentSeparatorRegex(), 0).length + 1,
                        2
                )
        );

        final List<List<String>> potentialPartialIdsList = new ArrayList<>();
        final var onlyChildMapper = new OnlyChildMapper();

        var lastParent = -1;

        for(int i = 0; i < suggestionsList.size(); i++) {
            final var idSuggestion = suggestionsList.get(i);

            // Hide suggestion when its text starts with the text of another suggestion
            // Even though the suggestion might not be a proper child (separated by idSegmentSeparatorRegex), it should still be hidden in that case,
            // such that it is not suggested before the shorter suggestion.
            // Also mark the (improper) parent as partial id suggestion, such that when the player decides to complete it, the next child nodes are suggested.

            // Since the suggestions are sorted, it is not necessary to go through all of them. If any suggestions hide the current suggestion,
            // those include the last parent or the previous suggestion.
            if(lastParent != -1 && shouldSuggestionHidePotentialChild(suggestionsList.get(lastParent), idSuggestion, currentInput)) {
                // Was already marked as partial id suggestion
                continue;
            }
            if(i > 0 && shouldSuggestionHidePotentialChild(suggestionsList.get(i - 1), idSuggestion, currentInput)) {
                markAsPartialIdSuggestion(suggestionsList.get(i - 1));
                lastParent = i - 1;
                continue;
            }
            final var potentialPartialIds = getPotentialPartialIds(idSuggestion.getText());
            potentialPartialIdsList.add(potentialPartialIds);

            if(PartialIdAutocomplete.config.getCollapseSingleChildNodes()) {
                onlyChildMapper.addPotentialPartialIds(potentialPartialIds);
            }
        }

        final var result = new LinkedHashSet<Suggestion>();
        for(final var potentialPartialIds : potentialPartialIdsList) {
            final var isDefaultNamespace = !potentialPartialIds.isEmpty() && potentialPartialIds.getFirst().equals("minecraft:");
            // Used to skip over the `minecraft:` suggestion when the input omits the default `minecraft` namespace
            final var segmentCountOffset = isDefaultNamespace && noNamespaceInput ? 1 : 0;
            if(potentialPartialIds.size() < inputSegmentCount + segmentCountOffset)
                continue;
            final var potentialPartialId = potentialPartialIds.get(inputSegmentCount - 1 + segmentCountOffset);
            final var onlyChildMapperResolved = onlyChildMapper.getOnlyChildOrSelf(potentialPartialId);
            if(onlyChildMapperResolved == null) continue;
            result.add(new Suggestion(
                    originalSuggestions.getRange(),
                    onlyChildMapperResolved
            ));
        }
        return result;
    }

    private Collection<Suggestion> getPartialIdsAllSegments(String currentInput) {
        final var partialIds = new LinkedHashSet<String>();
        final var onlyChildMapper = new OnlyChildMapper();
        for(final var idSuggestion : originalSuggestions.getList()) {
            final var potentialPartialIds = getPotentialPartialIds(idSuggestion.getText());
            if(PartialIdAutocomplete.config.getCollapseSingleChildNodes()) {
                onlyChildMapper.addPotentialPartialIds(potentialPartialIds);
            }
            for(final var potentialPartialId : potentialPartialIds) {
                // Don't suggest parent segments
                if(currentInput.startsWith(potentialPartialId)
                        // Even when the input omits the default `minecraft` namespace
                        || potentialPartialId.startsWith("minecraft:") && currentInput.startsWith(potentialPartialId.substring("minecraft:".length()))
                )
                    continue;
                partialIds.add(potentialPartialId);
            }
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

    private void addSuggestionsSorted(List<Suggestion> dest, Collection<Suggestion> src) {
        var i = 0;
        for(final var suggestion : src) {
            while(i <= dest.size() && dest.get(i).compareToIgnoreCase(suggestion) < 0)
                i++;
            dest.add(i, suggestion);
        }
    }

    public static boolean areSuggestionsIds(Suggestions suggestions) {
        return suggestions.getList().stream().allMatch(suggestion ->
               suggestion.getText().matches(PartialIdAutocomplete.config.getIdValidatorRegex())
        );
    }

    private static class OnlyChildMapper {
        private final Map<String, Either<ParentState, String>> onlyChildMap = new HashMap<>();

        public void addPotentialPartialIds(List<String> potentialPartialIds) {
            if(potentialPartialIds.isEmpty())
                return;
            final var last = potentialPartialIds.getLast();
            if(!onlyChildMap.containsKey(last))
                onlyChildMap.put(last, Either.left(ParentState.HIDE));
            else {
                onlyChildMap.put(last, Either.left(ParentState.SHOW));
                return;
            }
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
