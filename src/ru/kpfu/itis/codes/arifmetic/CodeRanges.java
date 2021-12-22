package ru.kpfu.itis.codes.arifmetic;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CodeRanges {


    private List<Double> lengths;
    private List<Integer> counts;
    private List<Byte> values;
    private int entriesSum;
    private int blockSize;

    public CodeRanges(List<Double> lengths, List<Byte> values, int blockSize){
        this.lengths = lengths;
        this.values = values;
        this.blockSize = blockSize;
    }

    public CodeRanges(Map<Byte, Integer> statistics, int blockSize){
        lengths = new ArrayList<>();
        values = new ArrayList<>();
        counts = new ArrayList<>();
        int entrySum = statistics.values().stream().reduce((sum, a) -> sum += a).orElse(0);
        for(Map.Entry<Byte, Integer> e : statistics.entrySet()){
            counts.add(e.getValue());
            lengths.add((double) e.getValue() / (double) entrySum);
            values.add(e.getKey());
        }
        entriesSum = entrySum;
        this.blockSize = blockSize;
    }

    public byte[] serialize(){
        int size = counts.size() * (Integer.BYTES + 1) + 1;
        byte[] bytes = new byte[size];
        int cur = 0;
        bytes[cur++] = (byte) blockSize;
        for(int i = 0; i < counts.size(); i++){
            int currentCount = counts.get(i);
            byte currentValue = values.get(i);
            bytes[cur++] = (byte) (currentCount >> 24);
            bytes[cur++] = (byte) (currentCount >> 16);
            bytes[cur++] = (byte) (currentCount >> 8);
            bytes[cur++] = (byte) currentCount;
            bytes[cur++] = currentValue;
        }
        return bytes;
    }

    public static CodeRanges deserialize(byte[] bytes){
        int cur = 0;
        List<Integer> counts = new ArrayList<>();
        List<Byte> values = new ArrayList<>();
        int blockSize = bytes[cur++] & 0xFF;
        while (cur < bytes.length){
            int currentCount = 0;
            currentCount += (bytes[cur++] & 0xFF) << 24;
            currentCount += (bytes[cur++] & 0xFF) << 16;
            currentCount += (bytes[cur++] & 0xFF) << 8;
            currentCount += bytes[cur++] & 0xFF;
            counts.add(currentCount);
            byte currentValue = 0;
            for (int i = 0; i < blockSize; i++){
                currentValue += ((bytes[cur]) & (1 << i));
            }
            cur++;
            values.add(currentValue);
        }
        int entriesSum = 0;
        for(int c : counts){
            entriesSum += c;
        }
        final int fixedSum = entriesSum;
        List<Double> lengths = counts.stream().map(i -> (double) i / fixedSum).collect(Collectors.toList());
        CodeRanges ranges = new CodeRanges(lengths, values, blockSize);
        ranges.entriesSum = entriesSum;
        ranges.counts = counts;
        return ranges;
    }

    public void fitRange(byte b, Range currentRange){
        double start = 0;
        boolean found = false;
        double length = 0;
        for(int i = 0; i < lengths.size(); i++){
            if(values.get(i) == b){
                found = true;
                length = lengths.get(i);
                break;
            }
            start += lengths.get(i);
        }
        if(!found) {
            throw new NoSuchElementException("No element: " + b);
        }
        double oldLength = currentRange.getEnd() - currentRange.getStart();
        double newStart = currentRange.getStart() + start * oldLength;
        double newEnd = newStart + length * oldLength;
        currentRange.setStart(newStart);
        currentRange.setEnd(newEnd);
    }

    public Range containsRange(Range range, Range outerRange){
        int i = 0;
        double scale = outerRange.getEnd() - outerRange.getStart();
        double start = range.getStart() - outerRange.getStart();
        double end = range.getEnd() - outerRange.getStart();
        double curLength = lengths.get(i) * scale;
        double lenAccum = outerRange.getStart();
        while (start >= curLength && end > curLength){
            start -= curLength;
            end -= curLength;
            i++;
            lenAccum += curLength;
            curLength = lengths.get(i) * scale;
        }
        if (!(start <= curLength && end <= curLength)){
            return null;
        }
        return new Range(lenAccum, lenAccum + curLength, values.get(i));
    }

    public byte valueForDot(double dot){
        int i = 0;
        while (dot > lengths.get(i)){
            dot -= lengths.get(i);
            i++;
        }
        return values.get(i);
    }

    @Override
    public String toString() {
        return "CodeRanges{" +
                "lengths=" + lengths +
                ", values=" + values +
                '}';
    }

    public int getEntriesSum() {
        return entriesSum;
    }

    public int getBlockSize() {
        return blockSize;
    }
}
