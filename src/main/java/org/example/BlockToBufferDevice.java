package org.example;

import java.io.*;

// A block-level abstraction over a Disk (static) + Buffer to Block Interface
public class BlockToBufferDevice {
    static final int BLOCK_SIZE = 4096;                                     //the size of a "disk block"
    final RandomAccessFile disk;

    public BlockToBufferDevice() throws IOException {
        this.disk = new RandomAccessFile("disk.img", "rw");     //the file in this use case is standing in to represent "the disk"

        if(this.disk.length() == 0) {
            throw new IllegalArgumentException("disk is empty");
        }
    }

    private long getDiskBlockOffset(long blockIndex) {                       //disk block position
        return (long) blockIndex * BLOCK_SIZE;
    }

    public long getTotalAddressableDiskBlocks() throws IOException {        //total number of disk blocks
        long remainder = this.disk.length() % BLOCK_SIZE;

        if(remainder != 0) {
            throw new IllegalArgumentException("corrupted disk. not multiple of block size");
        }

        return this.disk.length() / BLOCK_SIZE;
    }

    public byte[] readBlockIntoBuffer(long blockIndex) throws IOException {
        long totalAddressableBlocks = this.getTotalAddressableDiskBlocks();

        if(blockIndex >= totalAddressableBlocks) {
            throw new IllegalArgumentException("block index cannot exceed available number of blocks");
        }
        if(blockIndex < 0) {
            throw new IllegalArgumentException("block index cannot be negative");
        }



        byte[] ramBuffer = new byte[BLOCK_SIZE];                                //buffer for receiving bytes from disk block
        long diskBlockOffset = this.getDiskBlockOffset(blockIndex);             //calculate disk block position
        this.disk.seek(diskBlockOffset);                                        //move actual disk head position to calculated block position [SYSCALL]


        int ramBufferRemainingLength = ramBuffer.length;
        int ramBufferStartingOffset = 0;

        while(ramBufferRemainingLength != 0) {
            int r = this.disk.read(ramBuffer, ramBufferStartingOffset, ramBufferRemainingLength);      //read into ramBuffer. offset into the buffer depending on how many byte still need to be read from disk [SYSCALL]

            if(r == 0) {
                throw new IllegalArgumentException("block read stalled");                                    //reading 0 bytes will stall the program
            }
            if(r == -1) {
                throw new IllegalArgumentException("EOF detected");
            }


            ramBufferRemainingLength = ramBufferRemainingLength - r;

            ramBufferStartingOffset = ramBufferStartingOffset + r;
        }

        return ramBuffer;
    }

    public void writeBufferIntoBlock(long blockIndex, byte[] ramBuffer) throws IOException {
        long totalAddressableBlocks = this.getTotalAddressableDiskBlocks();

        if(blockIndex >= totalAddressableBlocks) {
            throw new IllegalArgumentException("block index cannot exceed available number of blocks");
        }
        if(blockIndex < 0) {
            throw new IllegalArgumentException("block index cannot be negative");
        }
        if(ramBuffer == null) {
            throw new IllegalArgumentException("data block cannot be null");
        }
        if(ramBuffer.length != BLOCK_SIZE) {
            throw new IllegalArgumentException("data block does not meet the valid block size");
        }


        long diskBlockOffset = this.getDiskBlockOffset(blockIndex);                   //calculate disk block position
        this.disk.seek(diskBlockOffset);                                              //move actual disk head position to calculated block position [SYSCALL]

        this.disk.write(ramBuffer, 0, ramBuffer.length);                          //all or nothing. Either write the requested length or throws. [SYSCALL]

    }


}
