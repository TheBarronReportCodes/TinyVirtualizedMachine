package org.example;

/**
 * PURPOSE: Given an index, retrieve or persist a bitmap to/from disk
 *
 * reads and write a bitmap to/from disk
 *
 * NOTE: does not read and write the bitmap OBJECT, only the ARRAY within the object
 *
 * Uses:
 *
 * the bitmap (array/buffer only)
 * this blockToBufferDevice
 *
 * So yes, it should know:
 *
 * where the bitmap region starts
 * where the inode bitmap is in the bitmap region
 * where the disk block bitmap is in the bitmap region
 * how large that region is
 * how to read/write bits
 *
 */
public class BitmapObjectToDiskDevice {
    Bitmapper inodeAllocationBitmapper;
    Bitmapper diskBlockAllocationBitmapper;
    DiskMetadata diskMetadata;

    public BitmapObjectToDiskDevice(DiskMetadata diskMetadata) {
        this.diskMetadata = diskMetadata;

        long totalAddressableInodes = (BlockToBufferDevice.BLOCK_SIZE / InodeObjectToDiskDevice.INODE_SIZE) * this.diskMetadata.inodeTableRegionBlockCount;

        this.inodeAllocationBitmapper = new Bitmapper(totalAddressableInodes);
        this.diskBlockAllocationBitmapper = new Bitmapper(diskMetadata.totalAddressableBlocks);
    }

    public Bitmapper readInodeBitmapFromDisk() {

    }

    public byte[] readDiskBlockBitmapFromDisk() {
    }



}
