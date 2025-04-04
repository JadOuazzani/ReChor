package ch.epfl.rechor;

/*
 *	Author:      Jad Ouazzani
 *	Date:
 */

/**
 * Permet de manipuler des intervalles d'entiers empaquetés dans un entier de type int.
 * Dans cette représentation, les 24 bits de poids fort contiennent la borne inférieure de l'intervalle, tandis que les 8 bits de poids faible contiennent sa longueur.
 */

public final class PackedRange {

    /**
     * Retourne la valeur de type int représentant l'intervalle d'entiers allant de startInclusive (inclus) à endExclusive (exclu),
     */
    public static int pack(int startInclusive, int endExclusive) {
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument((startInclusive >> 24) == 0 && (length >> 8) == 0);
        //Preconditions.checkArgument(startInclusive >= 0 && startInclusive < (1 << 24));
        //Preconditions.checkArgument(length >= 0 && length < (1 << 8)); // 0 ≤ length < 256
        //Preconditions.checkArgument(startInclusive <= endExclusive);   // autorise length == 0
        return Bits32_24_8.pack(startInclusive, length);
    }

    /**
     * Retourne la longueur de l'intervalle d'entiers empaqueté donné,
     */
    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Retourne le début de l'intervalle d'entiers empaqueté donné, c.-à-d. le plus petit entier qui en fait partie,
     */
    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Retourne le plus petit entier strictement supérieur à tous les entiers de l'intervalle.
     */
    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);
    }

}
