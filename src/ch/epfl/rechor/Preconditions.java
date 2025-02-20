package ch.epfl.rechor;
/*
 *	Author:      Jad Ouazzani
 *	Date:        18/02
 */

public final class Preconditions {
    private Preconditions() {
    }

    void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
