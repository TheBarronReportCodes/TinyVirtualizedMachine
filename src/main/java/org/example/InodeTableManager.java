package org.example;

import java.io.IOException;

/**
 *
 * reads and writes an inodeBuffer to and from disk; the inode table is not supposed to live fully in RAM
 *
 * Uses:
 *
 * the disk layout metadata layer
 * the block I/O layer
 * the inode encoder/decoder layer
 *
 * So yes, it should know:
 *
 * where the inode table region starts
 * how large that region is
 * how to read/write blocks
 * how to turn bytes into an Inode object
 * how to turn an Inode object back into bytes
 */
public class InodeTableManager {
    static int INODE_SIZE = 128;
    DiskMetadata diskMetadata;
    BlockToBufferDevice blockToBufferDevice;
    final int inodesPerBlock;
    final long totalAddressableInodes;

    public InodeTableManager(DiskMetadata diskMetadata, BlockToBufferDevice blockToBufferDevice) {
        if (diskMetadata == null) {
            throw new IllegalArgumentException("metadata cannot be null");
        }

        this.diskMetadata = diskMetadata;
        this.blockToBufferDevice = blockToBufferDevice;

        this.inodesPerBlock = BlockToBufferDevice.BLOCK_SIZE / INODE_SIZE;
        this.totalAddressableInodes = inodesPerBlock * diskMetadata.inodeTableRegionBlockCount;
    }

    public byte[] readInodeFromDisk(int inodeIndex) throws IOException {

        int blockIndex = inodeIndex / this.inodesPerBlock;
        int inodeSlotIndex = inodeIndex % this.inodesPerBlock;

        byte[] ramBuffer = this.blockToBufferDevice.readBlockIntoBuffer(this.diskMetadata.inodeTableRegionStartBlock + blockIndex);

        int x = 0;
        byte[] inodeBuffer = new byte[INODE_SIZE];

        while(x < INODE_SIZE) {
            inodeBuffer[x] = ramBuffer[(inodeSlotIndex * INODE_SIZE) + x];
            x++;
        }

        return inodeBuffer;
    }

    //writeInode
    //clearInode





}
