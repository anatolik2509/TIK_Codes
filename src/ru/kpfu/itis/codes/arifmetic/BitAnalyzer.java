package ru.kpfu.itis.codes.arifmetic;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class BitAnalyzer {
    private Map<Byte, Integer> blockStatistic;
    private InputStream inputStream;

    public BitAnalyzer(InputStream inputStream) {
        this.blockStatistic = new HashMap<>();
        this.inputStream = inputStream;
    }

    public Map<Byte, Integer> analyze(int blockLength){
        try {
            int b = 0;
            while ((b = inputStream.read()) != -1) {
                int currentBit = 0;
                byte entry = 0;
                while (currentBit < 8) {
                    for (int i = 0; i < blockLength; i++) {
                        entry += ((b & 1) << i);
                        currentBit++;
                        b = b >> 1;
                    }
                    Integer entries = blockStatistic.get(entry);
                    if (entries == null) {
                        blockStatistic.put(entry, 1);
                    } else {
                        blockStatistic.put(entry, entries + 1);
                    }
                    entry = 0;
                }
            }

            inputStream.close();

            return blockStatistic;
        }
        catch (IOException e){
            throw new IllegalArgumentException();
        }
    }



}
