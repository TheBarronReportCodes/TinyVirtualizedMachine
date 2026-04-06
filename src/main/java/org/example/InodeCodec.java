package org.example;

//encoder/decoder of inode metadata to/from a record (using the schema); independent of where it sits on disk
public class InodeCodec {

    // Encode an Inode into a fixed-size inode record (INODE_SIZE bytes).
    public static byte[] encodeInode(InodeMetadata metadata) {
        if (metadata == null) throw new IllegalArgumentException("inode cannot be null");
        if (metadata.directPointers == null) throw new IllegalArgumentException("directPointers cannot be null");
        if (metadata.directPointers.length != InodeMetadata.InodeMetadataSchema.DIRECT_POINTERS_COUNT) {
            throw new IllegalArgumentException(
                    "directPointers length must be " + InodeMetadata.InodeMetadataSchema.DIRECT_POINTERS_COUNT);
        }


        byte[] record = new byte[InodeTable.INODE_SIZE_IN_BYTES];


        // type (1 byte)
        record[InodeMetadata.InodeMetadataSchema.TYPE_OFFSET] = metadata.type;


        // size (8 bytes, little-endian)
        putLongLE(record, InodeMetadata.InodeMetadataSchema.SIZE_IN_BYTES_OFFSET, metadata.sizeInBytes);
        // you cannot store a long in a byte array directly
        // you must split it into 8 bytes
        // using: little-endian or big-endian...little endian for our example
        // implementation: right shift to get the portion we want, convert it to a byte, store it in inodeByteArray


        // direct pointers (each 4 bytes, little-endian)
        int i = 0;
        while (i < InodeMetadata.InodeMetadataSchema.DIRECT_POINTERS_COUNT) {
            int slot = InodeMetadata.InodeMetadataSchema.DIRECT_POINTERS_OFFSET
                    + (InodeMetadata.InodeMetadataSchema.POINTER_SIZE_IN_BYTES * i);


            putIntLE(record, slot, metadata.directPointers[i]);
            i++;
        }


        return record;
    }

    // Decode a fixed-size inode record (INODE_SIZE bytes) back into an Inode.
    public static InodeMetadata decodeInode(byte[] inodeRecord) {
        if (inodeRecord == null) throw new IllegalArgumentException("inodeRecord cannot be null");
        if (inodeRecord.length != InodeTable.INODE_SIZE_IN_BYTES) {
            throw new IllegalArgumentException("inodeRecord must be exactly INODE_SIZE bytes");
        }


        byte type = inodeRecord[InodeMetadata.InodeMetadataSchema.TYPE_OFFSET];
        long sizeInBytes = getLongLE(inodeRecord, InodeMetadata.InodeMetadataSchema.SIZE_IN_BYTES_OFFSET);

        int[] directPointers = new int[InodeMetadata.InodeMetadataSchema.DIRECT_POINTERS_COUNT];
        int i = 0;
        while (i < directPointers.length) {
            int slot = InodeMetadata.InodeMetadataSchema.DIRECT_POINTERS_OFFSET
                    + (InodeMetadata.InodeMetadataSchema.POINTER_SIZE_IN_BYTES * i);


            directPointers[i] = getIntLE(inodeRecord, slot);
            i++;
        }


        return new InodeMetadata.Builder()
                .type(type)
                .sizeInBytes(sizeInBytes)
                .directPointers(directPointers)
                .build();
    }

    // -------------------------
    // Little-endian primitives
    // -------------------------
    private static void putIntLE(byte[] buf, int offset, int value) {
        requireRange(buf, offset, 4);
        int i = 0;
        while (i < 4) {
            buf[offset + i] = (byte) ((value >> (8 * i)) & 0xFF);
            i++;
        }
    }


    private static int getIntLE(byte[] buf, int offset) {
        requireRange(buf, offset, 4);
        int value = 0;
        int i = 0;
        while (i < 4) {
            int unsignedByte = buf[offset + i] & 0xFF;
            value |= (unsignedByte << (8 * i));
            i++;
        }
        return value;
    }


    private static void putLongLE(byte[] buf, int offset, long value) {
        requireRange(buf, offset, 8);
        int i = 0;
        while (i < 8) {
            buf[offset + i] = (byte) ((value >> (8 * i)) & 0xFF);
            i++;
        }
    }


    private static long getLongLE(byte[] buf, int offset) {
        requireRange(buf, offset, 8);
        long value = 0;
        int i = 0;
        while (i < 8) {
            long unsignedByte = buf[offset + i] & 0xFFL;
            value |= (unsignedByte << (8 * i));
            i++;
        }
        return value;
    }


    private static void requireRange(byte[] buf, int offset, int len) {
        if (offset < 0) throw new IllegalArgumentException("offset cannot be negative");
        if (len < 0) throw new IllegalArgumentException("len cannot be negative");
        if (offset + len > buf.length) {
            throw new IllegalArgumentException("range exceeds buffer: offset=" + offset + " len=" + len + " bufLen=" + buf.length);
        }
    }
}
