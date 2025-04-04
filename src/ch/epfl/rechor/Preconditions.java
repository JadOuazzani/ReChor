package ch.epfl.rechor;
/*
 *	Author:      Jad Ouazzani
 *	Date:        18/02
 */

public final class Preconditions {

    //rendre impossible la création d'instances de la classe, puisque cela n'a clairement aucun sens
    private Preconditions() {
    }

    //qui lève l'exception IllegalArgumentException si son argument est faux, et ne fait rien sinon.
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
