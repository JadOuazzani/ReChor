package ch.epfl.rechor;

/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

public final class PackedRange {

    public static int pack(int startInclusive, int endExclusive) {
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument(startInclusive >= 0 && startInclusive < (1 << 24));
        Preconditions.checkArgument(length >= 0 && length < (1 << 8)); // 0 ≤ length < 256
        Preconditions.checkArgument(startInclusive <= endExclusive);   // autorise length == 0
        return Bits32_24_8.pack(startInclusive, length);
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
