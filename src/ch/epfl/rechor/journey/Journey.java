package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 *  Représente un voyage

 * @param legs les étapes du voyage
 */


public record Journey(List<Leg> legs) {

    /**
     * Constructeur de {@code Journey} qui vérifie la liste des étapes reçue
     *
     * @throws IllegalArgumentException si la liste des segments est vide ou si les segments ne sont pas correctement enchaînés.
     */
    public Journey {
        //n'est pas vide,
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!legs.isEmpty());
        legs = List.copyOf(legs); //pour l'immuabilité

        //les étapes à pied alternent avec celles en transport,
        for (int i = 1; i < legs.size(); i++) {
            boolean isCurrentFoot = legs.get(i) instanceof Journey.Leg.Foot;
            boolean isPreviousFoot = legs.get(i - 1) instanceof Journey.Leg.Foot;
            Preconditions.checkArgument(isCurrentFoot != isPreviousFoot);
            Preconditions.checkArgument(!legs.get(i).depTime().isBefore(legs.get(i - 1).arrTime())); //l'instant de départ ne précède pas celui d'arrivée de la précédente,
            Preconditions.checkArgument(legs.get(i).depStop().equals(legs.get(i - 1).arrStop())); //l'arrêt de départ est identique à l'arrêt d'arrivée de la précédente.
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


    /**
     * Interface scellée représentant une étape d'un voyage
     */


    public sealed interface Leg {

        /**
         * @return L'arrêt de départ de l'étape.
         */

        Stop depStop();

        /**
         * @return L'heure de départ de l'étape.
         */

        LocalDateTime depTime();

        /**
         * @return L'arrêt d'arrivée de l'étape.
         */

        Stop arrStop();

        /**
         * @return L'heure d'arrivée de l'étape.
         */

        LocalDateTime arrTime();

        /**
         * @return La liste des arrêts intermédiaires de l'étape.
         */

        List<IntermediateStop> intermediateStops();

        /**
         * @return La durée totale de l'étape.
         */

        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }


        /**
         * Représente un arrêt intermédiaire d'une étape.
         *
         * @param stop L'arrêt intermédiaire en question.
         * @param arrTime L'heure d'arrivée à cet arrêt.
         * @param depTime L'heure de départ de cet arrêt.
         */

        record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

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
         * Représente une étape effectuée en transport public.
         *
         * @param vehicle le type de véhicule utilisé
         * @param route le nom de la ligne sur laquelle circule le véhicule
         * @param destination le nom de la destination finale du véhicule utilisé
         */
        record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime,
                                List<IntermediateStop> intermediateStops, Vehicle vehicle, String route,
                                String destination) implements Leg {

            /**
             * Constructeur vérifiant la validité des paramètres.
             *
             * @throws NullPointerException aucun d'entre eux n'est null,
             * @throws IllegalArgumentException la date/heure d'arrivée n'est pas antérieure à celle de départ.
             */
            public Transport {
                requireNonNull(vehicle);
                requireNonNull(route);
                requireNonNull(destination);
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                intermediateStops = List.copyOf(intermediateStops);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));
            }

        }

        /**
         * Représente une étape effectuée à pied
         * @param arrTime le type de véhicule utilisé
         */
         record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {
            public Foot {
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                Preconditions.checkArgument(!arrTime.isBefore(depTime));


            }

            //retourne une liste vide, car une étape à pied ne comporte jamais d'arrêts intermédiaires
            @Override
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Vérifie si l'étape est un changement au sein de la même gare
             *
             * @return {@code true} si c'est un transfert, sinon {@code false}.
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }


        }
    }

}
