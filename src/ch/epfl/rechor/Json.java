package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Json {

    record JArray(List<Json> values) implements Json {
        @Override
        public String toString() {
            return values.stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(",", "[", "]"));
        }

    }

    record JObject(Map<JString, Json> table) implements Json {
        @Override
        public String toString() {
            return table.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(",", "{", "}"));
        }

    }

    record JString(String chain) implements Json {
        @Override
        public String toString() {
            return "\"" + chain + "\"";
        }

    }

    record JNumber(double value) implements Json {
        @Override
        public String toString() {
            return Double.toString(value);
        }

    }
}
