package org.example;


import java.io.IOException;

// orchestrates calls to the encoding/decoding and read/write layers [INTERFACE]; can encode all or part of the disk. allows for disk virtualization
public class Volume {
    BlockToBufferDevice blockToBufferDevice;    // buffer → disk
    ObjectToBufferDevice objectToBufferDevice;  // buffer → object
    DiskMetadata diskMetadata;
    FileMetadata fileMetadata;
    boolean isMounted;
    static final int SUPERBLOCK_LOCATION = 0;

    public Volume(BlockToBufferDevice blockToBufferDevice, DiskMetadata diskMetadata, FileMetadata fileMetadata) {
        this.blockToBufferDevice = blockToBufferDevice;
        this.diskMetadata = diskMetadata;
        this.fileMetadata = fileMetadata;
    }

    public void format() throws IOException {      // object → buffer → disk [METADATA]

        byte[] superblockBuffer = ObjectToBufferDevice.encodeDiskMetadataObjectIntoBuffer(this.diskMetadata);

        this.blockToBufferDevice.writeBufferIntoBlock(SUPERBLOCK_LOCATION, superblockBuffer);

    }

    public void format(BitmapRegion bitmapRegion, InodeObjectToDiskDevice inodeRegion) throws IOException {                                // object → buffer → disk [REGIONS]

    }

    public void mount() throws IOException {                                                         // disk → buffer → object
    }

    public void flush() {                                                                            // object → buffer → disk [ANY DATA — whether it’s metadata or not]
    }

    public void unmount() {                                                                          // call flush, delete object, change state

    }


}