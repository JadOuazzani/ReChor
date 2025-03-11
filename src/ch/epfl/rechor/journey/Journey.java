package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Représente un trajet composé de plusieurs segments ({@link Leg}).
 * Assure que la liste des segments est valide et ordonnée chronologiquement.
 *
 * @param legs Liste des segments constituant le trajet, ne peut pas être vide ou nulle.
 */


public record Journey(List<Leg> legs) {

    public Journey {

        /**
         * Constructeur de {@code Journey} qui vérifie la validité des segments.
         *
         * @throws IllegalArgumentException si la liste des segments est vide ou si les segments ne sont pas correctement enchaînés.
         */

        Preconditions.checkArgument(legs != null && !legs.isEmpty());
        legs = List.copyOf(legs);

        for (int i = 1; i < legs.size(); i++) {
            boolean isCurrentFoot = legs.get(i) instanceof Journey.Leg.Foot;
            boolean isPreviousFoot = legs.get(i - 1) instanceof Journey.Leg.Foot;
            Preconditions.checkArgument(isCurrentFoot != isPreviousFoot);
            Preconditions.checkArgument(!legs.get(i).depTime().isBefore(legs.get(i - 1).arrTime()));
            Preconditions.checkArgument(legs.get(i).depStop().equals(legs.get(i - 1).arrStop()));


        }
    }


    /**
     * Interface scellée représentant un segment de trajet.
     */


    public sealed interface Leg {

        /**
         * @return L'arrêt de départ du segment.
         */

        public Stop depStop();

        /**
         * @return L'heure de départ du segment.
         */

        public LocalDateTime depTime();

        /**
         * @return L'arrêt d'arrivée du segment.
         */

        public Stop arrStop();

        /**
         * @return L'heure d'arrivée du segment.
         */

        public LocalDateTime arrTime();

        /**
         * @return La liste des arrêts intermédiaires du segment.
         */

        public List<IntermediateStop> intermediateStops();

        /**
         * @return La durée totale du segment.
         */

        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Représente un arrêt intermédiaire sur un segment de transport.
         *
         * @param stop L'arrêt intermédiaire.
         * @param arrTime L'heure d'arrivée à cet arrêt.
         * @param depTime L'heure de départ de cet arrêt.
         */


        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            /**
             * Constructeur vérifiant la cohérence des horaires.
             *
             * @throws NullPointerException si {@code stop} est nul.
             * @throws IllegalArgumentException si {@code depTime} est avant {@code arrTime}.
             */
            public IntermediateStop {
                requireNonNull(stop);
                Preconditions.checkArgument(!depTime.isBefore(arrTime));
            }

        }

        /**
         * Représente un segment de transport utilisant un véhicule.
         *
         * @param vehicle Le véhicule utilisé.
         * @param route La route empruntée.
         * @param destination La destination du trajet.
         */

        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                                List<IntermediateStop> intermediateStops, Vehicle vehicle, String route,
                                String destination) implements Leg {

            /**
             * Constructeur vérifiant la validité des paramètres.
             *
             * @throws NullPointerException si un des paramètres obligatoires est nul.
             * @throws IllegalArgumentException si l'heure d'arrivée est avant l'heure de départ.
             */
            public Transport {
                requireNonNull(vehicle);
                requireNonNull(route);
                requireNonNull(destination);
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                if (intermediateStops == null) {
                    throw new NullPointerException();
                }
                intermediateStops = List.copyOf(intermediateStops);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));

            }

        }

        /**
         * Représente un segment effectué à pied.
         */


        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {
            public Foot {
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));


            }

            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Vérifie si le segment est un transfert, c'est-à-dire que l'arrêt de départ et d'arrivée sont identiques.
             *
             * @return {@code true} si c'est un transfert, sinon {@code false}.
             */

            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }


        }
    }

    /**
     * @return L'arrêt de départ du trajet.
     */
    public Stop depStop() {
        return legs.getFirst().depStop();
    }
    /**
     * @return L'arrêt d'arrivée du trajet.
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }
    /**
     * @return L'heure de départ du trajet.
     */
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }
    /**
     * @return L'heure d'arrivée du trajet.
     */
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }
    /**
     * @return La durée totale du trajet.
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }


}
