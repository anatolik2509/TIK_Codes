package ru.kpfu.itis.codes.arifmetic;

public class Range {
    private double start;
    private double end;
    private byte value;

    public Range(double start, double end) {
        this.start = start;
        this.end = end;
    }

    public Range(double start, double end, byte value) {
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public int popCommonBit(){
        int startBit = (int) Math.floor(start * 2);
        int endBit = (int) Math.floor(end * 2);
        if(startBit != endBit){
            return -1;
        }
        if(startBit > 1){
            return -1;
        }
        start = start * 2 - startBit;
        end = end * 2 - endBit;
        return startBit;
    }

    public void pushBit(int bit){
        if(bit == 0){
            end = (start + end) / 2;
        }
        if(bit == 1){
            start = (start + end) / 2;
        }
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Range{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    public byte getValue() {
        return value;
    }
}
