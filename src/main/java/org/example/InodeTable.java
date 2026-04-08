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
    static int INODE_SIZE = 128;


}
