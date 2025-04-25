package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import static java.lang.Integer.toUnsignedLong;

/**
 * Provides methods to manipulate augmented optimization criteria, which are packed
 * into a long value.
 *
 * <p>The packed long is structured as follows (from the most-significant bit to the
 * least-significant bit):
 * <pre>
 * Bits 63         : Always 0 (ensures a positive value)
 * Bits 62 to 51   : Encoded departure minutes (as a complement)
 * Bits 50 to 39   : Encoded arrival minutes
 * Bits 38 to 32   : Number of changes (7 bits)
 * Bits 31 to 0    : Payload (32 bits)
 * </pre>
 * The departure minutes are stored as a complement so that all criteria can be handled
 * uniformly (minimization framework).
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class PackedCriteria {

    // Mask for payload: lower 32 bits
    public static final long PAYLOAD_MASK = 0xFFFF_FFFFL;
    // Masks for modifying specific fields:
    // Mask to clear the departure minutes field (bits 51 to 62)
    public static final long WITHOUT_DEP_MINS_MASK = 0x0007_FFFF_FFFF_FFFFL;
    // Mask to clear the payload (upper 32 bits remain)
    public static final long WITHOUT_PAYLOAD_MASK = 0xFFFF_FFFF_0000_0000L;
    // GAP: offset for converting actual minutes to the stored value.
    // Minutes are expressed relative to -240 minutes (i.e. 4 hours before midnight).
    public static final int GAP = -240;
    // Bit field sizes
    private static final int BITS_FOR_MINUTES = 12;   // For both departure and arrival minutes
    // Masks for extracting fields
    public static final long MINUTES_MASK = (1L << BITS_FOR_MINUTES) - 1;
    private static final int BITS_FOR_CHANGES = 7;      // For number of changes
    // Shift amounts for each field within the 64-bit value
    private static final int SHIFT_DEP_MINS = 51;       // Departure minutes field (stored as complement)
    private static final int SHIFT_ARR_MINS = 39;       // Arrival minutes field
    private static final int SHIFT_CHANGES = 32;       // Changes field
    // Maximum value for the changes field (7 bits)
    private static final long MAX_CHANGES_FIELD = (1L << BITS_FOR_CHANGES) - 1;
    // Mask to clear the changes field (bits 38 to 32)
    public static final long WITHOUT_CHANGE_MASK = ~(MAX_CHANGES_FIELD << SHIFT_CHANGES);
    // Maximum minute value representable in 12 bits
    private static final long MAX_MIN = (1L << BITS_FOR_MINUTES) - 1;

    // Constants for the valid arrival minutes range.
    private static final int MIN_TIME = GAP;          // -240 minutes
    private static final int MAX_TIME_EXCLUSIVE = 2880; // 2880 minutes

    // Constant for incrementing changes.
    private static final int INCREMENT = 1;

    /**
     * Packs the arrival minutes, number of changes, and payload into a long value.
     *
     * @param arrMins the arrival time in minutes after midnight (must be between
     *                -240 and 2879)
     * @param changes the number of changes (must be less than 128)
     * @param payload the payload (occupies the lower 32 bits)
     * @return the packed long value representing these criteria
     */
    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(changes >> BITS_FOR_CHANGES == 0);
        Preconditions.checkArgument(arrMins >= MIN_TIME && arrMins < MAX_TIME_EXCLUSIVE);
        int translatedMins = arrMins - GAP;
        return (((long) translatedMins) << SHIFT_ARR_MINS)
                | (((long) changes) << SHIFT_CHANGES)
                | toUnsignedLong(payload);
    }

    /**
     * Determines whether the packed criteria include a departure time.
     *
     * @param criteria the packed criteria value
     * @return {@code true} if a departure time is present; {@code false} otherwise
     */
    public static boolean hasDepMins(long criteria) {
        return ((criteria >>> SHIFT_DEP_MINS) & MINUTES_MASK) != 0;
    }

    /**
     * Extracts the departure time from the packed criteria.
     *
     * <p>The departure minutes are stored as a complement. This method converts them
     * back to the actual departure minutes after midnight.</p>
     *
     * @param criteria the packed criteria (must include a departure time)
     * @return the departure time in minutes after midnight
     */
    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        long depBits = (criteria >>> SHIFT_DEP_MINS) & MINUTES_MASK;
        long complement = MAX_MIN - depBits;
        return (int) complement + GAP;
    }

    /**
     * Extracts the arrival time from the packed criteria.
     *
     * @param criteria the packed criteria value
     * @return the arrival time in minutes after midnight
     */
    public static int arrMins(long criteria) {
        long arrMins = (criteria >>> SHIFT_ARR_MINS) & MINUTES_MASK;
        return (int) arrMins + GAP;
    }

    /**
     * Extracts the number of changes from the packed criteria.
     *
     * @param criteria the packed criteria value
     * @return the number of changes
     */
    public static int changes(long criteria) {
        return (int) ((criteria >>> SHIFT_CHANGES) & MAX_CHANGES_FIELD);
    }

    /**
     * Extracts the payload from the packed criteria.
     *
     * @param criteria the packed criteria value
     * @return the payload value
     */
    public static int payload(long criteria) {
        return (int) (criteria & PAYLOAD_MASK);
    }

    /**
     * Determines if the first packed criteria dominate or are equal to the second.
     *
     * <p>If departure times are present, a later departure is considered better,
     * while an earlier arrival and fewer changes are preferable.</p>
     *
     * @param criteria1 the first packed criteria
     * @param criteria2 the second packed criteria
     * @return {@code true} if criteria1 dominate or are equal to criteria2;
     * {@code false} otherwise
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));
        boolean depOk = !hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2);
        boolean arrOk = arrMins(criteria1) <= arrMins(criteria2);
        boolean changeOk = changes(criteria1) <= changes(criteria2);
        return depOk && arrOk && changeOk;
    }

    /**
     * Returns the packed criteria with the departure time removed.
     *
     * @param criteria the original packed criteria
     * @return the packed criteria without the departure time field
     */
    public static long withoutDepMins(long criteria) {
        return criteria & WITHOUT_DEP_MINS_MASK;
    }

    /**
     * Returns the packed criteria with the specified departure time.
     *
     * @param criteria the original packed criteria
     * @param depMins1 the departure time in minutes after midnight
     * @return the packed criteria with the updated departure time
     */
    public static long withDepMins(long criteria, int depMins1) {
        long depBits = MAX_MIN - ((long) depMins1 - GAP);
        return withoutDepMins(criteria) | (depBits << SHIFT_DEP_MINS);
    }

    /**
     * Returns the packed criteria with an additional change.
     *
     * @param criteria the original packed criteria
     * @return the packed criteria with one additional change
     */
    public static long withAdditionalChange(long criteria) {
        long additionalChange = changes(criteria) + INCREMENT;
        long clearedCriteria = criteria & WITHOUT_CHANGE_MASK;
        return clearedCriteria | (additionalChange << SHIFT_CHANGES);
    }

    /**
     * Returns the packed criteria with the specified payload.
     *
     * @param criteria the original packed criteria
     * @param payload1 the new payload value
     * @return the packed criteria with the updated payload
     */
    public static long withPayload(long criteria, int payload1) {
        return (criteria & WITHOUT_PAYLOAD_MASK) | toUnsignedLong(payload1);
    }
}
