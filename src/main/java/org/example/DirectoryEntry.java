package org.example;

/**
 * PURPOSE: small object that maps a name to an inode index. a name is an easier way to refer to an index
 *
 * KEY: name
 * VALUE: inodeIndex
 *
 * VIEW: [
 *          ("notes.txt", 5),
 *          ("photo.jpg", 12),
 *          ("docs", 20)
 *       ]
 */
public class DirectoryEntry {
    static int DIRECTORY_ENTRY_SIZE = 272;
    String name;
    int inodeIndex;

    private DirectoryEntry(Builder builder) {
        this.name = builder.name;
        this.inodeIndex = builder.inodeIndex;
    }

    public static class Builder {
        String name;
        int inodeIndex;

        public void setName(String name) {
            this.name = name;
        }
        public void setInodeIndex(int inodeIndex) {
            this.inodeIndex = inodeIndex;
        }

        public DirectoryEntry build() {
            return new DirectoryEntry(this);
        }
    }

    /**
     * PURPOSE: tells how to interpret the directory's data blocks [on disk]
     *
     * [ name       (255 bytes) ]
     * [ inodeIndex (4 bytes) ]
     *
     */
    public static class DirectoryEntrySchema {
        static final int NAME_BYTE_OFFSET = 0;
        static final int NAME_LEN = 255;

        static final int INODE_INDEX_BYTE_OFFSET = 264;
        static final int INODE_INDEX_LEN = 8;

    }
}
