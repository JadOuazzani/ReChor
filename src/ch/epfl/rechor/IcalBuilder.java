package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Représente un bâtisseur d'événement au format iCalendar
 */

public class IcalBuilder {

    private final static int MAX_LENGTH = 75;
    private final static int SUBSEQUENT_MAX = 74;
    private final static String CRLF = "\r\n";
    private StringBuilder result = new StringBuilder();
    private ArrayList<Component> components = new ArrayList<>();

    /**
     * Représente un composant ou un objet
     */
    public enum Component {
        VCALENDAR, //représente un objet iCalendar
        VEVENT;// représente un composant événement dans un calendrier
    }

    /**
     * Représente un nom d'une ligne
     */
    public enum Name {
        BEGIN,
        END,
        PRODID, //identifie le logiciel utilisé pour créer l'objet
        VERSION, //donne la version de la norme iCalendar à laquelle correspond l'objet
        UID, //identifie de manière unique l'événement,
        DTSTAMP, //donne la date et l'heure à laquelle l'événement a été créé ou modifié pour la dernière fois
        DTSTART, //donne la date et l'heure du début de l'événement,
        DTEND, //donne la date et l'heure de la fin de l'événement,
        SUMMARY, //donne une description sommaire de l'événement, qui est généralement toujours visible dans un calendrier électronique,
        DESCRIPTION; //donne une description détaillée de l'événement, qui n'est généralement visible que pour l'éventuel événement sélectionné dans un calendrier électronique.
    }

    /**
     * Ajoute à l'événement en cours de construction une ligne en prenant garde à plier la ligne au besoin afin de respecter la contrainte qu'aucune ligne d'une donnée iCalendar ne devrait dépasser 75 caractères
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
     * Ajoute à l'événement en cours de construction une ligne dont le nom est celui donné et la valeur est la représentation textuelle de la date/heure donnée
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        add(name, fmt.format(dateTime));
        return this;
    }

    /**
     * Commence un composant en ajoutant une ligne dont le nom est BEGIN et la valeur est le nom du composant donné,
     */
    public IcalBuilder begin(Component component) {
        components.add(component);
        add(Name.BEGIN, component.name());
        return this;
    }

    /**
     * Termine le dernier composant qui a été commencé précédemment par begin mais pas encore terminé par un appel à end précédent
     */
    public IcalBuilder end() {
        Preconditions.checkArgument(!components.isEmpty());
        add(Name.END, components.removeLast().name());
        return this;
    }

    /**
     * Retourne la chaîne de caractères au format iCalendar représentant l'événement en cours de construction
     */
    public String build() {
        Preconditions.checkArgument(components.isEmpty());
        return result.toString();
    }

}
