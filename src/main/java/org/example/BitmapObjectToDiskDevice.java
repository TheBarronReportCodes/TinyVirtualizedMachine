package org.example;

import java.io.IOException;

/**
 * “Where does the bitmapper's bitmap live on disk and how do I store it?”
 */

public class BitmapObjectToDiskDevice {
    DiskMetadata diskMetadata;
    BlockToBufferDevice blockToBufferDevice;

    public BitmapObjectToDiskDevice(DiskMetadata diskMetadata, BlockToBufferDevice blockToBufferDevice) {
        this.diskMetadata = diskMetadata;
        this.blockToBufferDevice = blockToBufferDevice;
    }

    void initializeInodeBitmapRegionOnDisk() throws IOException {
        byte[] emptyBitmapBlock = new byte[BlockToBufferDevice.BLOCK_SIZE];

        this.blockToBufferDevice.writeBufferIntoBlock(
                this.diskMetadata.inodeBitmapRegionStartBlock,
                emptyBitmapBlock
        );
    }

    void initializeDiskBlockBitmapRegionOnDisk() throws IOException {
        byte[] emptyBitmapBlock = new byte[BlockToBufferDevice.BLOCK_SIZE];

        this.blockToBufferDevice.writeBufferIntoBlock(
                this.diskMetadata.diskblockBitmapRegionStartBlock,
                emptyBitmapBlock
        );
    }

    byte[] readInodeBitmapFromDisk() throws IOException {
        long totalAddressableInodes = (BlockToBufferDevice.BLOCK_SIZE / InodeObjectToDiskDevice.INODE_SIZE) * this.diskMetadata.inodeTableRegionBlockCount;

        Bitmapper bitmapper = new Bitmapper(totalAddressableInodes);

        if (bitmapper.bitmap.length > BlockToBufferDevice.BLOCK_SIZE) {
            throw new IllegalArgumentException("inode bitmap does not fit in one block");
        }

        byte[] blockBuffer = this.blockToBufferDevice.readBlockIntoBuffer(this.diskMetadata.inodeBitmapRegionStartBlock);

        int x = 0;

        while(x < bitmapper.bitmap.length) {
            bitmapper.bitmap[x] = blockBuffer[x];
            x++;
        }

        return bitmapper.bitmap;
    }

    byte[] readDiskBlockBitmapFromDisk() throws IOException {

        Bitmapper bitmapper = new Bitmapper(this.diskMetadata.totalAddressableBlocks);

        if (bitmapper.bitmap.length > BlockToBufferDevice.BLOCK_SIZE) {
            throw new IllegalArgumentException("disk block bitmap does not fit in one block");
        }

        byte[] blockBuffer = this.blockToBufferDevice.readBlockIntoBuffer(this.diskMetadata.diskblockBitmapRegionStartBlock);

        int x = 0;

        while(x < bitmapper.bitmap.length) {
            bitmapper.bitmap[x] = blockBuffer[x];
            x++;
        }

        return bitmapper.bitmap;
    }

    void writeInodeBitmap(Bitmapper bitmapper) throws IOException {

        byte[] blockBuffer = new byte[BlockToBufferDevice.BLOCK_SIZE];

        if (bitmapper.bitmap.length > BlockToBufferDevice.BLOCK_SIZE) {
            throw new IllegalArgumentException("inode bitmap does not fit in one block");
        }

        int x = 0;

        while(x < bitmapper.bitmap.length) {
            blockBuffer[x] = bitmapper.bitmap[x];
            x++;
        }

        this.blockToBufferDevice.writeBufferIntoBlock(this.diskMetadata.inodeBitmapRegionStartBlock, blockBuffer);

    }

    void writeDiskBlockBitmap(Bitmapper bitmapper) throws IOException {

        byte[] blockBuffer = new byte[BlockToBufferDevice.BLOCK_SIZE];

        if (bitmapper.bitmap.length > BlockToBufferDevice.BLOCK_SIZE) {
            throw new IllegalArgumentException("disk block bitmap does not fit in one block");
        }

        int x = 0;

        while(x < bitmapper.bitmap.length) {
            blockBuffer[x] = bitmapper.bitmap[x];
            x++;
        }

        this.blockToBufferDevice.writeBufferIntoBlock(this.diskMetadata.diskblockBitmapRegionStartBlock, blockBuffer);

    }


}
