package ch.epfl.rechor.journey;


/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

import ch.epfl.rechor.Preconditions;

import static java.lang.Integer.toUnsignedLong;

public final class PackedCriteria {

    public final static long DEP_MASK = 0x7FF8_0000_0000_0000L;
    public final static long ARR_MASK = 0x0007_FF80_0000_0000L;
    public final static long CHANGES_MASK = 0x0000_0007_F000_0000L;
    public final static long PAYLOAD_MASK = 0x0000_0000_FFFF_FFFFL;
    public final static long WTH_DEP_MINS = 0x0007_FFFF_FFFF_FFFFL;
    public final static long WTHT_PAYLOAD = 0xFFFF_FFFF_0000_0000L;
    public final static long WTHT_CHANGE = 0xFFFF_FFF8_0FFF_FFFFL;


    private static final int GAP = -240;
    private static final int COMPLEMENT = 4095;

    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(changes >> 7 == 0);
        Preconditions.checkArgument(arrMins >= 240 && arrMins < 2880);
        int translateMins = arrMins - GAP;
        long value = 0L;
        value |= ((long) arrMins << 39 | (long) changes << 32 | toUnsignedLong(payload));
        return value;
    }

    public static boolean hasDepMins(long criteria) {
        return (criteria & DEP_MASK) != 0;
    }

    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int depMins = (int) (criteria & DEP_MASK >> 51);
        return depMins + GAP;
    }

    public static int arrMins(long criteria) {
        int arrMins = (int) (criteria & ARR_MASK >> 39);
        return arrMins + GAP;
    }

    public static int changes(long criteria) {
        return (int) (criteria & CHANGES_MASK >> 32);
    }

    public static int payload(long criteria) {
        return (int) (criteria & PAYLOAD_MASK);
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) && hasDepMins(criteria2));
        Preconditions.checkArgument(!hasDepMins(criteria1) && !hasDepMins(criteria2));
        return ((COMPLEMENT - depMins(criteria1)) <= (COMPLEMENT - depMins(criteria2))) && (arrMins(criteria1) <= arrMins(criteria2)) && (changes(criteria1) <= changes(criteria2));
    }

    public static long withoutDepMins(long criteria) {
        return criteria & WTH_DEP_MINS;
    }

    public static long withDepMins(long criteria, int depMins1) {
        return withoutDepMins(criteria) | ((long) depMins1 << 51);
    }

    public static long withAdditionalChange(long criteria) {
        long additionalChange = changes(criteria) + 1;
        long withoutChange = criteria & WTHT_CHANGE;
        return withoutChange | additionalChange << 32;
    }

    public static long withPayload(long criteria, int payload1) {
        return (criteria & WTHT_PAYLOAD) | ((long) payload1);
    }

}
