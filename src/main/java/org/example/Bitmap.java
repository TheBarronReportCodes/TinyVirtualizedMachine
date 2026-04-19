package org.example;

/**
 * PURPOSE: Given an index, track the usage of a unit using bits
 *
 * A bitmap is a compact structure that tracks the usage of N items using bits.
 *
 * NOTE: The bitmap is an independent structure that knows how to manipulate bits in a byte. It does not know those bits mean or represent.
 *
 * Block #: 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
 * Bitmap: 1 1 1 1 0 0 1 0 0 0 0 0 0 0 0 0
 */
public class Bitmap {
    byte[] bitmap;
    long trackedUnitCount;
    final int BITS_IN_A_BYTE = 8;

    /**
     * "I track N units, and I internally allocate enough bytes to store those bits. This resulting byte array is the Bitmap"
     *
     *  Imagine you have 10,000 blocks.
     *  Using booleans → 10,000 bytes of space ❌
     *  Using bits     → 10,000 bits = 1,250 bytes of space ✅
     *  That’s an 8× space win, and scanning bits is fast.
     *
     *
     * @param trackedUnitCount
     */
    public Bitmap(long trackedUnitCount) {
        this.trackedUnitCount = trackedUnitCount;

        long totalBytes = (this.trackedUnitCount + 7) / 8;

        this.bitmap = new byte[(int) totalBytes];
    }

    /**
     * Test whether a bit in the bitmap is currently used or not
     *
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     *  Blocks:                                0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
     *  Bitmap byte index:                     0               | 1                       | ...
     *  Bitmap Bit index (inside byte index):  0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7
     *
     */
    public boolean isUsed(int bitIndex) {
        requireNonNegative(bitIndex);
        requireCorrectRange(bitIndex, trackedUnitCount);

        int byteGroup = bitIndex / BITS_IN_A_BYTE;
        int byteGroupBit = bitIndex % BITS_IN_A_BYTE;

        int currentBitmask = this.bitmap[byteGroup];                             // current bitmask in bitmap   EX: 01011000
        int markedBitmask = (1 << byteGroupBit);                                 // one marked bit in the byte. EX: 00001000

        int isMarkedBitInCurrentBitmask = (currentBitmask & markedBitmask);      // 01011000 AND 00001000 -> 00001000

        return markedBitmask == isMarkedBitInCurrentBitmask;
    }

    /**
     * Mark a bit in the bitmap as used
     *
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     *  Blocks:                                0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
     *  Bitmap byte index:                     0               | 1                       | ...
     *  Bitmap Bit index (inside byte index):  0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7
     *
     */
    public void markUsed(int bitIndex) {
        requireNonNegative(bitIndex);
        requireCorrectRange(bitIndex, trackedUnitCount);

        int byteGroup = bitIndex / BITS_IN_A_BYTE;
        int byteGroupBit = bitIndex % BITS_IN_A_BYTE;

        int currentBitmask = this.bitmap[byteGroup];
        int toBeAppliedBitmask = (1 << byteGroupBit);

        this.bitmap[byteGroup] = (byte) (currentBitmask | toBeAppliedBitmask);

    }

    /**
     * Mark a bit in the bitmap as free
     *
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     *  Blocks:                                0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
     *  Bitmap byte index:                     0               | 1                       | ...
     *  Bitmap Bit index (inside byte index):  0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7
     *
     */
    public void markFree(int bitIndex) {
        requireNonNegative(bitIndex);
        requireCorrectRange(bitIndex, trackedUnitCount);

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
