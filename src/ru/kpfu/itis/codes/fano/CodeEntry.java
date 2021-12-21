package ru.kpfu.itis.codes.fano;

public class CodeEntry {
    private final String value;
    private final byte[] code;
    private final int length;

    public CodeEntry(String value, byte[] code, int length) {
        this.value = value;
        this.code = code;
        this.length = length;
    }

    public String getValue() {
        return value;
    }

    public byte[] getCode() {
        return code;
    }

    public int getLength() {
        return length;
    }
}
