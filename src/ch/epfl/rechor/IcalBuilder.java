package ch.epfl.rechor;

/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class IcalBuilder {
    private final static int maxLength = 75;
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
        String CRLF = "\r\n";
        StringBuilder stringBuilder1 = new StringBuilder(name + ":" + value);
        while (stringBuilder1.length() > maxLength) {
            result.append(stringBuilder1, 0, maxLength).append(CRLF);
            stringBuilder1 = new StringBuilder(stringBuilder1.substring(maxLength));
        }
        result.append(stringBuilder1);
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
