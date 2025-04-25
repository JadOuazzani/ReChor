package ch.epfl.rechor;

/**
 * Provides methods for manipulating a pair of values (24 bits and 8 bits respectively)
 * packed into a 32-bit integer.
 * <p>
 * The 24 high-order bits represent the first value, and the 8 low-order bits represent the second value.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class Bits32_24_8 {

    /**
     * Number of bits allocated for the first value.
     */
    private static final int BITS_FOR_FIRST = 24;

    /**
     * Number of bits allocated for the second value.
     */
    private static final int BITS_FOR_SECOND = 8;

    /**
     * Mask used to extract the second value (8 bits).
     */
    private static final int MASK_FOR_SECOND = (1 << BITS_FOR_SECOND) - 1;

    /**
     * Packs a 24-bit value and an 8-bit value into a 32-bit integer.
     * <p>
     * The method checks that the first value fits in 24 bits and the second in 8 bits.
     * </p>
     *
     * @param bits24 the 24-bit value.
     * @param bits8  the 8-bit value.
     * @return a 32-bit integer in which the 24 high-order bits come from {@code bits24} and the 8 low-order bits come from {@code bits8}.
     * @throws IllegalArgumentException if either value requires more bits than allowed.
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits24 >> BITS_FOR_FIRST) == 0 && (bits8 >> BITS_FOR_SECOND) == 0);
        return bits24 << BITS_FOR_SECOND | bits8;
    }

    /**
     * Unpacks and returns the 24 high-order bits from the given 32-bit integer.
     *
     * @param bits32 the 32-bit integer containing the packed values.
     * @return the first value (24 bits) as an integer.
     */
    public static int unpack24(int bits32) {
        return bits32 >>> BITS_FOR_SECOND;
    }

    /**
     * Unpacks and returns the 8 low-order bits from the given 32-bit integer.
     *
     * @param bits32 the 32-bit integer containing the packed values.
     * @return the second value (8 bits) as an integer.
     */
    public static int unpack8(int bits32) {
        return bits32 & MASK_FOR_SECOND;
    }
}
