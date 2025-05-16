package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Représente un index de noms d'arrêts permettant d'effectuer des recherches
 * basées sur une requête textuelle. L'index gère les noms principaux et
 * alternatifs, et retourne les noms principaux correspondants triés par pertinence.
 * La classe est immuable.
 */
public final class StopIndex {

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
     * Construit un nouvel index d'arrêts.
     *
     * @param stops    La liste des noms d'arrêts principaux.
     * @param altNames La table associant des noms alternatifs à leur nom principal.
     * @throws NullPointerException si stops ou altNames est nul.
     */
    public StopIndex(List<String> stops, Map<String, String> altNames) {
        Stream<StopInfo> mainStops = stops.stream().filter(s -> (s != null && !s.isEmpty())).map(s -> new StopInfo(s, s));
        Stream<StopInfo> altStops = altNames.entrySet().stream().filter(e -> !(e.getKey() == null && e.getKey().isEmpty()) && !(e.getValue() == null && e.getValue().isEmpty())).map(e -> new StopInfo(e.getKey(), e.getValue()));
        this.allSearchableStops = Stream.concat(mainStops, altStops).toList();
        this.sortedMainStops = new ArrayList<>(new TreeSet<>(stops));
    }

    /**
     * Compile une sous-requête en Pattern Java, en respectant les règles de sensibilité à la casse et d'équivalence
     * des accents spécifiées.
     *
     * @param subQuery La sous-requête originale.
     * @return Le Pattern compilé.
     */
    private static Pattern compileSubQueryToPattern(String subQuery) {
        String regex = subQuery.chars().mapToObj(cp -> {
            char c = (char) cp;
            return (Character.isLowerCase(c) && ACCENT_MAP.containsKey(c)) ? ACCENT_MAP.get(c) : Pattern.quote(String.valueOf(c));
        }).collect(Collectors.joining());

        boolean queryIsLowerCase = subQuery.chars().noneMatch(Character::isUpperCase);
        int flags = Pattern.UNICODE_CASE;
        return Pattern.compile(regex, queryIsLowerCase ? flags |= Pattern.CASE_INSENSITIVE : flags);
    }

    /**
     * Calcule le score de pertinence d'un nom d'arrêt pour une requête donnée.
     *
     * @param stopName   Le nom de l'arrêt (original).
     * @param patterns   La liste des Patterns compilés correspondant aux subQueries.
     * @return Le score total de pertinence.
     */
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

            if ((start == 0 || !Character.isLetter(stopName.charAt(start - 1)))) {
                mult *= 4;
            }

            if ((end == stopName.length() || !Character.isLetter(stopName.charAt(end)))) {
                mult *= 2;
            }
            return base * mult;

        }).sum();
    }

    /**
     * Recherche les arrêts correspondant à une requête donnée.
     *
     * @param query      La requête de recherche.
     * @param maxResults Le nombre maximum de résultats à retourner.
     * @return Une liste des noms d'arrêts principaux correspondants,
     * triée par pertinence décroissante, sans doublons, et limitée à maxResults.
     */
    public List<String> stopsMatching(String query, int maxResults) {
        String trimmed = query == null ? "" : query;
        List<Pattern> patterns = Arrays.stream(trimmed.split("\\s+")).filter(s -> !s.isEmpty()).map(StopIndex::compileSubQueryToPattern).toList();
        Map<String, Integer> scores = allSearchableStops.stream()
                .filter(s -> patterns.stream().allMatch(p -> p.matcher(s.searchableName).find()))
                .collect(Collectors.toMap(StopInfo::primaryName, si -> calculateScore(si.searchableName(), patterns), Integer::max));
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .limit(maxResults)
                .toList();
    }

    private record StopInfo(String searchableName, String primaryName) {}
}