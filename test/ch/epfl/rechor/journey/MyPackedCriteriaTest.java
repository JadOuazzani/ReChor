package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MyPackedCriteriaTest {

    @Test
    public void testPackValid() {
        int arrMins = 60;
        int changes = 2;
        int payload = 123456789;

        long criteria = PackedCriteria.pack(arrMins, changes, payload);

        assertFalse(PackedCriteria.hasDepMins(criteria), "Les critères ne doivent pas contenir d'heure de départ");
        assertEquals(arrMins, PackedCriteria.arrMins(criteria), "L'heure d'arrivée doit être correctement empaquetée");
        assertEquals(changes, PackedCriteria.changes(criteria), "Le nombre de changements doit être correctement empaqueté");
        assertEquals(payload, PackedCriteria.payload(criteria), "La charge utile doit être correctement empaquetée");
    }

    @Test
    public void testPackValidNegativeArrMins() {
        int arrMins = -240;
        int changes = 2;
        int payload = 123456789;

        long criteria = PackedCriteria.pack(arrMins, changes, payload);

        assertEquals(arrMins, PackedCriteria.arrMins(criteria), "L'heure d'arrivée a été modifiée");
        assertFalse(PackedCriteria.hasDepMins(criteria), "Les critères ne doivent pas contenir d'heure de départ");
        assertEquals(changes, PackedCriteria.changes(criteria), "Le nombre de changements doit être correctement empaqueté");
        assertEquals(payload, PackedCriteria.payload(criteria), "La charge utile doit être correctement empaquetée");
    }

    @Test
    public void testPackInvalidArrMinsTooLow() {
        int invalidArrMins = -241;
        int changes = 0;
        int payload = 0;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(invalidArrMins, changes, payload);
        });
    }

    @Test
    public void testPackInvalidArrMinsTooHigh() {
        int invalidArrMins = 3200;
        int changes = 0;
        int payload = 0;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(invalidArrMins, changes, payload);
        });
    }

    @Test
    public void testPackInvalidChanges() {
        int arrMins = 100;
        int invalidChanges = 128;
        int payload = 0;
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(arrMins, invalidChanges, payload);
        });
    }

    @Test
    public void testHasDepMinsAndDepMinsException() {
        long criteria = PackedCriteria.pack(200, 3, 42);
        assertFalse(PackedCriteria.hasDepMins(criteria), "Les critères ne doivent pas contenir d'heure de départ");
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.depMins(criteria);
        });
    }

    @Test
    public void testWithDepMins() {
        int arrMins = 150;
        int changes = 1;
        int payload = 999;

        long criteria = PackedCriteria.pack(arrMins, changes, payload);

        assertFalse(PackedCriteria.hasDepMins(criteria), "Les critères ne doivent pas contenir d'heures de départ");

        int depMins = 50;
        long criteriaWithDep = PackedCriteria.withDepMins(criteria, depMins);

        assertTrue(PackedCriteria.hasDepMins(criteriaWithDep), "Les critères doivent contenir une heure de départ après ajout");
        assertEquals(depMins, PackedCriteria.depMins(criteriaWithDep), "L'heure de départ doit être correctement empaquetée");
        assertEquals(arrMins, PackedCriteria.arrMins(criteriaWithDep), "L'heure d'arrivée ne doit pas changer");
        assertEquals(changes, PackedCriteria.changes(criteriaWithDep), "Le nombre de changements ne doit pas changer");
        assertEquals(payload, PackedCriteria.payload(criteriaWithDep), "La charge utile ne doit pas changer");

        int newDepMins = 60;
        long criteriaWithNewDep = PackedCriteria.withDepMins(criteriaWithDep, newDepMins);

        assertTrue(PackedCriteria.hasDepMins(criteriaWithNewDep), "Les critères doivent contenir une heure de départ");
        assertEquals(newDepMins, PackedCriteria.depMins(criteriaWithNewDep), "La nouvelle heure de départ doit être correctement empaquetée");
        assertEquals(arrMins, PackedCriteria.arrMins(criteriaWithNewDep), "L'heure d'arrivée ne doit pas changer");
        assertEquals(changes, PackedCriteria.changes(criteriaWithNewDep), "Le nombre de changements ne doit pas changer");
        assertEquals(payload, PackedCriteria.payload(criteriaWithNewDep), "La charge utile ne doit pas changer");
    }

    @Test
    public void testWithoutDepMins() {
        int arrMins = 300;
        int changes = 4;
        int payload = 42;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        long criteriaWithDep = PackedCriteria.withDepMins(criteria, 100);
        assertTrue(PackedCriteria.hasDepMins(criteriaWithDep), "Avant suppression, les critères doivent contenir une heure de départ");
        long criteriaWithoutDep = PackedCriteria.withoutDepMins(criteriaWithDep);
        assertFalse(PackedCriteria.hasDepMins(criteriaWithoutDep), "Après suppression, les critères ne doivent pas contenir d'heure de départ");
        // Vérifie que les autres champs restent inchangés
        assertEquals(arrMins, PackedCriteria.arrMins(criteriaWithoutDep), "L'heure d'arrivée ne doit pas changer");
        assertEquals(changes, PackedCriteria.changes(criteriaWithoutDep), "Le nombre de changements ne doit pas changer");
        assertEquals(payload, PackedCriteria.payload(criteriaWithoutDep), "La charge utile ne doit pas changer");
    }

    @Test
    public void testWithAdditionalChange() {
        int arrMins = 400;
        int changes = 5;
        int payload = 77;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        long criteriaPlusChange = PackedCriteria.withAdditionalChange(criteria);
        assertEquals(changes + 1, PackedCriteria.changes(criteriaPlusChange), "Le nombre de changements doit être incrémenté de 1");
        assertEquals(arrMins, PackedCriteria.arrMins(criteriaPlusChange), "L'heure d'arrivée ne doit pas changer");
        assertEquals(payload, PackedCriteria.payload(criteriaPlusChange), "La charge utile ne doit pas changer");
        assertFalse(PackedCriteria.hasDepMins(criteriaPlusChange), "L'heure de départ ne doit pas être ajoutée par withAdditionalChange");
    }

    // Teste la modification de la charge utile avec withPayload
    @Test
    public void testWithPayload() {
        int arrMins = 250;
        int changes = 2;
        int payload = 100;
        long criteria = PackedCriteria.pack(arrMins, changes, payload);
        int newPayload = 555;
        long criteriaWithNewPayload = PackedCriteria.withPayload(criteria, newPayload);
        assertEquals(newPayload, PackedCriteria.payload(criteriaWithNewPayload), "La charge utile doit être modifiée");
        assertEquals(arrMins, PackedCriteria.arrMins(criteriaWithNewPayload), "L'heure d'arrivée ne doit pas changer");
        assertEquals(changes, PackedCriteria.changes(criteriaWithNewPayload), "Le nombre de changements ne doit pas changer");
        assertFalse(PackedCriteria.hasDepMins(criteriaWithNewPayload), "L'heure de départ ne doit pas être ajoutée automatiquement");
    }

    // Teste la méthode dominatesOrIsEqual pour des critères sans heure de départ
    @Test
    public void testDominatesOrIsEqualWithoutDep() {
        long crit1 = PackedCriteria.pack(100, 2, 10);
        long crit2 = PackedCriteria.pack(120, 3, 20);
        assertTrue(PackedCriteria.dominatesOrIsEqual(crit1, crit1), "Un critère doit dominer ou être égal à lui-même");
        assertTrue(PackedCriteria.dominatesOrIsEqual(crit1, crit2), "crit1 doit dominer crit2");
        assertFalse(PackedCriteria.dominatesOrIsEqual(crit2, crit1), "crit2 ne doit pas dominer crit1");
    }

    @Test
    public void testDominatesOrIsEqualWithDep() {
        long base = PackedCriteria.pack(300, 3, 99);
        long critA = PackedCriteria.withDepMins(base, 100);
        long critB = PackedCriteria.withDepMins(base, 120);
        assertTrue(PackedCriteria.dominatesOrIsEqual(critB, critA), "critB doit dominer critA si le départ est plus tardif");
        assertFalse(PackedCriteria.dominatesOrIsEqual(critA, critB), "critA ne doit pas dominer critB");
    }

    @Test
    public void testDominatesOrIsEqualMismatchDep() {
        long critWithoutDep = PackedCriteria.pack(400, 4, 50);
        long critWithDep = PackedCriteria.withDepMins(critWithoutDep, 70);
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.dominatesOrIsEqual(critWithoutDep, critWithDep);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.dominatesOrIsEqual(critWithDep, critWithoutDep);
        });
    }

    @Test
    public void testDominatesOrIsEqualFirstExample() {
        long base1 = PackedCriteria.pack(540, 1, 50);
        long base2 = PackedCriteria.pack(540, 2, 50);
        long critA = PackedCriteria.withDepMins(base1, 485);
        long critB = PackedCriteria.withDepMins(base2, 480);
        assertTrue(PackedCriteria.dominatesOrIsEqual(critA, critB), "Le premier critère doit dominer le second");
    }

    @Test
    public void testNeitherDominatesOrIsEqualExample() {
        long base1 = PackedCriteria.pack(540, 1, 50);
        long base2 = PackedCriteria.pack(535, 1, 50);
        long critA = PackedCriteria.withDepMins(base1, 485);
        long critB = PackedCriteria.withDepMins(base2, 480);

        assertFalse(PackedCriteria.dominatesOrIsEqual(critA, critB), "Le premier critère ne domine pas le second");
        assertFalse(PackedCriteria.dominatesOrIsEqual(critB, critA), "Le second critère ne domine pas le second");
    }

    @Test
    public void testPackWithCourseExample() {
        long base = PackedCriteria.pack(540, 2, 50);
        long criteria = PackedCriteria.withDepMins(base, 480);

        assertEquals(0b110100101111, criteria >>> 51 & 0xFFFFFF);
        assertEquals(0b1100001100, base >>> 39 & 0xFFFFFF);
        assertEquals(0b10, PackedCriteria.changes(criteria));
    }

    // Additional Tests

    @Test
    public void testPackValidOther() {
        long criteria = PackedCriteria.pack(0, 10, 12345);
        // Après translation, arrMins renvoie 0 (0 + 240 - 240)
        assertEquals(0, PackedCriteria.arrMins(criteria));
        assertEquals(10, PackedCriteria.changes(criteria));
        assertEquals(12345, PackedCriteria.payload(criteria));
        assertFalse(PackedCriteria.hasDepMins(criteria));
    }

    @Test
    public void testPackInvalidArrMinsOther() {
        // Trop faible
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(-241, 0, 0));
        // Trop élevé (>= 2880)
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(2880, 0, 0));
    }

    @Test
    public void testPackInvalidChangesOther() {
        // 128 ne tient pas dans 7 bits
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.pack(0, 128, 0));
    }

    @Test
    public void testWithDepMinsOther() {
        long criteria = PackedCriteria.pack(100, 5, 1000);
        assertFalse(PackedCriteria.hasDepMins(criteria));
        long withDep = PackedCriteria.withDepMins(criteria, 60);
        assertTrue(PackedCriteria.hasDepMins(withDep));
        assertEquals(60, PackedCriteria.depMins(withDep));
    }

    @Test
    public void testWithAdditionalChangeOther() {
        long criteria = PackedCriteria.pack(0, 10, 500);
        long newCriteria = PackedCriteria.withAdditionalChange(criteria);
        assertEquals(11, PackedCriteria.changes(newCriteria));
    }

    @Test
    public void testWithPayloadOther() {
        long criteria = PackedCriteria.pack(0, 0, 100);
        long newCriteria = PackedCriteria.withPayload(criteria, 200);
        assertEquals(200, PackedCriteria.payload(newCriteria));
    }

    @Test
    public void testDominatesOrIsEqualOther() {
        long c1 = PackedCriteria.pack(0, 5, 1000);
        long c2 = PackedCriteria.pack(0, 5, 1000);
        assertTrue(PackedCriteria.dominatesOrIsEqual(c1, c2));

        long cDominant = PackedCriteria.pack(0, 3, 1500); // meilleur en tout
        long cSub = PackedCriteria.pack(0, 5, 1000);
        assertTrue(PackedCriteria.dominatesOrIsEqual(cDominant, cSub));

        long cNoDep = PackedCriteria.pack(0, 5, 1000);
        long cWithDep = PackedCriteria.withDepMins(cNoDep, 30);
        assertThrows(IllegalArgumentException.class, () -> PackedCriteria.dominatesOrIsEqual(cNoDep, cWithDep));
    }



}