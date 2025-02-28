package ch.epfl.rechor;

/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

public final class PackedRange {

    public static int pack(int startInclusive, int endExclusive) {
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument(startInclusive >= 0 && startInclusive < Math.pow(2, 24));
        Preconditions.checkArgument(length <= 255);
//        Preconditions.checkArgument(startInclusive < endExclusive);
        return Bits32_24_8.pack(startInclusive, endExclusive);
    }

    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);
    }

}
