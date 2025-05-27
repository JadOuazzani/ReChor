package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a JSON value in the ReCHor application.
 * <p>
 * This sealed interface is implemented by four nested record types,
 * corresponding to the four JSON data types used: array, object, string, and number.
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public sealed interface Json {

    /**
     * Represents a JSON array, i.e. an ordered list of JSON values.
     *
     * @param values the list of {@link Json} elements contained in this array
     */
    record JArray(List<Json> values) implements Json {
        /**
         * Returns the JSON text representation of this array.
         * <p>
         * Each element is converted to its JSON string via {@code toString()},
         * and elements are separated by commas, enclosed in square brackets.
         *
         * @return a compact JSON array string, e.g. ["a",123,{}]
         */
        @Override
        public String toString() {
            return values.stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(",", "[", "]"));
        }
    }

    /**
     * Represents a JSON object, i.e. an unordered collection of name/value pairs.
     *
     * @param table a map associating each JSON string key to its corresponding {@link Json} value
     */
    record JObject(Map<JString, Json> table) implements Json {
        /**
         * Returns the JSON text representation of this object.
         * <p>
         * Each entry is rendered as key:value, with keys as quoted strings
         * and values via their {@code toString()}, entries separated by commas,
         * all enclosed in curly braces.
         *
         * @return a compact JSON object string, e.g. {"key":"value", "num":42}
         */
        @Override
        public String toString() {
            return table.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }

    /**
     * Represents a JSON string value.
     *
     * @param chain the raw Java string content of this JSON string (without quotes)
     */
    record JString(String chain) implements Json {
        /**
         * Returns the JSON text representation of this string,
         * i.e. the content surrounded by double quotes.
         *
         * @return a quoted JSON string, e.g. "\"hello\""
         */
        @Override
        public String toString() {
            return "\"" + chain + "\"";
        }
    }

    /**
     * Represents a JSON number value.
     *
     * @param value the numeric value, represented as a Java double
     */
    record JNumber(double value) implements Json {
        /**
         * Returns the JSON text representation of this number,
         * using the standard Java {@link Double#toString(double)} format.
         *
         * @return a JSON number string, e.g. "3.14"
         */
        @Override
        public String toString() {
            return Double.toString(value);
        }
    }
}
