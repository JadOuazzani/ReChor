    package ch.epfl.rechor.journey;

    import java.util.ArrayList;
    import java.util.List;

    import static ch.epfl.rechor.journey.PackedCriteria.*;

    public class JourneyExtractor {
        private JourneyExtractor(){}
        List<Journey> journeys(Profile profile, int depStationId) {

            List<Journey> journeys = new ArrayList<>();
            ParetoFront pf = profile.forStation(depStationId);
            pf.forEach((long criteria) -> {
                int depTime = depMins(criteria);
                int arrTime = arrMins(criteria);
                int transfers = changes(criteria);
                int payload = payload(criteria);






            });
        }

    }
