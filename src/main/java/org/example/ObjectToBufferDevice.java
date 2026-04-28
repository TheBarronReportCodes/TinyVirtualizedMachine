package org.example;

/**
 * encode: write in memory object to buffer [prep for movement to disk]
 * decode: read in memory object from buffer [prep for use in memory]
 */
public class ObjectToBufferDevice {

    static byte[] encodeDiskMetadataObjectIntoBuffer(DiskMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("no valid disk metadata found");
        }
        if (metadata.magicSignature == null) {
            throw new IllegalArgumentException("magic signature cannot be null");
        }
        if (metadata.magicSignature.length != DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_LEN) {
            throw new IllegalArgumentException("no valid magic signature found");
        }

        byte[] ramBuffer = new byte[BlockToBufferDevice.BLOCK_SIZE];

        // ---- magic ----
        int x = 0;
        while (x < DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_LEN) {
            ramBuffer[DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_BYTE_OFFSET + x] = metadata.magicSignature[x];
            x++;
        }

        // ---- version ----
        ramBuffer[DiskMetadata.DiskMetadataSchema.VERSION_BYTE_OFFSET] = metadata.version;


        // ---- helper: write long little-endian ----
        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.TOTAL_ADDRESSABLE_DISK_BLOCKS_BYTE_OFFSET,
                metadata.totalAddressableBlocks);


        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.INODE_BITMAP_REGION_START_BLOCK_BYTE_OFFSET,
                metadata.inodeBitmapRegionStartBlock);


        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.INODE_BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET,
                metadata.inodeBitmapRegionBlockCount);

        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.DISK_BLOCK_BITMAP_REGION_START_BLOCK_BYTE_OFFSET,
                metadata.diskblockBitmapRegionStartBlock);

        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.DISK_BLOCK_BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET,
                metadata.diskblockBitmapRegionBlockCount);


        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.INODE_TABLE_REGION_START_BLOCK_BYTE_OFFSET,
                metadata.inodeTableRegionStartBlock);


        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.INODE_TABLE_REGION_BLOCK_COUNT_BYTE_OFFSET,
                metadata.inodeTableRegionBlockCount);


        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.DATA_REGION_START_BLOCK_BYTE_OFFSET,
                metadata.dataRegionStartBlock);


        return ramBuffer;
    }

    static DiskMetadata decodeBufferIntoDiskMetadataObject(byte[] ramBuffer) {
        if (ramBuffer == null) {
            throw new IllegalArgumentException("ramBuffer cannot be null");
        }
        if (ramBuffer.length != BlockToBufferDevice.BLOCK_SIZE) {
            throw new IllegalArgumentException("invalid buffer size");
        }

        // ---- magic ----
        byte[] magicSignature = new byte[DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_LEN];
        int x = 0;
        while (x < DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_LEN) {
            magicSignature[x] = ramBuffer[DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_BYTE_OFFSET + x];
            x++;
        }


        // ---- version ----
        byte version = ramBuffer[DiskMetadata.DiskMetadataSchema.VERSION_BYTE_OFFSET];


        // ---- helper: read long little-endian ----
        long totalAddressableDiskBlocks =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.TOTAL_ADDRESSABLE_DISK_BLOCKS_BYTE_OFFSET);


        long inodeBitmapRegionStartBlock =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.INODE_BITMAP_REGION_START_BLOCK_BYTE_OFFSET);


        long inodeBitmapRegionBlockCount =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.INODE_BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET);

        long diskBlockBitmapRegionStartBlock =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.DISK_BLOCK_BITMAP_REGION_START_BLOCK_BYTE_OFFSET);


        long diskBlockBitmapRegionBlockCount =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.DISK_BLOCK_BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET);


        long inodeTableRegionStartBlock =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.INODE_TABLE_REGION_START_BLOCK_BYTE_OFFSET);


        long inodeTableRegionBlockCount =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.INODE_TABLE_REGION_BLOCK_COUNT_BYTE_OFFSET);


        long dataRegionStartBlock =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.DATA_REGION_START_BLOCK_BYTE_OFFSET);


        return new DiskMetadata.Builder()
                .magicSignature(magicSignature)
                .version(version)
                .totalAddressableBlocks(totalAddressableDiskBlocks)
                .inodeBitmapRegionStartBlock(inodeBitmapRegionStartBlock)
                .inodeBitmapRegionBlockCount(inodeBitmapRegionBlockCount)
                .diskBlockBitmapRegionStartBlock(diskBlockBitmapRegionStartBlock)
                .diskBlockBitmapRegionBlockCount(diskBlockBitmapRegionBlockCount)
                .inodeTableRegionStartBlock(inodeTableRegionStartBlock)
                .inodeTableRegionBlockCount(inodeTableRegionBlockCount)
                .dataRegionStartBlock(dataRegionStartBlock)
                .build();
    }

    static byte[] encodeInodeObjectIntoBuffer(Inode metadata) {         // Inode -> byte[128]
        if (metadata == null) {
            throw new IllegalArgumentException("no valid disk metadata found");
        }

        byte[] ramBuffer = new byte[Inode.INODE_SIZE];

        writeLongLE(ramBuffer, Inode.InodeSchema.FILE_TYPE_BYTE_OFFSET, metadata.fileType);
        writeLongLE(ramBuffer, Inode.InodeSchema.FILE_SIZE_BYTE_OFFSET, metadata.fileSize);

        int i = 0;
        int pointersOffset = 0;
        while(i < metadata.fileDiskBlockPointers.length) {
            writeLongLE(ramBuffer, Inode.InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET + pointersOffset, metadata.fileDiskBlockPointers[i]);
            pointersOffset += Inode.InodeSchema.FILE_DISK_BLOCK_POINTER_LEN;
            i++;
        }

        return ramBuffer;
    }

    static Inode decodeBufferIntoInodeObject(byte[] ramBuffer) {             // byte[128] -> Inode
        if(ramBuffer == null) {
            throw new IllegalArgumentException("ramBuffer cannot be null");
        }
        if(ramBuffer.length != Inode.INODE_SIZE) {
            throw new IllegalArgumentException("invalid buffer size");
        }

        byte fileType = ramBuffer[Inode.InodeSchema.FILE_TYPE_BYTE_OFFSET];

        long fileSize = readLongLE(ramBuffer, Inode.InodeSchema.FILE_SIZE_BYTE_OFFSET);

        long[] diskBlockPointers = new long[Inode.InodeSchema.TOTAL_NUMBER_OF_ADDRESSABLE_DISK_BLOCK_POINTERS];

        int i = 0;
        int pointerOffset = 0;
        while(i < diskBlockPointers.length) {
            diskBlockPointers[i] = readLongLE(ramBuffer, Inode.InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET + pointerOffset);

            pointerOffset = pointerOffset + Inode.InodeSchema.FILE_DISK_BLOCK_POINTER_LEN;
            i++;
        }

        return new Inode.Builder()
                    .setFileType(fileType)
                    .setFileSize(fileSize)
                    .setFileDiskBlockPointers(diskBlockPointers)
                    .build();

    }

    static DirectoryEntry decodeBufferIntoDirectoryEntryObject() {

        return null;
    }

    static byte[] encodeDirectoryEntryObjectIntoBuffer(DirectoryEntry directoryEntry) {

        return null;
    }


    // -------------------------
    // Little-endian primitives
    // -------------------------
    private static void writeLongLE(byte[] buffer, int offset, long value) {
        int i = 0;
        while (i < 8) {                                               // Loop 8 times -> 64 bits
            buffer[offset + i] = (byte) ((value >> (8 * i)) & 0xFF);  // Shift one byte from the 64-bit object and move into the buffer
            i++;
        }
    }

    private static long readLongLE(byte[] buffer, int offset) {
        long value = 0;                                         // This will accumulate the final 64-bit number.
        int i = 0;
        while (i < 8) {                                         // Loop 8 times -> 64 bits
            long unsignedByte = buffer[offset + i] & 0xFFL;     // Read one byte at a time from the buffer
            value |= (unsignedByte << (8 * i));                 // Shift the byte into the 64-bit object
            i++;
        }
        return value;
    }


}
