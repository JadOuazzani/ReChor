package ch.epfl.rechor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MyPackedRangeTest {

    @Test
    public void testPackValid() {
        int start = 100;
        int end = 200;
        int packed = PackedRange.pack(start, end);
        assertEquals(start, PackedRange.startInclusive(packed));
        assertEquals(end - start, PackedRange.length(packed));
        assertEquals(end, PackedRange.endExclusive(packed));
    }

    @Test
    public void testPackEmptyInterval() {
        // Intervalle vide ou inversé
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(200, 100));
    }

    @Test
    public void testPackInvalidStart() {
        // startInclusive trop grand pour 24 bits
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(1 << 24, (1 << 24) + 10));
    }

    @Test
    public void testPackInvalidLength() {
        // Longueur trop grande (>255)
        assertThrows(IllegalArgumentException.class, () -> PackedRange.pack(0, 300));
    }

}
