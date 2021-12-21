package ru.kpfu.itis.codes.fano;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class Coder {
    private Map<String, CodeTree.Code> dictionary;
    private CodeTree codeTree;
    private Reader reader;
    private OutputStream outputStream;
    private int blockSize;
    private int[] twoPowers;

    public Coder(CodeTree codeTree, Reader reader, String outputPath, int blockSize) {
        this.codeTree = codeTree;
        this.dictionary = codeTree.getDictionary();
        this.reader = reader;
        this.blockSize = blockSize;
        try {
            outputStream = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        twoPowers = new int[9];
        twoPowers[0] = 1;
        for (int i = 1; i < twoPowers.length; i++){
            twoPowers[i] = twoPowers[i - 1] * 2;
        }
    }

    public void encode(){
        char[] input = new char[blockSize];
        CodeTree.Code code;
        BitReceiver receiver = new BitReceiver();
        try {
            byte[] codeTreeBytes = codeTree.serialize();
            System.out.println("Serialized tree:");
            System.out.println(Arrays.toString(codeTreeBytes));
            outputStream.write(codeTreeBytes.length >> 24);
            outputStream.write(codeTreeBytes.length >> 16);
            outputStream.write(codeTreeBytes.length >> 8);
            outputStream.write(codeTreeBytes.length);
            outputStream.write(codeTreeBytes);
            while (reader.read(input) != -1){
                String s = new String(input);
                code = dictionary.get(s);
                System.out.println("Write " + s + " as " + Arrays.toString(code.getValue()) + " length: " + code.getSize());
                int bitsToWrite = code.getSize();
                int cursor = code.getValue().length - 1;
                if(bitsToWrite % 8 != 0){
                    receiver.add(code.getValue()[cursor--], bitsToWrite % 8);
                    bitsToWrite -= bitsToWrite % 8;
                }
                while (bitsToWrite > 0){
                    receiver.add(code.getValue()[cursor--], 8);
                    bitsToWrite -= 8;
                }
            }
            receiver.flush();
            reader.close();
            outputStream.close();
        } catch (IOException ioException) {
            throw new IllegalArgumentException(ioException);
        }
    }

    private class BitReceiver{
        int currentBitPosition = 0;
        int currentByte = 0;

        public void add(byte b, int size) throws IOException {
            currentByte = currentByte << size;
            currentByte |= (b & 0xFF);
            currentBitPosition += size;
            System.out.println(size);
            System.out.println(Integer.toBinaryString(currentByte));
            if(currentBitPosition >= 8){
                byte newByte = (byte) (currentByte >> (currentBitPosition - 8));
                System.out.println("Sended " + (byte) newByte);
                outputStream.write(newByte);
                currentBitPosition -= 8;
            }
        }

        public void flush() throws IOException {
            add((byte) 1, 1);
            if(currentBitPosition > 0){
                int newByte = currentByte << (8 - currentBitPosition);
                System.out.println("Sended " + (byte) newByte);
                outputStream.write(newByte);
            }
        }
    }

}
