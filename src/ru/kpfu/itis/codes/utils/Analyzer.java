package ru.kpfu.itis.codes.utils;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;

public class Analyzer {
    private Map<String, Integer> blockStatistic;
    private Reader reader;

    public Analyzer(Reader reader) {
        this.blockStatistic = new HashMap<>();
        this.reader = reader;
    }

    public Map<String, Integer> analyze(int blockLength){
        char[] buffer = new char[blockLength];
        while (true) {
            try {
                if (reader.read(buffer) == -1) break;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            String s = new String(buffer);
            Integer entries = blockStatistic.get(s);
            if(entries == null){
                blockStatistic.put(s, 1);
            }
            else {
                blockStatistic.put(s, entries + 1);
            }
        }
        try {
            reader.close();
        } catch (IOException ignored) { }
        return blockStatistic;
    }

}
