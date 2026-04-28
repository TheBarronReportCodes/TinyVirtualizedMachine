package org.example;


import java.io.IOException;

/**
 * PURPOSE: Represent a mounted filesystem instance [disk.img  → Volume]
 *
 * HOLDS:
 *
 * DiskMetadata
 * BlockToBufferDevice
 * DiskBlockBitmapper
 * InodeBitmapper
 * InodeObjectToDiskDevice
 * BitmapObjectToDiskDevice
 * DirectoryEntryManager (for root initially)
 *
 * FILE/DIRECTORY API [exposes high-level operations]:
 *
 * createFile(String name)
 * readFile(String name)
 * writeFile(String name, byte[] data)
 * deleteFile(String name)
 * createDirectory(String name)
 * listDirectory()
 *
 */

// orchestrates calls to the encoding/decoding and read/write layers [INTERFACE]; can encode all or part of the disk. allows for disk virtualization
public class Volume {
    BlockToBufferDevice blockToBufferDevice;    // buffer → disk
    ObjectToBufferDevice objectToBufferDevice;  // buffer → object
    DiskMetadata diskMetadata;
    Inode inode;
    boolean isMounted;
    static final int SUPERBLOCK_LOCATION = 0;

    public Volume(BlockToBufferDevice blockToBufferDevice, DiskMetadata diskMetadata, Inode inode) {
        this.blockToBufferDevice = blockToBufferDevice;
        this.diskMetadata = diskMetadata;
        this.inode = inode;
    }

    public void format() throws IOException {      // object → buffer → disk [METADATA]

        byte[] superblockBuffer = ObjectToBufferDevice.encodeDiskMetadataObjectIntoBuffer(this.diskMetadata);

        this.blockToBufferDevice.writeBufferIntoBlock(SUPERBLOCK_LOCATION, superblockBuffer);

    }

    public void format(BitmapObjectToDiskDevice bitmapRegion, InodeObjectToDiskDevice inodeRegion) throws IOException {                                // object → buffer → disk [REGIONS]

    }

    public void mount() throws IOException {                                                         // disk → buffer → object
    }

    public void flush() {                                                                            // object → buffer → disk [ANY DATA — whether it’s metadata or not]
    }

    public void unmount() {                                                                          // call flush, delete object, change state

    }


}