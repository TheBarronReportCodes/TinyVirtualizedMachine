package org.example;

//Buffer to Object Interface
public class ObjectToBufferDevice {

    static byte[] encodeDiskMetadataObjectIntoBuffer(DiskMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("no valid superblock metadata found");
        }
        if (metadata.magicSignature == null) {
            throw new IllegalArgumentException("magic signature cannot be null");
        }
        if (metadata.magicSignature.length != DiskMetadata.DiskMetadataSchema.MAGIC_SIGNATURE_LEN) {
            throw new IllegalArgumentException("no valid superblock magic signature found");
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
            throw new IllegalArgumentException("invalid superblock size");
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

    // -------------------------
    // Little-endian primitives
    // -------------------------
    private static void writeLongLE(byte[] block, int offset, long value) {
        int i = 0;
        while (i < 8) {
            block[offset + i] = (byte) ((value >> (8 * i)) & 0xFF);
            i++;
        }
    }

    private static long readLongLE(byte[] block, int offset) {
        long value = 0;
        int i = 0;
        while (i < 8) {
            long unsignedByte = block[offset + i] & 0xFFL;
            value |= (unsignedByte << (8 * i));
            i++;
        }
        return value;
    }


}
