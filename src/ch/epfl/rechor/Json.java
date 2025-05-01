package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

public sealed interface Json {

    public record JArray(List<Json> values) implements Json {
        @Override
        public String toString() {
            return values.toString();
        }

    }

    public record JObject(Map< String, Json > table) implements Json {
        @Override
        public String toString() {
            return table.toString();
        }

    }

    public record JString(String chain) implements Json {
        @Override
        public String toString() {
            return "\"" + chain + "\"";
        }

    }

    public record JNumber(double value) implements Json {
        @Override
        public String toString() {
            return Double.toString(value);
        }

    }
}
