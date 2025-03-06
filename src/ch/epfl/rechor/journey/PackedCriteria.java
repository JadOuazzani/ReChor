package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import static java.lang.Integer.toUnsignedLong;

public final class PackedCriteria {

    public final static long MASK = ((1 << 12) - 1);
    public final static long PAYLOAD_MASK = 0xFFFF_FFFFL;
    public final static long WTH_DEP_MINS_MASK = 0x0007_FFFF_FFFF_FFFFL;
    public final static long WTHT_PAYLOAD_MASK = 0xFFFF_FFFF_0000_0000L;
    public final static long WTHT_CHANGE_MASK = ~(0x7FL << 32);

    public static final int GAP = -240;
    private static final long COMPLEMENT_CSTE = 4095L;

    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(changes >> 7 == 0);
        Preconditions.checkArgument(arrMins >= -240 && arrMins < 2880);
        int translateMins = arrMins - GAP;
        return (((long) translateMins) << 39 | ((long) changes) << 32 | toUnsignedLong(payload));
    }

    public static boolean hasDepMins(long criteria) {
        return ((criteria >>> 51) & MASK) != 0;
    }

    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        long depBits = (criteria >>> 51) & MASK;
        long complement = (long) (COMPLEMENT_CSTE - depBits);
        return (int) complement + GAP;
    }

    public static int arrMins(long criteria) {
        long arrMins = (criteria >>> 39) & MASK;
        return (int) arrMins + GAP;
    }

    public static int changes(long criteria) {
        return (int) (criteria >>> 32) & 0x7F;
    }

    public static int payload(long criteria) {
        return (int) (criteria & PAYLOAD_MASK);
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1)
                && hasDepMins(criteria2) || !hasDepMins(criteria1) && !hasDepMins(criteria2));

        return ((!hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2)))
                && (arrMins(criteria1) <= arrMins(criteria2))
                && (changes(criteria1) <= changes(criteria2));
    }

    public static long withoutDepMins(long criteria) {
        return criteria & WTH_DEP_MINS_MASK;
    }

    public static long withDepMins(long criteria, int depMins1) {
        long depBits = COMPLEMENT_CSTE - ((long) depMins1 - GAP);
        return withoutDepMins(criteria) | (depBits << 51);
    }

    public static long withAdditionalChange(long criteria) {
        long additionalChange = changes(criteria) + 1;
        long withoutChange = criteria & WTHT_CHANGE_MASK;
        return withoutChange | (additionalChange << 32);
    }

    public static long withPayload(long criteria, int payload1) {
        return (criteria & WTHT_PAYLOAD_MASK) | toUnsignedLong(payload1);
    }

}
