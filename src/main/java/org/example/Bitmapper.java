package org.example;

/**
 * PURPOSE: Given an index, track the usage of a unit using bits
 *
 * A bitmap is a compact structure that tracks the usage of N items using bits.
 *
 * NOTE: The bitmap is an independent structure that knows how to manipulate bits in a byte. It does not know those bits mean or represent.
 *
 * Object #: 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
 * Bitmapper: 1 1 1 1 0 0 1 0 0 0 0 0 0 0 0 0
 */
public class Bitmapper {
    /**
     * 1. Take an address that interprets a byte array
     * 2. Take an address that interprets a long
     * 3. Take a byte, set its value equal to 8 [represents the number of bits in a byte]
     */
    byte[] bitmap;
    long numberOfUnitsTracked;
    final byte BITS_IN_A_BYTE = 8;

    /**
     * "I track N units, and I internally allocate enough bytes to store those bits. This resulting byte array is the Bitmapper"
     *
     *  1. Take the long from the bitmapper, set its value equal to parameter [represents the number of units that the bitmap will track]
     *  2. Take a long, set its value equal to (the number of units tracked + 7) / 8 [represents how many bytes the bitmap needs to be to track all units. +7 accounts for units less than 8 so they at least have a byte allocated]
     *  3. Take a byte array, set its value equal to the result. [represents the actual bitmap, initialized]
     *
     *  Imagine you have 10,000 blocks.
     *  Using booleans → 10,000 bytes of space ❌
     *  Using bits     → 10,000 bits = 1,250 bytes of space ✅
     *  That’s an 8× space win, and scanning bits is fast.
     *
     * @param numberOfUnitsTracked
     */
    public Bitmapper(long numberOfUnitsTracked) {
        this.numberOfUnitsTracked = numberOfUnitsTracked;

        long totalBytes = (this.numberOfUnitsTracked + 7) / BITS_IN_A_BYTE;

        this.bitmap = new byte[(int) totalBytes];
    }

    /**
     * Test whether a bit in the bitmap is currently used or not
     *
     * 1. Take a byte, apply bit shift to it [represents a "marked" index]
     * 2. Take a byte from the bitmap [represents the actual index]
     * 3. Apply an AND bitwise operation between the two bytes [result represents whether actual index is "marked" or not]
     *
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     *  Blocks:                                0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
     *  Bitmapper byte index:                     0               | 1                       | ...
     *  Bitmapper Bit index (inside byte index):  0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7
     *
     */
    public boolean isUsed(int bitIndex) {
        requireNonNegative(bitIndex);
        requireCorrectRange(bitIndex, numberOfUnitsTracked);

        int byteGroup = bitIndex / BITS_IN_A_BYTE;
        int byteGroupBit = bitIndex % BITS_IN_A_BYTE;

        int markedBitmask = (1 << byteGroupBit);
        int currentBitmask = this.bitmap[byteGroup];

        int isMarkedBitInCurrentBitmask = (currentBitmask & markedBitmask);      // 01011000 AND 00001000 -> 00001000

        return markedBitmask == isMarkedBitInCurrentBitmask;
    }

    /**
     * Mark a bit in the bitmap as used
     *
     * 1. Take a byte, apply a bit shift to it [represents a "marked" index]
     * 2. Take a byte from the bitmap [represents the actual index]
     * 3. Apply an OR bitwise operation between the two bytes [result represents the actual index now being "marked"]
     *
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     *  Blocks:                                0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
     *  Bitmapper byte index:                     0               | 1                       | ...
     *  Bitmapper Bit index (inside byte index):  0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7
     *
     */
    public void markUsed(int bitIndex) {
        requireNonNegative(bitIndex);
        requireCorrectRange(bitIndex, numberOfUnitsTracked);

        int byteGroup = bitIndex / BITS_IN_A_BYTE;
        int byteGroupBit = bitIndex % BITS_IN_A_BYTE;

        int currentBitmask = this.bitmap[byteGroup];
        int toBeAppliedBitmask = (1 << byteGroupBit);

        this.bitmap[byteGroup] = (byte) (currentBitmask | toBeAppliedBitmask);

    }

    /**
     * Mark a bit in the bitmap as free
     *
     * 1. Take a byte, apply a bit shift to it [represents a "marked" index]
     * 2. Apply a NOT bitwise operation on the byte [represents an "unmarked" index]
     * 2. Take a byte from the bitmap [represents the actual index]
     * 3. Apply an AND bitwise operation between the two bytes [result represents the actual index now being "unmarked"]
     *
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     *  Blocks:                                0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
     *  Bitmapper byte index:                     0               | 1                       | ...
     *  Bitmapper Bit index (inside byte index):  0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7
     *
     */
    public void markFree(int bitIndex) {
        requireNonNegative(bitIndex);
        requireCorrectRange(bitIndex, numberOfUnitsTracked);

        int byteGroup = bitIndex / BITS_IN_A_BYTE;
        int byteGroupBit = bitIndex % BITS_IN_A_BYTE;

        int currentBitmask = this.bitmap[byteGroup];
        int toBeAppliedBitmask = ~(1 << byteGroupBit);              // create a bitmask with 1 bit on. then invert it. Ex: 00001000 -> 11110111

        this.bitmap[byteGroup] = (byte) (currentBitmask & toBeAppliedBitmask);
    }

    // ---- helpers ----
    private static void requireNonNegative(int index) {
        if(index < 0) throw new IllegalArgumentException("block offset cannot be negative");
    }
    private static void requireCorrectRange(int index, long trackedUnitCount) {
        if(index > trackedUnitCount - 1) throw new IllegalArgumentException("block offset cannot exceed total disk blocks available");
    }


}
