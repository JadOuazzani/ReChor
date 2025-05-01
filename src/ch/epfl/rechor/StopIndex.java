package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Représente un index de noms d'arrêts permettant d'effectuer des recherches
 * basées sur une requête textuelle. L'index gère les noms principaux et
 * alternatifs, et retourne les noms principaux correspondants triés par pertinence.
 * La classe est immuable.
 */
public final class StopIndex {

    private record StopInfo(String searchableName, String primaryName) {}

    private final List<StopInfo> allSearchableStops;

    // Map pour la conversion des caractères de base (minuscules, non accentués) en classes regex
    private static final Map<Character, String> ACCENT_MAP = createAccentMap();

    /**
     * Construit un nouvel index d'arrêts.
     *
     * @param stops      La liste des noms d'arrêts principaux.
     * @param altNames   La table associant des noms alternatifs à leur nom principal.
     * @throws NullPointerException si stops ou altNames est nul.
     */
    public StopIndex(List<String> stops, Map<String, String> altNames) {

        List<StopInfo> searchableList = new ArrayList<>();

        for (String stop : stops) {
            if (stop != null && !stop.isEmpty()) {
                // Conserve le nom original pour l'affichage et la recherche exacte
                searchableList.add(new StopInfo(stop, stop));
            }
        }

        for (Map.Entry<String, String> entry : altNames.entrySet()) {
            String altName = entry.getKey();
            String primaryName = entry.getValue();
            if (altName != null && !altName.isEmpty() && primaryName != null && !primaryName.isEmpty()) {
                // Conserve le nom alternatif original pour la recherche
                searchableList.add(new StopInfo(altName, primaryName));
            }
        }

        this.allSearchableStops = List.copyOf(searchableList);
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
        if (query == null || query.isBlank() || maxResults <= 0) {
            return Collections.emptyList();
        }

        // 1. Découper la requête originale en sous-requêtes non vides
        List<String> subQueries = Arrays.stream(query.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .toList();

        if (subQueries.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Compiler les patterns regex pour chaque sous-requête (respectant casse/accents)
        List<Pattern> patterns = subQueries.stream()
                .map(StopIndex::compileSubQueryToPattern)
                .toList();

        // 3. Calculer les scores pour chaque arrêt principal correspondant
        Map<String, Integer> scores = new HashMap<>();

        for (StopInfo stopInfo : allSearchableStops) {
            boolean matchesAll = true;
            // Vérifier si le nom cherchable (original) contient TOUTES les sous-requêtes
            for (Pattern pattern : patterns) {
                if (!pattern.matcher(stopInfo.searchableName()).find()) {
                    matchesAll = false;
                    break;
                }
            }

            if (matchesAll) {
                // Calculer le score basé sur les noms originaux
                int score = calculateScore(stopInfo.searchableName(), subQueries, patterns);
                // Mettre à jour la map des scores, en gardant le score max par nom principal
                scores.merge(stopInfo.primaryName(), score, Integer::max);
            }
        }

        // 4. Trier les résultats par score décroissant et retourner les noms principaux
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le score de pertinence d'un nom d'arrêt pour une requête donnée.
     *
     * @param stopName   Le nom de l'arrêt (original).
     * @param subQueries La liste des sous-requêtes (originales).
     * @param patterns   La liste des Patterns compilés correspondant aux subQueries.
     * @return Le score total de pertinence.
     */
    private int calculateScore(String stopName, List<String> subQueries, List<Pattern> patterns) {
        int totalScore = 0;

        for (int i = 0; i < subQueries.size(); ++i) {
            Matcher matcher = patterns.get(i).matcher(stopName);

            // Utilise la PREMIERE occurrence trouvée pour le score
            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                int matchLength = end - start;

                if (stopName.isEmpty()) continue; // Eviter division par zéro

                double baseScorePercent = ((double) matchLength / stopName.length()) * 100.0;
                int baseScore = (int) Math.floor(baseScorePercent);

                int multiplier = 1;
                boolean startsWord = (start == 0 || !Character.isLetter(stopName.charAt(start - 1)));
                if (startsWord) {
                    multiplier *= 4;
                }

                boolean endsWord = (end == stopName.length() || !Character.isLetter(stopName.charAt(end)));
                if (endsWord) {
                    multiplier *= 2;
                }

                totalScore += baseScore * multiplier;
            }
        }
        return totalScore;
    }

    /**
     * Compile une sous-requête en Pattern Java, en respectant les règles de
     * sensibilité à la casse et d'équivalence des accents spécifiées.
     *
     * @param subQuery La sous-requête originale.
     * @return Le Pattern compilé.
     */
    private static Pattern compileSubQueryToPattern(String subQuery) {
        boolean queryIsLowercase = subQuery.chars().noneMatch(Character::isUpperCase);
        StringBuilder regex = new StringBuilder();

        for (char c : subQuery.toCharArray()) {
            char lowerC = Character.toLowerCase(c);
            // Si c'est un caractère de base minuscule non accentué listé dans la map
            if (ACCENT_MAP.containsKey(lowerC) && c == lowerC) {
                regex.append(ACCENT_MAP.get(lowerC));
            } else {
                // Sinon (caractère accentué, majuscule, ou autre), traiter littéralement
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }

        // Appliquer la sensibilité à la casse seulement si la query est entièrement en minuscules
        int flags = Pattern.UNICODE_CASE; // Pour gestion correcte des caractères Unicode
        if (queryIsLowercase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        return Pattern.compile(regex.toString(), flags);
    }

    /**
     * Crée la map associant les caractères de base (minuscules, non accentués)
     * à leur classe regex d'équivalents accentués.
     * @return La map des équivalences.
     */
    private static Map<Character, String> createAccentMap() {
        Map<Character, String> map = new HashMap<>();
        map.put('a', "[aáàâä]");
        map.put('e', "[eéèêë]");
        map.put('i', "[iíìîï]");
        map.put('o', "[oóòôö]");
        map.put('u', "[uúùûü]");
        map.put('c', "[cç]");
        return Collections.unmodifiableMap(map);
    }
}