package ru.kpfu.itis.codes.fano;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

public class Decoder {
    private InputStream inputStream;
    private Writer writer;
    private CodeTree codeTree;
    private int bufferSize;


    public Decoder(InputStream inputStream, Writer writer){
        this(inputStream, writer, 1);
    }
    public Decoder(InputStream inputStream, Writer writer, int bufferSize) {
        this.inputStream = inputStream;
        this.writer = writer;
        this.bufferSize = bufferSize;
    }

    public void decode(){
        try {
            int treeSize = 0;
            byte[] treeSizeBytes = new byte[4];
            inputStream.read(treeSizeBytes);
            treeSize = ((treeSizeBytes[0] & 0xFF) << 24) | ((treeSizeBytes[1] & 0xFF) << 16) | ((treeSizeBytes[2] & 0xFF) << 8) | treeSizeBytes[3] & 0xFF;
            byte[] serializedTree = new byte[treeSize];
            inputStream.read(serializedTree);
            codeTree = CodeTree.deserialize(serializedTree);
            System.out.println("Deserialized tree:");
            System.out.println(Arrays.toString(codeTree.serialize()));
            byte[] buffer = new byte[bufferSize];
            int r;
            while ((r = inputStream.read(buffer)) == bufferSize && inputStream.available() != 0){
                BitSlicer slicer = new BitSlicer(buffer);
                while (slicer.hasNext()){
                    byte bit = slicer.nextBit();
                    System.out.print(bit);
                    String s = codeTree.move(bit);
                    if (s != null){
                        System.out.println("\nRecognized " + s);
                        writer.write(s);
                    }
                }
            }
            BitSlicer slicer = new BitSlicer(buffer, r - 2);
            while (slicer.hasNext()){
                String s = codeTree.move(slicer.nextBit());
                if (s != null){
                    writer.write(s);
                }
            }
            byte lastByte = buffer[r - 1];
            int lastByteSize = 8;
            while (lastByte % 2 == 0){
                lastByte = (byte) (lastByte >> 1);
                lastByteSize--;
            }
            lastByte = (byte) (lastByte >> 1);
            lastByteSize--;
            while (lastByteSize > 0){
                lastByteSize--;
                String s = codeTree.move((byte) ((lastByte >> lastByteSize) & 1));
                if(s != null){
                    writer.write(s);
                }
            }
            inputStream.close();
            writer.close();

        } catch (IOException e){
            throw new IllegalArgumentException(e);
        }
    }

    private static class BitSlicer{
        int currentBitPosition = 0;
        int currentBytePosition = 0;
        int endPosition;
        byte[] buffer;

        public BitSlicer(byte[] buffer) {
            this.buffer = buffer;
            endPosition = buffer.length;
        }

        public BitSlicer(byte[] buffer, int endPosition) {
            this.endPosition = endPosition;
            this.buffer = buffer;
        }

        public boolean hasNext(){
            return currentBytePosition < endPosition;
        }

        public byte nextBit(){
            byte bit = (byte) (((buffer[currentBytePosition] << currentBitPosition++) & 128) == 0 ? 0 : 1);
            if(currentBitPosition >= 8){
                currentBytePosition++;
                currentBitPosition = 0;
            }
            return bit;
        }
    }

}
