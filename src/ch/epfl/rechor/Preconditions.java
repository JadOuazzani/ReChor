package ch.epfl.rechor;

/**
 * Utility class for checking preconditions.
 * <p>
 * This class provides static methods to verify that certain conditions hold true.
 * If a condition is not met, an appropriate exception is thrown.
 * This helps to detect errors early in the program.
 * </p>
 *
 * @author Riad Elhariry
 * @author Jad Chahed Ouazzani
 */
public final class Preconditions {

    /**
     * Private constructor to prevent instantiation, as this class is not meant to be instantiated.
     */
    private Preconditions() {
    }

    /**
     * Checks that the specified condition is {@code true}.
     * <p>
     * If the condition is {@code false}, an {@link IllegalArgumentException} is thrown.
     * Otherwise, the method does nothing.
     * </p>
     *
     * @param shouldBeTrue the condition to check.
     * @throws IllegalArgumentException if {@code shouldBeTrue} is {@code false}.
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
