package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Represents an iCalendar event builder.
 * <p>
 * This class allows you to construct an iCalendar event by adding lines while ensuring that
 * no line exceeds the 75-character limit imposed by the iCalendar format.
 * </p>
 *
 * <p>
 * The builder supports adding both string and date/time values, as well as beginning and ending
 * components (such as VCALENDAR and VEVENT).
 * </p>
 *
 * @author
 *     Riad Elhariry,
 *     Jad Chahed Ouazzani
 */
public class IcalBuilder {

    private final static int MAX_LENGTH = 75;
    private final static int SUBSEQUENT_MAX = 74;
    private final static String CRLF = "\r\n";
    private StringBuilder result = new StringBuilder();
    private ArrayList<Component> components = new ArrayList<>();

    /**
     * Represents a component or object in an iCalendar file.
     */
    public enum Component {
        VCALENDAR, // represents an iCalendar object
        VEVENT     // represents an event component within a calendar
    }

    /**
     * Represents a property name for an iCalendar line.
     */
    public enum Name {
        BEGIN,
        END,
        PRODID,      // identifies the software used to create the object
        VERSION,     // specifies the iCalendar version of the object
        UID,         // uniquely identifies the event
        DTSTAMP,     // indicates the date and time when the event was created or last modified
        DTSTART,     // indicates the start date and time of the event
        DTEND,       // indicates the end date and time of the event
        SUMMARY,     // provides a brief description of the event
        DESCRIPTION; // provides a detailed description of the event
    }

    /**
     * Adds a line to the event under construction with the given property name and string value.
     * <p>
     * If the resulting line exceeds the 75-character limit, the line is folded according to the iCalendar
     * specification, with subsequent lines starting with a space.
     * </p>
     *
     * @param name  the property name.
     * @param value the string value.
     * @return this builder for method chaining.
     */
    public IcalBuilder add(Name name, String value) {
        String line = name + ":" + value;
        if (line.length() <= MAX_LENGTH) {
            result.append(line).append(CRLF);
        } else {
            result.append(line.substring(0, MAX_LENGTH)).append(CRLF);
            int index = MAX_LENGTH;
            while (index < line.length()) {
                int end = Math.min(index + SUBSEQUENT_MAX, line.length());
                result.append(" ").append(line.substring(index, end)).append(CRLF);
                index = end;
            }
        }
        return this;
    }

    /**
     * Adds a line to the event under construction with the given property name and a date/time value.
     * <p>
     * The date/time value is formatted as {@code yyyyMMdd'T'HHmmss}.
     * </p>
     *
     * @param name     the property name.
     * @param dateTime the date/time value.
     * @return this builder for method chaining.
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        add(name, fmt.format(dateTime));
        return this;
    }

    /**
     * Begins a component by adding a line with the property {@code BEGIN} and the name of the component.
     * <p>
     * The component is added to an internal stack so that it can be properly ended later.
     * </p>
     *
     * @param component the component to begin.
     * @return this builder for method chaining.
     */
    public IcalBuilder begin(Component component) {
        components.add(component);
        add(Name.BEGIN, component.name());
        return this;
    }

    /**
     * Ends the most recently begun component that has not yet been ended.
     * <p>
     * This method adds a line with the property {@code END} and the name of the component.
     * </p>
     *
     * @return this builder for method chaining.
     * @throws IllegalArgumentException if there is no component to end.
     */
    public IcalBuilder end() {
        Preconditions.checkArgument(!components.isEmpty());
        add(Name.END, components.remove(components.size() - 1).name());
        return this;
    }

    /**
     * Returns the iCalendar formatted string representing the event under construction.
     * <p>
     * This method verifies that all components have been properly ended before returning the result.
     * </p>
     *
     * @return the complete iCalendar formatted string.
     * @throws IllegalArgumentException if there are unclosed components.
     */
    public String build() {
        Preconditions.checkArgument(components.isEmpty());
        return result.toString();
    }
}
