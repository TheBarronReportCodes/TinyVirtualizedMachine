package org.example;

/**
 * An inode (index node) [a.k.a file metadata] is an object that stores the metadata about a file or directory, but not
 * its name or its actual content
 */
public class Inode {
    static int INODE_SIZE = 128;
    byte fileType;  // file object or directory object [1 for file; 2 for directory]
    long fileSize;
    long[] fileDiskBlockPointers;

    private Inode(Builder builder) {
        this.fileType = builder.fileType;
        this.fileSize = builder.fileSize;
        this.fileDiskBlockPointers = builder.fileDiskBlockPointers;
    }

    //builder design pattern: Offsets creation of the Inode object to another object, called the Builder; allowing fields to be validated & optionally created
    public static class Builder {
        private byte fileType;
        private long fileSize;
        private long[] fileDiskBlockPointers;

        public Builder setFileType(byte type) {
            this.fileType = type;
            return this;
        }

        public Builder setFileSize(long sizeInBytes) {
            this.fileSize = sizeInBytes;
            return this;
        }

        public Builder setFileDiskBlockPointers(long[] directPointers) {
            this.fileDiskBlockPointers = directPointers;
            return this;
        }

        public Inode build() {

            validateLayoutInvariants();
            return new Inode(this);

        }

        // ---- layout + consistency checks ----
        private void validateLayoutInvariants() {
            // Basic schema sanity
            require(INODE_SIZE > 0, "inode size must be > 0");
            require(InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET >= 0, "direct pointers offset must be >= 0");
            require(InodeSchema.FILE_DISK_BLOCK_POINTER_LEN > 0, "pointer size must be > 0");
            require(InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET <= INODE_SIZE,
                    "direct pointers offset must be <= inode size");
            require((INODE_SIZE - InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET) % InodeSchema.FILE_DISK_BLOCK_POINTER_LEN == 0,
                    "direct pointer region must be an integer number of pointers");


            // Type checks (choose your policy; here: 0=FREE, 1=FILE, 2=DIR)
            require(fileType == 0 || fileType == 1 || fileType == 2, "invalid inode type (expected 0=FREE,1=FILE,2=DIR)");


            // Size checks
            require(fileSize >= 0, "sizeInBytes cannot be negative");


            // Direct pointers array checks
            require(fileDiskBlockPointers != null, "directPointers cannot be null");
            require(fileDiskBlockPointers.length == InodeSchema.TOTAL_NUMBER_OF_ADDRESSABLE_DISK_BLOCK_POINTERS,
                    "directPointers length must equal DIRECT_POINTERS_COUNT (" + InodeSchema.TOTAL_NUMBER_OF_ADDRESSABLE_DISK_BLOCK_POINTERS + ")");

            // Pointer values: 0 means "unused". Non-zero must be positive.
            for (int i = 0; i < fileDiskBlockPointers.length; i++) {
                long p = fileDiskBlockPointers[i];
                require(p >= 0, "directPointers[" + i + "] cannot be negative");
            }

            // Optional: enforce that FREE inodes must look empty
            if (fileType == 0) {
                require(fileSize == 0, "FREE inode must have sizeInBytes == 0");
                for (int i = 0; i < fileDiskBlockPointers.length; i++) {
                    require(fileDiskBlockPointers[i] == 0, "FREE inode must have all direct pointers == 0");
                }
            }

            // Optional: enforce size fits within direct pointer capacity (no indirect pointers yet)
            long maxBytesRepresentable = (long) countNonZeroPointers(fileDiskBlockPointers) * BlockToBufferDevice.BLOCK_SIZE;
            require(fileSize <= maxBytesRepresentable,
                    "sizeInBytes exceeds capacity of provided direct pointers (no indirect blocks yet)");
        }

        // ---- Invariant helpers (put inside Builder) ----
        private static int countNonZeroPointers(long[] ptrs) {
            int c = 0;
            for (long p : ptrs) if (p != 0) c++;
            return c;
        }


        private static void require(boolean condition, String message) {
            if (!condition) throw new IllegalArgumentException(message);
        }
    }

    /**
     * PURPOSE: tells how to interpret the inode's data blocks [on disk]
     *
     * [ fileType   (1 byte)    ]
     * [ fileSize   (8 bytes)   ]
     * [ fileDiskBlockPointer   (8 bytes)   ]
     *
     */
    public static class InodeSchema {
        static final int FILE_TYPE_BYTE_OFFSET = 0;
        static final int FILE_TYPE_LEN = 1;

        static final int FILE_SIZE_BYTE_OFFSET = 8;
        static final int FILE_SIZE_LEN = 8;

        static final int FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET = 16;
        static final int FILE_DISK_BLOCK_POINTER_LEN = 8;
        static final int TOTAL_NUMBER_OF_ADDRESSABLE_DISK_BLOCK_POINTERS = (INODE_SIZE - FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET) / FILE_DISK_BLOCK_POINTER_LEN;

    }


}
