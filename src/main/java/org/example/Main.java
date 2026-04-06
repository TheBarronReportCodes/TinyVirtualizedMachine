package org.example;


import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BlockToBufferDevice blockToBufferDevice1 = new BlockToBufferDevice();

        ////////////BLOCKDEVICE TEST////////
        byte[] buffer1 = new byte[4096];
        int i = 0;
        while(i < buffer1.length) {
            buffer1[i] = (byte) (i % 256);
            i++;
        }
        blockToBufferDevice1.writeBufferIntoBlock(1, buffer1);


        byte[] buffer2 = blockToBufferDevice1.readBlockIntoBuffer(1);
        int k = 0;
        while(k < buffer2.length) {
            System.out.println(buffer2[k]);
            k++;
        }

        ///////////////SUPERBLOCK + VOLUME TEST///////////
        byte[] magicSignature = {(byte) 0x7F, // 01111111  — classic sentinel (used in ELF: 0x7F 'E' 'L' 'F')
                                (byte) 0x44, // 01100100  — ASCII 'D'   (disk)
                                (byte) 0x44, // 01100100  — ASCII 'D'   (demo)
                                (byte) 0x46, // 01000110  — ASCII 'F'   (file)
                                (byte) 0x53};// 01010011  — ASCII 'S'   (system)
        byte version = (byte) 0x02;
        long totalAddressableDiskBlocks = blockToBufferDevice1.getTotalAddressableDiskBlocks();

//        SuperBlockMetadata superBlockMetadata = volume1.createSuperBlockMetadata(magicSignature, version, totalAddressableDiskBlocks);
//        volume1.format(superBlockMetadata);

        byte[] diskBlock0 = blockToBufferDevice1.readBlockIntoBuffer(0);
        int j = 0;
        while(j < diskBlock0.length) {
            System.out.println(diskBlock0[j]);
            j++;
        }



    }
}