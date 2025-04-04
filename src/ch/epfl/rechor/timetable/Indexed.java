package ch.epfl.rechor.timetable;

/**
 * Interface intended to be extended by all interfaces representing indexed data.
 *
 * <p>By "indexed data" we mean any timetable data that is conceptually stored in an array
 * and identified by an index ranging from 0 (inclusive) to the size of the array (exclusive).</p>
 */
public interface Indexed {
    /**
     * Returns the number of elements in the indexed data.
     *
     * @return the size of the data.
     */
    int size();
}
