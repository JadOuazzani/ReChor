package ch.epfl.rechor;


/**
 * Permettant de manipuler une paire de valeurs de 24 et 8 bits respectivement empaquetées dans un entier de 32 bits de type int
 */

public final class Bits32_24_8 {

    /**
     * Retourne le vecteur de 32 bits dont les 24 bits de poids fort
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits24 >> 24) == 0 && (bits8 >> 8) == 0); //lève une IllegalArgumentException si l'une des deux valeurs nécessite plus de bits qu'elle ne devrait
        return bits24 << 8 | bits8;
    }

    /**
     * Retourne les 24 bits de poids fort du vecteur de 32 bits donné,
     */
    public static int unpack24(int bits32) {
        return bits32 >>> 8;
    }

    /**
     * Retourne les 8 bits de poids faible du vecteur de 32 bits donné.
     */
    public static int unpack8(int bits32) {
        return bits32 & 0xff;
    }

}
