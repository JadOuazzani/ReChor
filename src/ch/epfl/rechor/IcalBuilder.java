package ch.epfl.rechor;

/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class IcalBuilder {
    private final static int MAX_LENGTH = 75;
    private final static int SUBSEQUENT_MAX = 74;
    private StringBuilder result = new StringBuilder();
    private ArrayList<Component> components = new ArrayList<>();

    public enum Component {
        VCALENDAR,
        VEVENT;
    }

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION;
    }

    public IcalBuilder add(Name name, String value) {
        String line = name + ":" + value;
        if (line.length() <= MAX_LENGTH) {
            result.append(line).append("\r\n");
        } else {
            result.append(line.substring(0, MAX_LENGTH)).append("\r\n");
            int index = MAX_LENGTH;
            while (index < line.length()) {
                int end = Math.min(index + SUBSEQUENT_MAX, line.length());
                result.append(" ").append(line.substring(index, end)).append("\r\n");
                index = end;
            }
        }
        return this;
    }

    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        add(name, fmt.format(dateTime));
        return this;
    }

    public IcalBuilder begin(Component component) {
        components.add(component);
        add(Name.BEGIN, component.name());
        return this;
    }

    public IcalBuilder end() {
        Preconditions.checkArgument(!components.isEmpty());
        add(Name.END, components.removeLast().name());
        return this;
    }

    public String build() {
        Preconditions.checkArgument(components.isEmpty());
        return result.toString();
    }

}
