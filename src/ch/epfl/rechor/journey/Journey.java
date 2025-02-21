package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static java.util.Objects.requireNonNull;

public record Journey(List<Leg> legs) {

    public Journey {

        Preconditions.checkArgument(legs!=null&&!legs.isEmpty());
        legs = List.copyOf(legs);

        for (int i=1; i<legs.size(); i++) {
            boolean isCurrentFoot = legs.get(i) instanceof Journey.Leg.Foot;
            boolean isPreviousFoot = legs.get(i-1) instanceof Journey.Leg.Foot;
            Preconditions.checkArgument(isCurrentFoot != isPreviousFoot);
            Preconditions.checkArgument(!legs.get(i).depTime().isBefore(legs.get(i-1).arrTime()));
            Preconditions.checkArgument(legs.get(i).depStop().equals(legs.get(i-1).arrStop()));


        }
    }
    public sealed interface Leg{

        public Stop depStop();
        public LocalDateTime depTime();
        public Stop arrStop();
        public LocalDateTime arrTime();
        public List<IntermediateStop> intermediateStops();
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }









        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime){
            public IntermediateStop{
                requireNonNull(stop);
                Preconditions.checkArgument(!depTime.isBefore(arrTime));

            }

        }

        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {
            public Transport {
                requireNonNull(vehicle);
                requireNonNull(route);
                requireNonNull(destination);
                requireNonNull(depStop);
                requireNonNull(depTime);
                requireNonNull(arrStop);
                requireNonNull(arrTime);
                if (intermediateStops==null) {
                    throw new NullPointerException();
                }

                intermediateStops = List.copyOf(intermediateStops);


                Preconditions.checkArgument(!arrTime.isBefore(depTime));

            }

        }

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

            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }


        }
    }


    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }

    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }


}
