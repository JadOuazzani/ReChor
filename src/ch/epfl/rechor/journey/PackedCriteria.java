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
    public final static long PAYLOAD_MASK = 0xFFFF_FFFFL;
    public final static long WTH_DEP_MINS = 0x0007_FFFF_FFFF_FFFFL;
    public final static long WTHT_PAYLOAD = 0xFFFF_FFFF_0000_0000L;
    public final static long WTHT_CHANGE = 0xFFFF_FFF8_0FFF_FFFFL;


    private static final int GAP = -240;
    private static final int COMPLEMENT = 4095;

    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(changes >> 7 == 0);
        Preconditions.checkArgument(arrMins >= -240 && arrMins < 2880);
        int translateMins = arrMins - GAP;
        long value = 0L;
        value |= (((long) translateMins) << 39 | ((long) changes) << 32 | toUnsignedLong(payload));
        return value;
    }

    public static boolean hasDepMins(long criteria) {
        return ((criteria >>> 51) & (1 << 12) - 1) != 0;
    }

    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        long depBits = (criteria >>> 51) & ((1 << 12) - 1);
        long complement = (long) (4095L - depBits);
        return (int) complement + GAP;
    }

    public static int arrMins(long criteria) {
        long arrMins = (criteria >>> 39) & ((1 << 12) - 1);
        return (int) arrMins + GAP;
    }

    public static int changes(long criteria) {
        return (int) (criteria >>> 32) & 0x7F;
    }

    public static int payload(long criteria) {
        return (int) (criteria & PAYLOAD_MASK);
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) && hasDepMins(criteria2) || !hasDepMins(criteria1) && !hasDepMins(criteria2));
        return ((!hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2))) && (arrMins(criteria1) <= arrMins(criteria2)) && (changes(criteria1) <= changes(criteria2));
    }

    public static long withoutDepMins(long criteria) {
        return criteria & WTH_DEP_MINS;
    }

    public static long withDepMins(long criteria, int depMins1) {
        //Preconditions.checkArgument(depMins1 >= -240 && depMins1 < 2880);
        long depBits = 4095L - ((long) depMins1 - GAP);
        return withoutDepMins(criteria) | (depBits << 51);
    }

    public static long withAdditionalChange(long criteria) {
        long additionalChange = changes(criteria) + 1;
        long withoutChange = criteria & WTHT_CHANGE;
        return withoutChange | (additionalChange << 32);
    }

    public static long withPayload(long criteria, int payload1) {
        return (criteria & WTHT_PAYLOAD) | toUnsignedLong(payload1);
    }

}
