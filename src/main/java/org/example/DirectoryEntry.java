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
    String name;
    int inodeIndex;

    public DirectoryEntry(String name, int inodeIndex) {
        this.name = name;
        this.inodeIndex = inodeIndex;
    }
}
