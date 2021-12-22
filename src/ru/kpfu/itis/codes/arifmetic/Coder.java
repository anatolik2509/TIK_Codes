package ru.kpfu.itis.codes.arifmetic;

import sun.text.normalizer.UTF16;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

public class Coder {
    private CodeRanges codeRanges;
    private OutputStream outputStream;
    private InputStream inputStream;
    private int blockSize;

    public Coder(Map<Byte, Integer> statistics, OutputStream output, InputStream inputStream, int blockSize){
        codeRanges = new CodeRanges(statistics, blockSize);
        outputStream = output;
        this.inputStream = inputStream;
        this.blockSize = blockSize;
    }

    public void encode(){
        int b = 0;
        Range currentRange = new Range(0, 1);
        BitReceiver receiver = new BitReceiver();
        int size = codeRanges.getEntriesSum();
        byte[] codeRangeBytes = codeRanges.serialize();
        try {
            outputStream.write(size >> 24);
            outputStream.write(size >> 16);
            outputStream.write(size >> 8);
            outputStream.write(size);
            outputStream.write(codeRangeBytes.length >> 24);
            outputStream.write(codeRangeBytes.length >> 16);
            outputStream.write(codeRangeBytes.length >> 8);
            outputStream.write(codeRangeBytes.length);
            outputStream.write(codeRangeBytes);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.out.println("encoding:");
        while (true){
            try {
                if ((b = inputStream.read()) == -1) break;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            System.out.println("input: " + b);
            int currentBit = 0;
            byte value = 0;
            while (currentBit < 8){
                for(int i = 0; i < blockSize; i++){
                    value += ((b >> 7 - currentBit) & 1) << blockSize - i - 1;
                    currentBit++;
                }
                codeRanges.fitRange(value, currentRange);
                //System.out.println("value: " + value);
                System.out.println("fitted: " + currentRange);
                int bit;
                while ((bit = currentRange.popCommonBit()) != -1){
                    receiver.add((byte) bit, 1);
                    //System.out.print(bit);
                }
                value = 0;
            }
            //System.out.println();
            System.out.println("extended: " + currentRange);
        }
        double accuracy = 2;
        double targetAccuracy = (currentRange.getEnd() - currentRange.getStart()) / 2;
        double dot = (currentRange.getEnd() + currentRange.getStart()) / 2;
        while (accuracy > targetAccuracy){
            //System.out.println(dot);
            dot *= 2;
            byte bit = (byte) Math.floor(dot);
            dot -= bit;
            receiver.add(bit, 1);
            //System.out.println(bit);
            accuracy /= 2;
        }
        System.out.println();
        receiver.flush();
        try {
            outputStream.close();
            inputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
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
