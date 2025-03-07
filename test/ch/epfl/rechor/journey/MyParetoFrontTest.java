package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static ch.epfl.rechor.journey.PackedCriteria.*;
import static org.junit.jupiter.api.Assertions.*;

public class MyParetoFrontTest {

    // -- Tests sur ParetoFront.EMPTY --

    @Test
    public void emptyFrontHasSizeZero() {
        assertEquals(0, ParetoFront.EMPTY.size());
    }

    @Test
    public void emptyFrontGetThrows() {
        assertThrows(NoSuchElementException.class, () -> ParetoFront.EMPTY.get(600, 2));
    }

    @Test
    public void emptyFrontForEachDoesNothing() {
        final int[] counter = {0};
        ParetoFront.EMPTY.forEach(value -> counter[0]++);
        assertEquals(0, counter[0]);
    }

    // -- Tests basiques sur Builder et build --

    @Test
    public void buildFromEmptyBuilderGivesEmptyFront() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        ParetoFront front = builder.build();
        assertEquals(0, front.size());
    }

    @Test
    public void addSingleTupleThenBuildGivesFrontOfSizeOne() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t = pack(500, 1, 42);
        builder.add(t);
        ParetoFront front = builder.build();
        assertEquals(1, front.size());
        assertEquals(t, front.get(500, 1));
    }

    // -- Test "add" : ajout d'un tuple dominé doit être rejeté --

    @Test
    public void addDominatedTupleIsRejected() {
        // t1 domine t2, car 480 < 490 et 0 < 1
        long t1 = pack(480, 0, 111);
        long t2 = pack(490, 1, 222);

        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(t1);       // on ajoute d'abord t1
        builder.add(t2);       // t2 est dominé par t1 -> rejeté

        ParetoFront front = builder.build();
        assertEquals(1, front.size());
        assertEquals(t1, front.get(480, 0));
        // get(490,1) doit lever une exception
        assertThrows(NoSuchElementException.class, () -> front.get(490, 1));
    }

    // -- Test "add" : ajout d'un tuple dominant doit faire supprimer les dominés --

    @Test
    public void addDominatingTupleRemovesDominated() {
        // t1 est dominé par t2, car 490 < 500 et 1 <= 2, et il y a une inégalité stricte sur l'arrivée
        long t1 = pack(500, 2, 111);
        long t2 = pack(490, 1, 222);

        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(t1);  // on ajoute d'abord t1
        builder.add(t2);  // t2 domine t1 -> t1 doit être éliminé

        ParetoFront front = builder.build();
        assertEquals(1, front.size());
        assertEquals(t2, front.get(490, 1));
        // get(500,2) doit lever une exception
        assertThrows(NoSuchElementException.class, () -> front.get(500, 2));
    }

    // -- Test "add" : ajout de tuples incomparables -> on les garde tous --

    @Test
    public void addIncomparableTuplesAreAllKept() {
        /*
         * On définit 3 tuples incomparables dans le sens de Pareto :
         *  - tA(500, 2)  => arrivée 500, 2 changements
         *  - tB(520, 1)  => arrivée 520, 1 changement
         *  - tC(480, 3)  => arrivée 480, 3 changements
         *
         *  Aucun ne domine un autre car chacun est "meilleur" sur un critère et "pire" sur l'autre.
         */
        long tA = pack(500, 2, 111);
        long tB = pack(520, 1, 222);
        long tC = pack(480, 3, 333);

        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(tA);
        builder.add(tB);
        builder.add(tC);

        ParetoFront front = builder.build();
        assertEquals(3, front.size());

        // Vérifions qu'on peut récupérer chacun
        assertEquals(tA, front.get(500, 2));
        assertEquals(tB, front.get(520, 1));
        assertEquals(tC, front.get(480, 3));
    }

    // -- Test "add" avec heure de départ --

    @Test
    public void addTupleWithDepTime() {
        long tNoDep = pack(600, 2, 111);
        long tWithDep = withDepMins(tNoDep, 550);  // on fixe l'heure de départ à 550

        ParetoFront.Builder builder = new ParetoFront.Builder();
        // Si la comparaison impose que tNoDep et tWithDep soient "incomparables" (p. ex. checkArgument(false)),
        // alors on doit gérer la stratégie (soit les ignorer, soit lever exception, etc.)
        // Ici, on suppose qu'on n'ajoute pas deux tuples qui ne sont pas comparables, donc on teste le cas normal :
        builder.add(tWithDep);

        ParetoFront front = builder.build();
        assertEquals(1, front.size());

        // On vérifie qu'on le retrouve
        long retrieved = front.get(600, 2);
        assertTrue(hasDepMins(retrieved));
        assertEquals(550, depMins(retrieved));
    }

    // -- Test "get" sur un front non vide --

    @Test
    public void getThrowsForAbsentTuple() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(480, 0, 111));
        builder.add(pack(500, 1, 222));
        ParetoFront front = builder.build();

        // get(490,0) n'existe pas
        assertThrows(NoSuchElementException.class, () -> front.get(490, 0));
    }

    // -- Test "forEach" --

    @Test
    public void forEachEnumeratesAllTuples() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t1 = pack(480, 0, 111);
        long t2 = pack(470, 1, 222);
        long t3 = pack(460, 2, 333);
        // (Supposons qu'aucun ne domine un autre ici, on choisit des valeurs incomparables)
        builder.add(t1);
        builder.add(t2);
        builder.add(t3);

        ParetoFront front = builder.build();
        List<Long> collected = new ArrayList<>();
        front.forEach(collected::add);

        assertEquals(3, collected.size());
        assertTrue(collected.contains(t1));
        assertTrue(collected.contains(t2));
        assertTrue(collected.contains(t3));
    }

    // -- Test "toString" --


    // -- Test "addAll" --

    @Test
    public void addAllMergesFrontsRemovingDominated() {
        /*
         * builder1 : contient (500, 2), (520, 0)
         * builder2 : contient (480, 3), (490, 1)
         * On les fusionne dans builder1 -> on doit obtenir (500,2), (520,1), (480,3), (490,0)
         * sauf si l'un domine un autre. Vérifions que (490,0) domine (500,2) ?
         *   - (490 < 500) et (0 < 2), oui, donc (500,2) est dominé -> il doit disparaître.
         *
         * Au final, on doit avoir (490,1), (520,0), (480,3) (si aucun ne domine un autre).
         */
        long tA = pack(500, 2, 111);
        long tB = pack(520, 0, 222);
        ParetoFront.Builder builder1 = new ParetoFront.Builder();
        builder1.add(tA);
        builder1.add(tB);

        long tC = pack(480, 3, 333);
        long tD = pack(490, 1, 444);
        ParetoFront.Builder builder2 = new ParetoFront.Builder();
        builder2.add(tC);
        builder2.add(tD);

        builder1.addAll(builder2);
        ParetoFront front = builder1.build();

        // tA = (500,2) est dominé par (490,0), donc il disparaît.
        // Reste tB=(520,1), tC=(480,3), tD=(490,0).
        assertEquals(3, front.size());
        assertEquals(tB, front.get(520,0));
        assertEquals(tC, front.get(480,3));
        assertEquals(tD, front.get(490,1));
        // get(500,2) doit lever une exception
        assertThrows(NoSuchElementException.class, () -> front.get(500, 2));
    }

    // -- Test "fullyDominates" --

    @Test
    public void fullyDominatesIsTrueIfAllAreDominated() {

        long t1 = withDepMins(pack(490,1, 111), 5);
        long t2 = pack(500,2, 222);
        ParetoFront.Builder b1 = new ParetoFront.Builder();
        b1.add(t1);
        ParetoFront.Builder b2 = new ParetoFront.Builder();
        b2.add(t2);

        assertTrue(b1.fullyDominates(b2, 5));
    }

    @Test
    public void fullyDominatesIsFalseIfOneTupleIsNotDominated() {

        long t1 = withDepMins(pack(480,3, 111), 5);
        long t2 = pack(500,1, 222);
        long t3 = pack(490,2, 333);
        ParetoFront.Builder b1 = new ParetoFront.Builder();
        b1.add(t1);
        ParetoFront.Builder b2 = new ParetoFront.Builder();
        b2.add(t2).add(t3);
        assertFalse(b1.fullyDominates(b2, 5));

    }

    // -- Test "build" et immuabilité --

    @Test
    public void builtFrontIsImmutableAfterBuild() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long t1 = pack(480, 0, 111);
        builder.add(t1);
        ParetoFront front1 = builder.build();
        assertEquals(1, front1.size());

        // On modifie le builder après avoir construit front1
        long t2 = pack(500, 1, 222);
        builder.add(t2);

        // front1 ne doit pas changer
        assertEquals(1, front1.size());
        assertEquals(t1, front1.get(480, 0));

        // Une nouvelle construction reflète la modification
        ParetoFront front2 = builder.build();
        assertEquals(1, front2.size());
        assertEquals(t1, front2.get(480, 0));
    }


}
