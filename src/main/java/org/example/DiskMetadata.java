package org.example;


import java.util.Arrays;

//disk metadata: data describing the disk itself; superblock (blockIndex 0 of disk) is where the metadata will be stored
public class DiskMetadata {

    final byte[] magicSignature;         // identifies disk format type
    final byte   version;                // schema version
    final long   totalAddressableBlocks; // disk geometry
    final long   bitmapRegionStartBlock;     // bitmap region metadata
    final long   bitmapRegionBlockCount;     // bitmap region metadata
    final long   inodeTableRegionStartBlock; // inode region metadata
    final long   inodeTableRegionBlockCount; // inode region metadata
    final long   dataRegionStartBlock;       // data region metadata
//    final long    maximumInodes;
//    final long    rootInodeNumber;

    private DiskMetadata(Builder builder) {

        this.magicSignature             = builder.magicSignature;
        this.version                    = builder.version;
        this.totalAddressableBlocks     = builder.totalAddressableBlocks;
        this.bitmapRegionStartBlock     = builder.bitmapRegionStartBlock;
        this.bitmapRegionBlockCount     = builder.bitmapRegionBlockCount;
        this.inodeTableRegionStartBlock = builder.inodeTableRegionStartBlock;
        this.inodeTableRegionBlockCount = builder.inodeTableRegionBlockCount;
        this.dataRegionStartBlock       = builder.dataRegionStartBlock;

    }

    //builder design pattern: Offsets creation of the DiskMetadata object to another object, called the Builder; allowing fields to be validated & optionally created
    public static class Builder {
        private byte[] magicSignature;            // identifies disk format type
        private byte version;                     // schema version
        private long totalAddressableBlocks;      // disk geometry

        private long  bitmapRegionStartBlock;     // bitmap region metadata
        private long  bitmapRegionBlockCount;     // bitmap region metadata
        private long  inodeTableRegionStartBlock; // inode region metadata
        private long  inodeTableRegionBlockCount; // inode region metadata
        private long  dataRegionStartBlock;       // data region metadata

        public Builder magicSignature(byte[] magicSignature) {
            if(magicSignature == null) {
                throw new IllegalArgumentException("magic signature cannot be null");
            }
            if(magicSignature.length != DiskMetadataSchema.MAGIC_SIGNATURE_LEN) {
                throw new IllegalArgumentException("magic signature length incorrect for proper superblock");
            }

            this.magicSignature = Arrays.copyOf(magicSignature, magicSignature.length);
            return this;
        }

        public Builder version(byte version) {
            if(version < 1) {
                throw new IllegalArgumentException("invalid version");
            }

            this.version = version;
            return this;
        }

        public Builder totalAddressableBlocks(long totalAddressableBlocks) {
            if(totalAddressableBlocks < 1) {
                throw new IllegalArgumentException("invalid number of addressable disk blocks");
            }

            this.totalAddressableBlocks = totalAddressableBlocks;
            return this;
        }

        public Builder bitmapRegionStartBlock(long bitmapRegionStartBlock) {
            if(bitmapRegionStartBlock < 1) {
                throw new IllegalArgumentException("bitmap region start block cannot be less than 1");
            }
            this.bitmapRegionStartBlock = bitmapRegionStartBlock;
            return this;
        }

        public Builder bitmapRegionBlockCount(long bitmapRegionBlockCount) {
            if(bitmapRegionBlockCount < 1) {
                throw new IllegalArgumentException("bitmap region block count cannot be less than 1");
            }
            this.bitmapRegionBlockCount = bitmapRegionBlockCount;
            return this;
        }

        public Builder inodeTableRegionStartBlock(long inodeTableRegionStartBlock) {
            if(inodeTableRegionStartBlock < 1) {
                throw new IllegalArgumentException("inode table region start block cannot be less than 1");
            }
            this.inodeTableRegionStartBlock = inodeTableRegionStartBlock;
            return this;
        }

        public Builder inodeTableRegionBlockCount(long inodeTableRegionBlockCount) {
            if(inodeTableRegionBlockCount < 1) {
                throw new IllegalArgumentException("inode table region start block cannot be less than 1");
            }
            this.inodeTableRegionBlockCount = inodeTableRegionBlockCount;
            return this;
        }

        public Builder dataRegionStartBlock(long dataRegionStartBlock) {
            if(dataRegionStartBlock < 1) {
                throw new IllegalArgumentException("data region start block cannot be less than 1");
            }
            this.dataRegionStartBlock = dataRegionStartBlock;
            return this;
        }

        public DiskMetadata build() {

            validateLayoutInvariants();
            return new DiskMetadata(this);

        }

        // Validates disk bounds, ordering, and overlap.
        // Call from build() AFTER all fields are set.
        private void validateLayoutInvariants() {
            requirePositive(totalAddressableBlocks, "totalAddressableBlocks");

            // Basic positivity
            requirePositive(bitmapRegionStartBlock, "bitmapRegionStartBlock");
            requirePositive(bitmapRegionBlockCount, "bitmapRegionBlockCount");
            requirePositive(inodeTableRegionStartBlock, "inodeTableRegionStartBlock");
            requirePositive(inodeTableRegionBlockCount, "inodeTableRegionBlockCount");
            requirePositive(dataRegionStartBlock, "dataRegionStartBlock");

            // Superblock is block 0; these regions must not point to 0
            require(bitmapRegionStartBlock >= 1, "bitmapRegionStartBlock must be >= 1 (block 0 is superblock)");
            require(inodeTableRegionStartBlock >= 1, "inodeTableRegionStartBlock must be >= 1 (block 0 is superblock)");
            require(dataRegionStartBlock >= 1, "dataRegionStartBlock must be >= 1 (block 0 is superblock)");

            // Compute region ends (exclusive): end = start + count
            requireNoOverflowAdd(bitmapRegionStartBlock, bitmapRegionBlockCount, "bitmapEndExclusive");
            long bitmapEndExclusive = bitmapRegionStartBlock + bitmapRegionBlockCount;

            requireNoOverflowAdd(inodeTableRegionStartBlock, inodeTableRegionBlockCount, "inodeTableEndExclusive");
            long inodeEndExclusive = inodeTableRegionStartBlock + inodeTableRegionBlockCount;

            // Disk bounds: start within disk, end within disk
            require(bitmapRegionStartBlock < totalAddressableBlocks,
                    "bitmapRegionStartBlock must be < totalAddressableBlocks");
            require(inodeTableRegionStartBlock < totalAddressableBlocks,
                    "inodeTableRegionStartBlock must be < totalAddressableBlocks");
            require(dataRegionStartBlock < totalAddressableBlocks,
                    "dataRegionStartBlock must be < totalAddressableBlocks");

            // endExclusive can equal totalAddressableBlocks (region can end at disk end)
            require(bitmapEndExclusive <= totalAddressableBlocks,
                    "bitmap region exceeds disk bounds: start+count must be <= totalAddressableBlocks");
            require(inodeEndExclusive <= totalAddressableBlocks,
                    "inode table region exceeds disk bounds: start+count must be <= totalAddressableBlocks");

            // Ordering + no overlap
            // Policy option: enforce contiguous packing (recommended early).
            require(inodeTableRegionStartBlock >= bitmapEndExclusive,
                    "inode table region overlaps bitmap region (inodeTableStart must be >= bitmapEnd)");


            require(dataRegionStartBlock >= inodeEndExclusive,
                    "data region overlaps inode table (dataStart must be >= inodeTableEnd)");
        }

        // ---- Invariant helpers (put inside Builder) ----
        private static void require(boolean condition, String message) {
            if (!condition) throw new IllegalArgumentException(message);
        }

        private static void requireNonNegative(long v, String name) {
            if (v < 0) throw new IllegalArgumentException(name + " cannot be negative");
        }

        private static void requirePositive(long v, String name) {
            if (v < 1) throw new IllegalArgumentException(name + " must be >= 1");
        }

        private static void requireNoOverflowAdd(long a, long b, String what) {
        // Ensures a + b does not overflow long and is monotonic.
            if (b > 0 && a > Long.MAX_VALUE - b) {
                throw new IllegalArgumentException("overflow while computing " + what);
            }
        }

    }

    //schema:               byte-level layout info; defines disk layout rather than in-memory layout
    //disk metadata schema: byte-level layout info for disk metadata (byte offsets, byte lengths, etc.); used to interpret the disk to buffer transfer
    public static class DiskMetadataSchema {
        static final int MAGIC_SIGNATURE_BYTE_OFFSET = 0;
        static final int VERSION_BYTE_OFFSET = 8;
        static final int TOTAL_ADDRESSABLE_DISK_BLOCKS_BYTE_OFFSET = 16;
        static final int BITMAP_REGION_START_BLOCK_BYTE_OFFSET = 32;
        static final int BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET = 48;
        static final int INODE_TABLE_REGION_START_BLOCK_BYTE_OFFSET = 64;
        static final int INODE_TABLE_REGION_BLOCK_COUNT_BYTE_OFFSET = 80;
        static final int DATA_REGION_START_BLOCK_BYTE_OFFSET = 96;

        static final int MAGIC_SIGNATURE_LEN = 5;
        static final int VERSION_LEN = 1;
        static final int TOTAL_ADDRESSABLE_DISK_BLOCKS_LEN = 8;
        static final int BITMAP_REGION_START_BLOCK_LEN = 8;
        static final int BITMAP_REGION_BLOCK_COUNT_LEN = 8;
        static final int INODE_TABLE_REGION_START_BLOCK_LEN = 8;
        static final int INODE_TABLE_REGION_BLOCK_COUNT_LEN = 8;
        static final int DATA_REGION_START_BLOCK_LEN = 8;

    }


}
