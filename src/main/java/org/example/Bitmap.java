package org.example;

import java.io.IOException;

/**
   "tiny database of bits"; used in this case to answer: what blocks are free and what blocks are used
    0 -> free
    1 -> used

    Imagine you have 10,000 blocks.
    Using booleans → 10,000 bytes of space ❌
    Using bits     → 10,000 bits = 1,250 bytes of space ✅
    That’s an 8× space win, and scanning bits is fast.

    Block #: 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
    Bitmap: 1 1 1 1 0 0 1 0 0 0 0 0 0 0 0 0
 **/
public class Bitmap {
    BlockToBufferDevice blockToBufferDevice;
    long startBlock;                    // where bitmap region begins on disk
    long blockCount;                    // how many blocks the bitmap occupies
    long totalDiskBlocksTracked;        // how many disk blocks the bitmap represents
    boolean isDirty;                    // dirty = in-memory bitmap no longer matches what is on disk
    byte[] bitmap;


    public Bitmap(BlockToBufferDevice blockToBufferDevice, DiskMetadata metadata) {
        if(blockToBufferDevice == null) {
            throw new IllegalArgumentException("block device cannot be null");
        }
        if(metadata == null) {
            throw new IllegalArgumentException("metadata cannot be null");
        }

        this.blockToBufferDevice = blockToBufferDevice;
        this.startBlock = metadata.bitmapRegionStartBlock;
        this.blockCount = metadata.bitmapRegionBlockCount;
        this.totalDiskBlocksTracked = metadata.totalAddressableBlocks;
        this.isDirty = true;
    }

    public void initializeAndWriteBitmapRegion() throws IOException {               //the bitmap region is sized in whole disk blocks
        byte[] bitmapblock = new byte[BlockToBufferDevice.BLOCK_SIZE];              //zero-defaulted block

        long offset = 0;

        while(offset < this.blockCount) {
            this.blockToBufferDevice.writeBufferIntoBlock(this.startBlock + offset, bitmapblock);

            offset++;
        }

    }

    public void initializeBitmap() throws IOException {
        long totalBitmapBytes = getTotalBitmapBytes();
        if (totalBitmapBytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("bitmap is too large to fit in a Java byte[]");
        }

        this.bitmap = new byte[(int) totalBitmapBytes];                             // in-memory bitmap (meaningful bytes only; padding exists on disk region); zero-defaulted

        this.isDirty = true;

    }

    public void writeBitmap() throws IOException {                                       // write/persist in-memory bitmap to disk
        int srcOffset = 0;
        long remaining = this.bitmap.length;

        long i = 0;
        while(i < this.blockCount) {
            byte[] bitmapBlock = new byte[BlockToBufferDevice.BLOCK_SIZE];

            int x = 0;
            while(x < BlockToBufferDevice.BLOCK_SIZE && remaining > 0) {
                bitmapBlock[x] = this.bitmap[srcOffset];

                x++;
                srcOffset++;
                remaining--;
            }

            this.blockToBufferDevice.writeBufferIntoBlock(this.startBlock + i, bitmapBlock);

            i++;
        }

        this.isDirty = false;
    }

    public boolean isUsed(int blockOffset) {                                     // test if the bit representing a block is 1 (used)
        /**
         * Blocks:      0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
         * Bitmap byte: 0               | 1                       | ...     [block # / num of blocks per bitmap byte]
         * Bit index:   0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7                   [block # / num of bits per bitmap byte]
         */
        requireNonNegative(blockOffset);
        requireCorrectRange(blockOffset, this.totalDiskBlocksTracked);

        int blockBitmapByteIndex = blockOffset / 8;
        int blockBitmapBitIndex = blockOffset % 8;

        int expectedBitmask = (1 << blockBitmapBitIndex);
        int actualBitmask = (this.bitmap[blockBitmapByteIndex] & expectedBitmask);

        return expectedBitmask == actualBitmask;
    }

    public void markUsed(int blockOffset) throws IOException {                       // mark the bit representing a block as 1 (used)
        /**
         * Blocks:      0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
         * Bitmap byte: 0               | 1                       | ...     [block # / num of blocks per bitmap byte]
         * Bit index:   0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7                   [block # / num of bits per bitmap byte]
         */
        requireNonNegative(blockOffset);
        requireCorrectRange(blockOffset, this.totalDiskBlocksTracked);

        int blockBitmapByteIndex = blockOffset / 8;
        int blockBitmapBitIndex = blockOffset % 8;

        int bitmask = (1 << blockBitmapBitIndex);
        this.bitmap[blockBitmapByteIndex] = (byte) (this.bitmap[blockBitmapByteIndex] | bitmask);

        this.isDirty = true;

    }

    public void markFree(int blockOffset) throws IOException {                         // mark the bit representing a block as 0 (free)
        /**
         * Blocks:      0 1 2 3 4 5 6 7 | 8 9 10 11 12 13 14 15   | ...
         * Bitmap byte: 0               | 1                       | ...     [block # / num of blocks per bitmap byte]
         * Bit index:   0 1 2 3 4 5 6 7 | 0 1 2 3 4 5 6 7                   [block # / num of bits per bitmap byte]
         */
        requireNonNegative(blockOffset);
        requireCorrectRange(blockOffset, this.totalDiskBlocksTracked);

        int blockBitmapByteIndex = blockOffset / 8;
        int blockBitmapBitIndex = blockOffset % 8;

        int bitmask = ~(1 << blockBitmapBitIndex);
        this.bitmap[blockBitmapByteIndex] = (byte) (this.bitmap[blockBitmapByteIndex] & bitmask);

        this.isDirty = true;
    }

    // ---- helpers ----
    private static void requireNonNegative(int blockOffset) {
        if(blockOffset < 0) throw new IllegalArgumentException("block offset cannot be negative");
    }
    private static void requireCorrectRange(int blockOffset, long totalDiskBlocksTracked) {
        if(blockOffset > totalDiskBlocksTracked - 1) throw new IllegalArgumentException("block offset cannot exceed total disk blocks available");
    }
    private long getTotalBitmapBytes() {
        long totalBitmapRegionBytes = this.blockCount * BlockToBufferDevice.BLOCK_SIZE;     // Capacity of the bitmap region on disk (bytes)

        long totalBitmapBytes;                                                      // Bytes required to track all disk blocks (ceil(totalDiskBlocksTracked / 8))
        if(this.totalDiskBlocksTracked % 8 == 0) {
            totalBitmapBytes = (this.totalDiskBlocksTracked / 8);
        } else {
            totalBitmapBytes = (this.totalDiskBlocksTracked / 8) + 1;
        }

        if(totalBitmapRegionBytes < totalBitmapBytes) {
            throw new IllegalArgumentException("bitmap region capacity is less than required amount needed to hold bitmap");
        }
        return totalBitmapBytes;
    }

}
