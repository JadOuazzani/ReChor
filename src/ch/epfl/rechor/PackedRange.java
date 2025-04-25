package ch.epfl.rechor;

/**
 * Provides methods for manipulating ranges of integers packed into a 32-bit integer.
 * <p>
 * In this representation, the {@code LOWER_BOUND_BITS} high-order bits store the lower bound of the range (inclusive),
 * while the {@code LENGTH_BITS} low-order bits store its length.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class PackedRange {

    /**
     * Number of bits used to store the lower bound of the range.
     */
    private static final int LOWER_BOUND_BITS = 24;

    /**
     * Number of bits used to store the length of the range.
     */
    private static final int LENGTH_BITS = 8;

    /**
     * Returns the int value representing the range of integers from {@code startInclusive} (inclusive)
     * to {@code endExclusive} (exclusive).
     *
     * @param startInclusive the starting value (inclusive) of the range.
     * @param endExclusive   the ending value (exclusive) of the range.
     * @return an int encoding the range.
     * @throws IllegalArgumentException if either the start value or the length require more bits than allowed.
     */
    public static int pack(int startInclusive, int endExclusive) {
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument((startInclusive >> LOWER_BOUND_BITS) == 0 &&
                (length >> LENGTH_BITS) == 0);
        return Bits32_24_8.pack(startInclusive, length);
    }

    /**
     * Returns the length of the packed integer range.
     *
     * @param interval the packed range.
     * @return the length of the range.
     */
    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Returns the starting value (inclusive) of the packed integer range,
     * i.e., the smallest integer that is part of the range.
     *
     * @param interval the packed range.
     * @return the starting value (inclusive) of the range.
     */
    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Returns the smallest integer that is strictly greater than all integers in the range,
     * i.e., the end value (exclusive) of the range.
     *
     * @param interval the packed range.
     * @return the ending value (exclusive) of the range.
     */
    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);
    }
}
