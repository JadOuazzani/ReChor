package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable index of stop names that supports text-based search with partial and
 * accent-/case-insensitive matching. It handles both primary and alternative names,
 * returning a list of matching primary stop names sorted by relevance with ties
 * broken by the original ordering of the searchable entries (primary and aliases).
 *
 * <p>Ties on score use the position of the first matching occurrence among both
 * primary and alternative names, in the order they were provided to the constructor.</p>
 *
 * <p>If the query is empty or only whitespace, returns the list of all primary stop names
 * in their original order, limited by maxResults.</p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class StopIndex {

    private static final Map<Character, String> ACCENT_MAP = Map.ofEntries(
            Map.entry('a', "[aáàâä]"),
            Map.entry('e', "[eéèêë]"),
            Map.entry('i', "[iíìîï]"),
            Map.entry('o', "[oóòôö]"),
            Map.entry('u', "[uúùûü]"),
            Map.entry('c', "[cç]")
    );

    private final List<StopInfo> allSearchableStops;

    /**
     * Constructs a new StopIndex.
     *
     * @param stops    the list of primary stop names; must not be null
     * @param altNames a map from alternative stop names to their corresponding primary names; must not be null
     * @throws NullPointerException if stops or altNames is null
     */
    public StopIndex(List<String> stops, Map<String, String> altNames) {
        Objects.requireNonNull(stops);
        Objects.requireNonNull(altNames);

        Stream<StopInfo> mainStops = stops.stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(s -> new StopInfo(s, s));

        Stream<StopInfo> altStops = altNames.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isEmpty()
                        && e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> new StopInfo(e.getKey(), e.getValue()));

        this.allSearchableStops = Stream.concat(mainStops, altStops)
                .toList();
    }

    private static Pattern compileSubQueryToPattern(String subQuery) {
        String regex = subQuery.chars()
                .mapToObj(cp -> {
                    char c = (char) cp;
                    return (Character.isLowerCase(c) && ACCENT_MAP.containsKey(c))
                            ? ACCENT_MAP.get(c)
                            : Pattern.quote(String.valueOf(c));
                })
                .collect(Collectors.joining());

        boolean queryIsLowerCase = subQuery.chars().noneMatch(Character::isUpperCase);
        int flags = Pattern.UNICODE_CASE;
        if (queryIsLowerCase) flags |= Pattern.CASE_INSENSITIVE;
        return Pattern.compile(regex, flags);
    }

    private int calculateScore(String stopName, List<Pattern> patterns) {
        if (stopName.isEmpty()) return 0;
        if (patterns.stream().anyMatch(p -> !p.matcher(stopName).find())) return 0;

        return patterns.stream().mapToInt(p -> {
            Matcher m = p.matcher(stopName);
            m.find();
            int start = m.start();
            int end = m.end();
            int len = end - start;

            int base = (int) Math.floor(len * 100.0 / stopName.length());
            int mult = 1;
            if (start == 0 || !Character.isLetter(stopName.charAt(start - 1))) mult *= 4;
            if (end == stopName.length() || !Character.isLetter(stopName.charAt(end))) mult *= 2;
            return base * mult;
        }).sum();
    }

    /**
     * Returns a list of primary stop names matching the given textual query,
     * sorted by descending relevance score, then by the original ordering of
     * searchable entries in case of ties. If the query is empty or whitespace,
     * returns all primary stop names in their original order.
     *
     * @param query      the search query (may be null or empty)
     * @param maxResults the maximum number of results to return
     * @return a list of matching primary stop names
     */
    public List<String> stopsMatching(String query, int maxResults) {
        String trimmed = query == null ? "" : query.trim();
        // if no query, return all primary names in original order
        if (trimmed.isEmpty()) {
            return allSearchableStops.stream()
                    .filter(si -> si.searchableName.equals(si.primaryName))
                    .map(si -> si.primaryName)
                    .distinct()
                    .limit(maxResults)
                    .toList();
        }

        List<Pattern> patterns = Arrays.stream(trimmed.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(StopIndex::compileSubQueryToPattern)
                .toList();

        // Map primaryName -> best (score, position)
        Map<String, ScorePos> best = new HashMap<>();

        IntStream.range(0, allSearchableStops.size()).forEach(i -> {
            StopInfo si = allSearchableStops.get(i);
            if (patterns.stream().allMatch(p -> p.matcher(si.searchableName).find())) {
                int score = calculateScore(si.searchableName, patterns);
                if (score > 0) {
                    best.merge(
                            si.primaryName,
                            new ScorePos(score, i),
                            (old, ne) -> {
                                if (ne.score > old.score) return ne;
                                if (ne.score == old.score && ne.pos < old.pos) return ne;
                                return old;
                            }
                    );
                }
            }
        });

        return best.entrySet().stream()
                .sorted((e1, e2) -> {
                    ScorePos a = e1.getValue(), b = e2.getValue();
                    int cmp = Integer.compare(b.score, a.score);
                    if (cmp != 0) return cmp;
                    return Integer.compare(a.pos, b.pos);
                })
                .map(Map.Entry::getKey)
                .limit(maxResults)
                .toList();
    }

    private static final class ScorePos {
        final int score;
        final int pos;
        ScorePos(int score, int pos) { this.score = score; this.pos = pos; }
    }

    private record StopInfo(String searchableName, String primaryName) {}
}