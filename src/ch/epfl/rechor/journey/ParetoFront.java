package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Represents a Pareto front of optimization criteria.
 * <p>
 * The Pareto front is a set of optimization criteria tuples (either pairs or triplets) stored in
 * packed form (as long values) in a private array.
 * </p>
 * <p>
 * The public static final field {@code EMPTY} represents an empty Pareto front.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public class ParetoFront {
    private long[] tableTuples;

    /**
     * An empty Pareto front.
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Private constructor that initializes the Pareto front with the given array of packed tuples.
     * <p>
     * The provided array is stored directly, so it must not be modified after being passed to this constructor.
     * </p>
     *
     * @param tableTuples an array of packed optimization criteria tuples
     */
    private ParetoFront(long[] tableTuples) {
        this.tableTuples = tableTuples;
    }

    /**
     * Returns the number of tuples (optimization criteria) in the Pareto front.
     *
     * @return the size of the Pareto front
     */
    public int size() {
        return tableTuples.length;
    }

    /**
     * Returns the packed optimization criteria tuple with the specified arrival minutes and number of changes.
     * <p>
     * If no such tuple exists in the Pareto front, a NoSuchElementException is thrown.
     * </p>
     *
     * @param arrMins the arrival time in minutes after midnight
     * @param changes the number of changes
     * @return the packed optimization criteria tuple matching the given arrival minutes and number of changes
     * @throws NoSuchElementException if no matching tuple is found in the Pareto front
     */
    public long get(int arrMins, int changes) {
        for (long criteria : tableTuples) {
            if (arrMins(criteria) == arrMins && changes(criteria) == changes) {
                return criteria;
            }
        }
        throw new NoSuchElementException("Critères introuvables dans la frontière");
    }

    /**
     * Applies the given LongConsumer action to each packed criteria tuple in the Pareto front.
     *
     * @param action the action to be performed on each tuple
     */
    public void forEach(LongConsumer action) {
        for (int i = 0; i < size(); i++) {
            action.accept(tableTuples[i]);
        }
    }

    /**
     * Returns a string representation of the Pareto front.
     * <p>
     * For each tuple, the departure time (if present), arrival time, and number of changes are displayed.
     * </p>
     *
     * @return a string representing the Pareto front
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParetoFront[");
        for (int i = 0; i < tableTuples.length; i++) {
            long tuple = tableTuples[i];
            if (i > 0) {
                sb.append("; ");
            }
            sb.append("(");
            // If the tuple contains a departure time, display it.
            if (PackedCriteria.hasDepMins(tuple)) {
                int dep = PackedCriteria.depMins(tuple);
                LocalDateTime depTime = LocalDateTime.of(
                        2025
                        , 2
                        , 18
                        , 0
                        , 0)
                        .plusMinutes(dep);
                sb.append("dep=").append(FormatterFr.formatTime(depTime)).append(", ");
            }
            int arr = PackedCriteria.arrMins(tuple);
            LocalDateTime arrTime = LocalDateTime.of(
                    2025
                    , 2
                    , 18
                    , 0
                    , 0)
                    .plusMinutes(arr);
            int changes = PackedCriteria.changes(tuple);
            sb.append("arr=").append(FormatterFr.formatTime(arrTime));
            sb.append(", chg=").append(changes);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Builder for constructing an immutable ParetoFront.
     * <p>
     * The builder maintains the Pareto front under construction as a long[] array which is resized as needed.
     * All methods that modify the front return the builder itself to allow chaining.
     * </p>
     */
    public static class Builder {
        long[] tableTuples;
        int size;
        private final static long MASK = ~0xFFFFFFFFL;
        private final static int initialCapacity = 2;

        /**
         * Creates a new Builder with an empty Pareto front.
         */
        public Builder() {
            this.tableTuples = new long[initialCapacity];
            this.size = 0;
        }

        /**
         * Copy constructor that creates a new Builder with the same state as the given builder.
         *
         * @param that the builder to copy
         */
        public Builder(Builder that) {
            this.tableTuples = new long[that.tableTuples.length];
            System.arraycopy(that.tableTuples, 0, this.tableTuples, 0, that.size);
            this.size = that.size;
        }

        /**
         * Returns true if and only if the Pareto front under construction is empty.
         *
         * @return true if no tuples have been added; false otherwise
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Clears the Pareto front under construction, removing all tuples.
         *
         * @return this Builder
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Compacts the internal array by removing tuples that are dominated by the new tuple.
         *
         * @param array    the current array of tuples
         * @param size     the current number of tuples
         * @param newTuple the new tuple used for comparison
         * @return the new size of the array after compaction
         */
        private int compact(long[] array, int size, long newTuple) {
            int dst = 0;
            for (int src = 0; src < size; src += 1) {
                if (PackedCriteria.dominatesOrIsEqual(newTuple, array[src])) continue;
                if (dst != src) array[dst] = array[src];
                dst += 1;
            }
            return dst;
        }

        /**
         * Adds the given packed tuple to the Pareto front.
         * <p>
         * The tuple is added only if it is not dominated or equal to an existing tuple; and any existing tuples
         * that are dominated by the new tuple are removed.
         * </p>
         *
         * @param packedTuple the packed optimization criteria tuple to add
         * @return this Builder
         */
        public Builder add(long packedTuple) {
            for (int i = 0; i < this.size; i++) {
                if (PackedCriteria.dominatesOrIsEqual(tableTuples[i], packedTuple)) {
                    return this;
                }
            }

            int insertionIndex = 0;
            long cmpNew = packedTuple & MASK;

            while (insertionIndex < this.size) {
                long cmpExisting = (tableTuples[insertionIndex] & MASK);
                if (cmpExisting > cmpNew) {
                    break;
                }
                insertionIndex++;
            }

            this.size = compact(tableTuples, this.size, packedTuple);

            if (this.size == tableTuples.length) {
                int newCapacity = (int) (tableTuples.length * 1.5);
                if (newCapacity == tableTuples.length) {
                    newCapacity++; // pour éviter de stagner en cas de taille 0 ou 1 (cas limite)
                }
                long[] arrayResized = new long[newCapacity];
                System.arraycopy(tableTuples, 0, arrayResized, 0, this.size);
                tableTuples = arrayResized;
            }

            System.arraycopy(tableTuples
                    , insertionIndex
                    , tableTuples
                    , insertionIndex + 1
                    , this.size - insertionIndex);
            tableTuples[insertionIndex] = packedTuple;
            size++;

            return this;
        }

        /**
         * Packs the given arrival minutes, number of changes, and payload into a tuple (without a departure time)
         * and adds it to the Pareto front.
         *
         * @param arrMins the arrival time in minutes after midnight
         * @param changes the number of changes
         * @param payload the payload value
         * @return this Builder
         */
        public Builder add(int arrMins, int changes, int payload) {
            long tuple = pack(arrMins, changes, payload);
            add(tuple);
            return this;
        }

        /**
         * Adds all tuples from the Pareto front of the specified builder to this builder's Pareto front.
         *
         * @param that the builder from which to add all tuples
         * @return this Builder
         */
        public Builder addAll(Builder that) {
            for (int i = 0; i < that.size; i++) {
                this.add(that.tableTuples[i]);
            }
            return this;
        }

        /**
         * Returns true if and only if every tuple in the Pareto front of the specified builder, after setting its
         * departure time to the given value, is dominated by or equal to at least one tuple in this builder's Pareto
         * front.
         *
         * @param that    the builder whose Pareto front is to be compared
         * @param depMins the departure time (in minutes after midnight) to assign to each tuple from the other builder
         * @return true if this builder fully dominates the other builder's Pareto front; false otherwise
         */
        public boolean fullyDominates(Builder that, int depMins) {
            for (int i = 0; i < that.size; i++) {
                long tWithDep = PackedCriteria.withDepMins(that.tableTuples[i], depMins);
                boolean isDominated = false;
                for (int j = 0; j < this.size; j++) {
                    if (PackedCriteria.dominatesOrIsEqual(tableTuples[j], tWithDep)) {
                        isDominated = true;
                        break;
                    }
                }

                if (!isDominated) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Applies the given LongConsumer action to each tuple in the Pareto front under construction.
         *
         * @param action the action to perform on each tuple
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tableTuples[i]);
            }
        }

        /**
         * Builds and returns an immutable ParetoFront containing all tuples added to this builder.
         * <p>
         * The internal array is copied to guarantee the immutability of the resulting ParetoFront.
         * </p>
         *
         * @return the constructed ParetoFront
         */
        public ParetoFront build() {
            long[] newArray = new long[this.size];
            System.arraycopy(this.tableTuples, 0, newArray, 0, this.size);
            return new ParetoFront(newArray);
        }

        /**
         * Returns a string representation of the current state of the builder
         *
         * @return a string representation of this builder's Pareto front
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ParetoFront.Builder[");
            for (int i = 0; i < size; i++) {
                long tuple = tableTuples[i];
                if (i > 0) {
                    sb.append("; ");
                }
                sb.append(tuple);
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
