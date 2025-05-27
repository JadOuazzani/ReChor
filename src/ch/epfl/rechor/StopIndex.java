package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable index of stop names that supports text-based search with partial and
 * accent-/case-insensitive matching.  It handles both primary and alternative names,
 * returning a list of matching primary stop names sorted by relevance.
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class StopIndex {

    /**
     * Maps each base character to a regular expression character class that matches
     * itself and all its accented variants.
     */
    private static final Map<Character, String> ACCENT_MAP = Map.ofEntries(
            Map.entry('a', "[aáàâä]"),
            Map.entry('e', "[eéèêë]"),
            Map.entry('i', "[iíìîï]"),
            Map.entry('o', "[oóòôö]"),
            Map.entry('u', "[uúùûü]"),
            Map.entry('c', "[cç]"));

    private final List<StopInfo> allSearchableStops;
    private final List<String> sortedMainStops;

    /**
     * Constructs a new {@code StopIndex}.
     *
     * @param stops    the list of primary stop names; must not be null
     * @param altNames a map from alternative stop names to their corresponding primary names; must not be null
     * @throws NullPointerException if {@code stops} or {@code altNames} is null
     */
    public StopIndex(List<String> stops, Map<String, String> altNames) {
        Stream<StopInfo> mainStops = stops.stream()
                .filter(s -> (s != null && !s.isEmpty()))
                .map(s -> new StopInfo(s, s));
        Stream<StopInfo> altStops = altNames.entrySet().stream()
                .filter(e -> !(e.getKey() == null && e.getKey().isEmpty())
                        && !(e.getValue() == null && e.getValue().isEmpty()))
                .map(e -> new StopInfo(e.getKey(), e.getValue()));
        this.allSearchableStops = Stream.concat(mainStops, altStops).toList();
        this.sortedMainStops = new ArrayList<>(new TreeSet<>(stops));
    }

    /**
     * Compiles a single sub-query string into a {@link Pattern}, applying
     * accent‐equivalence mappings and optional case‐insensitivity.
     *
     * <p>If the sub-query contains only lowercase letters, the resulting pattern
     * will ignore case differences.  Each unaccented letter is replaced by a
     * character class matching it and its accented variants.</p>
     *
     * @param subQuery the raw sub-query text
     * @return a compiled {@code Pattern} for matching that sub-query
     */
    private static Pattern compileSubQueryToPattern(String subQuery) {
        String regex = subQuery.chars().mapToObj(cp -> {
            char c = (char) cp;
            return (Character.isLowerCase(c) && ACCENT_MAP.containsKey(c))
                    ? ACCENT_MAP.get(c)
                    : Pattern.quote(String.valueOf(c));
        }).collect(Collectors.joining());

        boolean queryIsLowerCase = subQuery.chars().noneMatch(Character::isUpperCase);
        int flags = Pattern.UNICODE_CASE;
        if (queryIsLowerCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        return Pattern.compile(regex, flags);
    }

    /**
     * Calculates the relevance score of a stop name against the provided list of
     * sub-query patterns.  Returns zero if any pattern does not match.
     *
     * <p>For each matching pattern, the base score is the integer percentage of
     * stopName length covered by the match.  This base is multiplied by 4 if the
     * match is at the start of a word, and by 2 if at the end of a word, then
     * summed across all patterns.</p>
     *
     * @param stopName the stop name to score
     * @param patterns the compiled sub-query patterns
     * @return the total relevance score, or 0 if {@code stopName} is empty or any pattern fails to match
     */
    private int calculateScore(String stopName, List<Pattern> patterns) {
        if (stopName.isEmpty()) return 0;
        if (patterns.stream().anyMatch(p -> !p.matcher(stopName).find())) return 0;

        return patterns.stream().mapToInt(p -> {
            Matcher m = p.matcher(stopName);
            m.find();
            int start = m.start();
            int end   = m.end();
            int len   = end - start;

            int base = (int) Math.floor(len * 100.0 / stopName.length());
            int mult = 1;
            if (start == 0 || !Character.isLetter(stopName.charAt(start - 1))) {
                mult *= 4;
            }
            if (end == stopName.length() || !Character.isLetter(stopName.charAt(end))) {
                mult *= 2;
            }
            return base * mult;
        }).sum();
    }

    /**
     * Returns a list of primary stop names matching the given textual query,
     * sorted by descending relevance score, then alphabetically, limited to
     * {@code maxResults} entries, with no duplicates.
     *
     * <p>The query is split on whitespace into sub-queries; each sub-query is
     * matched with accent and case rules as defined in
     * {@link #compileSubQueryToPattern(String)}.</p>
     *
     * @param query      the search query (may be null or empty)
     * @param maxResults the maximum number of results to return
     * @return a list of matching primary stop names, sorted by relevance then name
     */
    public List<String> stopsMatching(String query, int maxResults) {
        String trimmed = query == null ? "" : query;
        List<Pattern> patterns = Arrays.stream(trimmed.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .map(StopIndex::compileSubQueryToPattern)
                .toList();
        Map<String, Integer> scores = allSearchableStops.stream()
                .filter(s -> patterns.stream().allMatch(p -> p.matcher(s.searchableName).find()))
                .collect(Collectors.toMap(
                        StopInfo::primaryName,
                        si -> calculateScore(si.searchableName(), patterns),
                        Integer::max));

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .limit(maxResults)
                .toList();
    }

    /**
     * Helper record associating a searchable name (either primary or alternative)
     * with its canonical primary name.
     *
     * @param searchableName the name used for matching
     * @param primaryName    the canonical primary stop name
     */
    private record StopInfo(String searchableName, String primaryName) {}
}
