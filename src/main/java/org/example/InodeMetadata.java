package org.example;

/**
 * An inode is fixed-size metadata in the inode table,
 * that describes a thing (file/dir/free) and contains pointers (block indices) to the disk blocks that hold its data.
 */
public class InodeMetadata {
    byte type;
    long sizeInBytes;
    int[] directPointers;

    public InodeMetadata(Builder builder) {
        this.type = builder.type;
        this.sizeInBytes = builder.sizeInBytes;
        this.directPointers = builder.directPointers;
    }

    //builder design pattern: Builder class controls creation of the InodeMetadata; allowing fields to be validated & optionally created
    public static class Builder {
        private byte type;
        private long sizeInBytes;
        private int[] directPointers;

        public Builder type(byte type) {
            this.type = type;
            return this;
        }

        public Builder sizeInBytes(long sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
            return this;
        }

        public Builder directPointers(int[] directPointers) {
            this.directPointers = directPointers;
            return this;
        }

        public InodeMetadata build() {

            validateLayoutInvariants();
            return new InodeMetadata(this);

        }

        // ---- layout + consistency checks ----
        private void validateLayoutInvariants() {
            // Basic schema sanity
            require(InodeTable.INODE_SIZE_IN_BYTES > 0, "inode size must be > 0");
            require(InodeMetadataSchema.DIRECT_POINTERS_OFFSET >= 0, "direct pointers offset must be >= 0");
            require(InodeMetadataSchema.POINTER_SIZE_IN_BYTES > 0, "pointer size must be > 0");
            require(InodeMetadataSchema.DIRECT_POINTERS_OFFSET <= InodeTable.INODE_SIZE_IN_BYTES,
                    "direct pointers offset must be <= inode size");
            require((InodeTable.INODE_SIZE_IN_BYTES - InodeMetadataSchema.DIRECT_POINTERS_OFFSET) % InodeMetadataSchema.POINTER_SIZE_IN_BYTES == 0,
                    "direct pointer region must be an integer number of pointers");


            // Type checks (choose your policy; here: 0=FREE, 1=FILE, 2=DIR)
            require(type == 0 || type == 1 || type == 2, "invalid inode type (expected 0=FREE,1=FILE,2=DIR)");


            // Size checks
            require(sizeInBytes >= 0, "sizeInBytes cannot be negative");


            // Direct pointers array checks
            require(directPointers != null, "directPointers cannot be null");
            require(directPointers.length == InodeMetadataSchema.DIRECT_POINTERS_COUNT,
                    "directPointers length must equal DIRECT_POINTERS_COUNT (" + InodeMetadataSchema.DIRECT_POINTERS_COUNT + ")");

            // Pointer values: 0 means "unused". Non-zero must be positive.
            for (int i = 0; i < directPointers.length; i++) {
                int p = directPointers[i];
                require(p >= 0, "directPointers[" + i + "] cannot be negative");
            }

            // Optional: enforce that FREE inodes must look empty
            if (type == 0) {
                require(sizeInBytes == 0, "FREE inode must have sizeInBytes == 0");
                for (int i = 0; i < directPointers.length; i++) {
                    require(directPointers[i] == 0, "FREE inode must have all direct pointers == 0");
                }
            }

            // Optional: enforce size fits within direct pointer capacity (no indirect pointers yet)
            long maxBytesRepresentable = (long) countNonZeroPointers(directPointers) * BlockToBufferDevice.BLOCK_SIZE;
            require(sizeInBytes <= maxBytesRepresentable,
                    "sizeInBytes exceeds capacity of provided direct pointers (no indirect blocks yet)");
        }

        // ---- Invariant helpers (put inside Builder) ----
        private static int countNonZeroPointers(int[] ptrs) {
            int c = 0;
            for (int p : ptrs) if (p != 0) c++;
            return c;
        }


        private static void require(boolean condition, String message) {
            if (!condition) throw new IllegalArgumentException(message);
        }
    }

    // inode metadata schema; used to interpret the inode on disk
    public static class InodeMetadataSchema {
        static final int TYPE_OFFSET = 0;
        static final int TYPE_LEN = 1;

        static final int SIZE_IN_BYTES_OFFSET = 8;
        static final int SIZE_IN_BYTES_LEN = 8;

        static final int DIRECT_POINTERS_OFFSET = 16;
        static final int POINTER_SIZE_IN_BYTES = 4;
        static final int DIRECT_POINTERS_COUNT = (InodeTable.INODE_SIZE_IN_BYTES - DIRECT_POINTERS_OFFSET) / POINTER_SIZE_IN_BYTES;
        static final int DIRECT_POINTERS_LEN = DIRECT_POINTERS_COUNT * POINTER_SIZE_IN_BYTES;

    }


}
