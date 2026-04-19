package org.example;

import java.io.IOException;

/**
 * PURPOSE: Given an index, retrieve or persist fileMetadata to/from Disk
 *
 * reads and writes an inodeBuffer to and from disk; the inode table is not supposed to live fully in RAM
 *
 * Uses:
 *
 * the DiskMetadata
 * the BlockToBufferDevice
 * the ObjectToBufferDevice
 *
 * So yes, it should know:
 *
 * where the inode table region starts
 * how large that region is
 * how to read/write blocks
 * how to turn bytes into an Inode object
 * how to turn an Inode object back into bytes
 */
public class InodeObjectToDiskDevice {
    static int INODE_SIZE = 128;
    DiskMetadata diskMetadata;
    BlockToBufferDevice blockToBufferDevice;
    final int inodesPerBlock;
    final long totalAddressableInodes;

    public InodeObjectToDiskDevice(DiskMetadata diskMetadata, BlockToBufferDevice blockToBufferDevice) {
        if (diskMetadata == null) {
            throw new IllegalArgumentException("metadata cannot be null");
        }

        this.diskMetadata = diskMetadata;
        this.blockToBufferDevice = blockToBufferDevice;

        this.inodesPerBlock = BlockToBufferDevice.BLOCK_SIZE / INODE_SIZE;
        this.totalAddressableInodes = inodesPerBlock * diskMetadata.inodeTableRegionBlockCount;
    }

    /**
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     */
    public FileMetadata readInodeObjectFromDisk(int inodeIndex) throws IOException {

        int blockIndex = inodeIndex / this.inodesPerBlock;
        int inodeIndexInsideBlock = inodeIndex % this.inodesPerBlock;
        int inodeByteOffsetInsideBlock = inodeIndexInsideBlock * INODE_SIZE;

        //BLOCK TO BUFFER
        byte[] blockBuffer = this.blockToBufferDevice.readBlockIntoBuffer(this.diskMetadata.inodeTableRegionStartBlock + blockIndex);

        //BUFFER TO BUFFER
        int x = 0;
        byte[] inodeBuffer = new byte[INODE_SIZE];

        while(x < INODE_SIZE) {
            inodeBuffer[x] = blockBuffer[(inodeByteOffsetInsideBlock) + x];
            x++;
        }

        //BUFFER TO OBJECT
        return ObjectToBufferDevice.decodeBufferIntoFileMetadataObject(inodeBuffer);
    }

    /**
     * NOTE: Modulo (%) and division (/) show up everywhere because they let you split a linear value into structure.
     *
     * At a high level:
     *
     * division = which group?
     * modulo = where inside the group?
     *
     */
    public void writeInodeObjectToDisk(int inodeIndex, FileMetadata fileMetadata) throws IOException {

        int blockIndex = inodeIndex / this.inodesPerBlock;
        int inodeIndexInsideBlock = inodeIndex % this.inodesPerBlock;
        int inodeByteOffsetInsideBlock = inodeIndexInsideBlock * INODE_SIZE;

        //OBJECT TO BUFFER
        byte[] inodeBuffer = ObjectToBufferDevice.encodeFileMetadataObjectIntoBuffer(fileMetadata);

        //BUFFER TO BUFFER
        byte[] blockBuffer = this.blockToBufferDevice.readBlockIntoBuffer(this.diskMetadata.inodeTableRegionStartBlock + blockIndex);

        int x = 0;
        while(x < INODE_SIZE) {
            blockBuffer[inodeByteOffsetInsideBlock + x] = inodeBuffer[x];
            x++;
        }

        //BUFFER TO BLOCK
        this.blockToBufferDevice.writeBufferIntoBlock(blockIndex, blockBuffer);
    }

}
