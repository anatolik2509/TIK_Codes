package ru.kpfu.itis.codes.arifmetic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public class Decoder {
    private OutputStream outputStream;
    private InputStream inputStream;
    private int bufferSize;

    public Decoder(OutputStream outputStream, InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        bufferSize = 1;
    }

    public void decode(){
        try {
            int size = 0;
            int codeRangesSize = 0;
            BitReceiver bitReceiver = new BitReceiver();
            size += inputStream.read() << 24;
            size += inputStream.read() << 16;
            size += inputStream.read() << 8;
            size += inputStream.read();
            codeRangesSize += inputStream.read() << 24;
            codeRangesSize += inputStream.read() << 16;
            codeRangesSize += inputStream.read() << 8;
            codeRangesSize += inputStream.read();
            byte[] codeRangesBytes = new byte[codeRangesSize];
            inputStream.read(codeRangesBytes);
            CodeRanges codeRanges = CodeRanges.deserialize(codeRangesBytes);
            byte[] buffer = new byte[1];
            int r = 0;
            Range dotRange = new Range(0, 1);
            Range outerRange = new Range(0, 1);
            System.out.println("decoding: ");
            while ((r = inputStream.read(buffer)) != -1) {
                BitSlicer slicer = new BitSlicer(buffer);
                while (slicer.hasNext()){
                    int b = slicer.nextBit();
                    //System.out.println("outer: " + outerRange);
                    //System.out.println("dot before bit: " + dotRange);
                    dotRange.pushBit(b);
                    System.out.println(b);
                    //System.out.println("dot after bit: " + dotRange);
                    Range newRange = codeRanges.containsRange(dotRange, outerRange);
                    if(newRange != null){
                        byte s = newRange.getValue();
                        outerRange = newRange;
                        //System.out.println(s);
                        System.out.println("cool outer: " + outerRange);
                        //System.out.println("cool dot: " + dotRange);
                        bitReceiver.add(s, codeRanges.getBlockSize());
                        size--;
                        System.out.println("size: " + size);
                        if(size == 0){
                            break;
                        }
                        int commonBit;
                        do {
                            commonBit = outerRange.popCommonBit();
                            if(commonBit != -1) dotRange.popCommonBit();
                        } while (commonBit != -1);
                        //System.out.println("extended outer: " + outerRange);
                        //System.out.println("extended dot: " + dotRange);
                    }
                }
                if(size == 0){
                    break;
                }
            }
            System.out.println();
            bitReceiver.flush();
            inputStream.close();
            outputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
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

    private class BitReceiver{
        int currentBitPosition = 0;
        int currentByte = 0;

        public void add(byte b, int size){
            currentByte = currentByte << size;
            currentByte |= (b & 0xFF);
            currentBitPosition += size;
            if(currentBitPosition >= 8){
                byte newByte = (byte) (currentByte >> (currentBitPosition - 8));
                try {
                    //System.out.println(newByte);
                    outputStream.write(newByte);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                currentBitPosition -= 8;
            }
        }

        public void flush(){
            if(currentBitPosition > 0){
                int newByte = currentByte << (8 - currentBitPosition);
                try {
                    outputStream.write(newByte);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
