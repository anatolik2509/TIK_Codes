package ru.kpfu.itis.codes.bookstack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Code {
    private List<Byte> codes;

    public Code(){
        codes = new ArrayList<>();
    }

    public void addCode(byte b){
        codes.add(b);
    }

    public void addCodeFirst(byte b){
        codes.add(0, b);
    }

    public byte getCode(int i){
        return codes.get(i);
    }

    public int size(){
        return codes.size();
    }

    public List<Byte> getCodes() {
        return codes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Code code = (Code) o;
        return Objects.equals(codes, code.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codes);
    }
}
