package org.example;

/**
 *  an inode table is a contiguous range of disk blocks
 *  each disk block in the inode table contains many inodes
 *
 *  Block 0 → superblock
 *  Block 1 → bitmap
 *  Block 2 → inode table (inodes 0–31)
 *  Block 3 → inode table (inodes 32–63)
 *  Block 4 → inode table (inodes 64–95)
 */
public class InodeTable {
    BlockToBufferDevice blockToBufferDevice;
    long startBlock;
    long blockCount;
    static final int INODE_SIZE_IN_BYTES = 128;
    long inodesPerBlock;
    long maxInodes;

    public InodeTable(BlockToBufferDevice blockToBufferDevice, DiskMetadata metadata) {
        if(blockToBufferDevice == null) {
            throw new IllegalArgumentException("block device cannot be null");
        }
        if(metadata == null) {
            throw new IllegalArgumentException("metadata cannot be null");
        }

        this.blockToBufferDevice = blockToBufferDevice;
        this.startBlock = metadata.inodeTableRegionStartBlock;
        this.blockCount = metadata.inodeTableRegionBlockCount;

        this.inodesPerBlock = BlockToBufferDevice.BLOCK_SIZE / INODE_SIZE_IN_BYTES;
        this.maxInodes = this.blockCount * this.inodesPerBlock;
    }

    public void writeInode(long inodeOffset, InodeMetadata inodeMetadata) {


    }

    public InodeMetadata readInode(long inodeOffset) {
        if(inodeOffset < 0) {
            throw new IllegalArgumentException("inode offset cannot be negative");
        }
        if(inodeOffset > maxInodes) {
            throw new IllegalArgumentException("inode offset cannot exceed the maximum number of inodes available");
        }

        //which inode-table block holds it?
        long diskBlockOffset = inodeOffset / this.inodesPerBlock;
        long inodeTableDiskBlockOffset = this.startBlock + diskBlockOffset;

        //where inside that 4096-byte block does the inode begin?
        long inodeIndexWithinDiskBlock = inodeOffset % this.inodesPerBlock;

        //access block (inodeTableDiskBlockOffset)

        //access index in block (inodeIndexWithinDiskBlock) 

    }

    public long findFreeInode() {


    }

}
