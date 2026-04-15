package org.example;

//  Buffer to Object Interface
//  This code does not read and write an inode object to disk; It reads and writes an inode object to and from an inode-sized byte buffer.
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
                DiskMetadata.DiskMetadataSchema.BITMAP_REGION_START_BLOCK_BYTE_OFFSET,
                metadata.bitmapRegionStartBlock);


        writeLongLE(ramBuffer,
                DiskMetadata.DiskMetadataSchema.BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET,
                metadata.bitmapRegionBlockCount);


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


        long bitmapRegionStartBlock =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.BITMAP_REGION_START_BLOCK_BYTE_OFFSET);


        long bitmapRegionBlockCount =
                readLongLE(ramBuffer, DiskMetadata.DiskMetadataSchema.BITMAP_REGION_BLOCK_COUNT_BYTE_OFFSET);


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
                .bitmapRegionStartBlock(bitmapRegionStartBlock)
                .bitmapRegionBlockCount(bitmapRegionBlockCount)
                .inodeTableRegionStartBlock(inodeTableRegionStartBlock)
                .inodeTableRegionBlockCount(inodeTableRegionBlockCount)
                .dataRegionStartBlock(dataRegionStartBlock)
                .build();
    }

    static byte[] encodeFileMetadataObjectIntoBuffer(FileMetadata metadata) {         // Inode -> byte[128]
        if (metadata == null) {
            throw new IllegalArgumentException("no valid disk metadata found");
        }

        byte[] ramBuffer = new byte[InodeTableManager.INODE_SIZE];

        writeLongLE(ramBuffer, FileMetadata.InodeSchema.FILE_TYPE_BYTE_OFFSET, metadata.fileType);
        writeLongLE(ramBuffer, FileMetadata.InodeSchema.FILE_SIZE_BYTE_OFFSET, metadata.fileSize);

        int i = 0;
        int pointersOffset = 0;
        while(i < metadata.fileDiskBlockPointers.length) {
            writeLongLE(ramBuffer, FileMetadata.InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET + pointersOffset, metadata.fileDiskBlockPointers[i]);
            pointersOffset += FileMetadata.InodeSchema.FILE_DISK_BLOCK_POINTER_LEN;
            i++;
        }

        return ramBuffer;
    }

    static FileMetadata decodeBufferIntoFileMetadataObject(byte[] ramBuffer) {             // byte[128] -> Inode
        if(ramBuffer == null) {
            throw new IllegalArgumentException("ramBuffer cannot be null");
        }
        if(ramBuffer.length != InodeTableManager.INODE_SIZE) {
            throw new IllegalArgumentException("invalid buffer size");
        }

        byte fileType = ramBuffer[FileMetadata.InodeSchema.FILE_TYPE_BYTE_OFFSET];

        long fileSize = readLongLE(ramBuffer, FileMetadata.InodeSchema.FILE_SIZE_BYTE_OFFSET);

        long[] diskBlockPointers = new long[FileMetadata.InodeSchema.TOTAL_NUMBER_OF_ADDRESSABLE_DISK_BLOCK_POINTERS];

        int i = 0;
        int pointerOffset = 0;
        while(i < diskBlockPointers.length) {
            diskBlockPointers[i] = readLongLE(ramBuffer, FileMetadata.InodeSchema.FILE_DISK_BLOCK_POINTERS_BYTE_OFFSET + pointerOffset);

            pointerOffset = pointerOffset + FileMetadata.InodeSchema.FILE_DISK_BLOCK_POINTER_LEN;
            i++;
        }

        return new FileMetadata.Builder()
                    .setFileType(fileType)
                    .setFileSize(fileSize)
                    .setFileDiskBlockPointers(diskBlockPointers)
                    .build();

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
