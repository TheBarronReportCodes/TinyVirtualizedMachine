package org.example;

/**
 * PURPOSE: stores and manages a list of DirectoryEntries
 *
 * map data structure (directory) -> manages key value pairs (directoryEntries)
 *
 * interact with a DirectoryEntry Array (search, add, remove, list)
 *
 */
public class DirectoryEntryManager {
    DirectoryEntry[] directoryEntries;         // map data structure

    public DirectoryEntryManager(DirectoryEntry[] directoryEntries) {
        this.directoryEntries = directoryEntries;
    }

    DirectoryEntry lookup(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        int x = 0;
        while (x < this.directoryEntries.length) {
            DirectoryEntry entry = this.directoryEntries[x];

            if (entry != null && key.equals(entry.name)) {
                return entry;
            }

            x++;
        }

        return null;
    }

    void addEntry(DirectoryEntry directoryEntry) {
        // 1. validate not null
        if (directoryEntry == null) {
            throw new IllegalArgumentException("directory entry object cannot be null");
        }

        int x = 0;

        while (x < this.directoryEntries.length) {
            DirectoryEntry current = this.directoryEntries[x];

            // 2. make sure name does not already exist
            if (current != null && directoryEntry.name.equals(current.name)) {
                throw new IllegalArgumentException("name already exists");
            }

            // 3. find first null slot
            if (current == null) {
                // 4. place entry there
                this.directoryEntries[x] = directoryEntry;
                return; // stop after inserting
            }

            x++;
        }

        // no free slot found
        throw new IllegalStateException("directory is full");

    }

    void removeEntry(String key) {
        // 1. find matching entry
        // 2. set that array slot to null
    }
}
