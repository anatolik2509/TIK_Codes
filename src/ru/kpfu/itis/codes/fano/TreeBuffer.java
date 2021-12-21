package ru.kpfu.itis.codes.fano;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TreeBuffer{
    private final byte[] bytes;

    private int sizeInBytes;

    private int codeSize;
    private int valueSize;

    private int bytesOnEntry;

    private int entriesStartPtr;
    private int entriesEndPtr;
    private int entriesCapacity;
    private int entriesCount;

    private static final int HEADER_SIZE = 2;

    public TreeBuffer(int codeSize, int valueSize, int entriesCapacity) {
        this.entriesCount = 0;
        this.codeSize = codeSize;
        this.valueSize = valueSize;
        this.bytesOnEntry = codeSize + valueSize + 1;
        this.entriesCapacity = entriesCapacity;
        sizeInBytes = HEADER_SIZE + entriesCapacity * bytesOnEntry;
        entriesStartPtr = HEADER_SIZE;
        entriesEndPtr = HEADER_SIZE;
        bytes = new byte[sizeInBytes];
        writeHeader();
    }

    public TreeBuffer(byte[] bytes){
        this.bytes = bytes;
        sizeInBytes = bytes.length;
        readHeader();
    }

    public void addEntry(String value, byte[] code, int length){
        bytes[entriesEndPtr++] = (byte) length;
        for(int i = 0; i < codeSize; i++){
            if(i >= code.length){
                bytes[entriesEndPtr] = 0;
                entriesEndPtr ++;
                continue;
            }
            bytes[entriesEndPtr] = code[i];
            entriesEndPtr++;
        }
        for(int i = 0; i < value.length(); i++){
            bytes[entriesEndPtr++] = (byte) (value.charAt(i) >> 8);
            bytes[entriesEndPtr++] = (byte) value.charAt(i);
        }
        entriesCount++;
    }

    public CodeEntry getNextEntry(){
        int length = bytes[entriesEndPtr++] & 0XFF;
        int byteLength = (int) Math.ceil(((double) length) / 8);
        byte[] codeBytes = new byte[byteLength];
        for(int i = 0; i < codeSize; i++){
            if(i >= byteLength){
                entriesEndPtr++;
                continue;
            }
            codeBytes[i] = bytes[entriesEndPtr++];
        }
        byte[] valueBytes = new byte[valueSize];
        for(int i = 0; i < valueSize; i++){
            valueBytes[i] = bytes[entriesEndPtr++];
        }
        return new CodeEntry(new String(valueBytes, StandardCharsets.UTF_16), codeBytes, length);
    }

    public boolean hasNextEntry(){
        return entriesEndPtr < bytes.length;
    }

    private void writeHeader(){
        bytes[0] = (byte) codeSize;
        bytes[1] = (byte) valueSize;
    }

    private void readHeader(){
        codeSize = bytes[0] & 0xFF;
        valueSize = bytes[1] & 0xFF;
        bytesOnEntry = codeSize + valueSize;
        entriesEndPtr = HEADER_SIZE;
        entriesStartPtr = HEADER_SIZE;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getEntriesCapacity(){
        return entriesCapacity;
    }

    public int getEntrySize() {
        return bytesOnEntry;
    }

    public int getCodeSize() {
        return codeSize;
    }

    public int getValueSize() {
        return valueSize;
    }
}
