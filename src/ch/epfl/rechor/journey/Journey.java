package ch.epfl.rechor.journey;

import java.time.LocalDateTime;
import java.util.List;

public record Journey(List<Leg> legs) {

    public interface Leg{
        public record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) implements Leg {

        }

        public record Transport(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime, List<IntermediateStop> intermediateStops, Vehicle vehicle, String route, String destination) implements Leg {

        }

        public record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop, LocalDateTime arrTime) implements Leg {

        }
    }
    public Journey {

        if(legs==null||legs.isEmpty()){
            throw new IllegalArgumentException();
        }
        legs = List.copyOf(legs);

        for (int i=1; i<legs.size(); i++) {
            boolean isCurrentFoot = legs.get(i) instanceof Journey.Leg.Foot;
            boolean isPreviousFoot = legs.get(i-1) instanceof Journey.Leg.Foot;

            if(isCurrentFoot == isPreviousFoot){
                throw new IllegalArgumentException();
            }
        }
    }

}
