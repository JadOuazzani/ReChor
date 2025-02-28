package ch.epfl.rechor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class MyBits32_24_8Test {

    @Test
    public void testPackValid() {
        int bits24 = 0x123456; // Valeur sur 24 bits
        int bits8 = 0xAB;     // Valeur sur 8 bits
        int packed = Bits32_24_8.pack(bits24, bits8);
        assertEquals(bits24, Bits32_24_8.unpack24(packed));
        assertEquals(bits8, Bits32_24_8.unpack8(packed));
    }

    @Test
    public void testPackInvalidBits24() {
        int invalidBits24 = 1 << 24; // Nécessite 25 bits
        int bits8 = 0;
        assertThrows(IllegalArgumentException.class, () -> Bits32_24_8.pack(invalidBits24, bits8));
    }

    @Test
    public void testPackInvalidBits8() {
        int bits24 = 0;
        int invalidBits8 = 1 << 8; // Nécessite 9 bits
        assertThrows(IllegalArgumentException.class, () -> Bits32_24_8.pack(bits24, invalidBits8));
    }
}
