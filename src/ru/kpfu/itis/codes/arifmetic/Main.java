package ru.kpfu.itis.codes.arifmetic;

import ru.kpfu.itis.codes.utils.Analyzer;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

public class Main {

    public static final int BLOCK_SIZE = 8;

    public static void main(String[] args) throws IOException {
        InputStream inputStream = new FileInputStream("test.txt");
        BitAnalyzer analyzer = new BitAnalyzer(inputStream);
        Map<Byte, Integer> map = analyzer.analyze(BLOCK_SIZE);
//        System.out.println(map);
//        CodeRanges codeRanges = new CodeRanges(map, BLOCK_SIZE);
//        System.out.println(codeRanges);
//        System.out.println(Arrays.toString(codeRanges.serialize()));
//        System.out.println(CodeRanges.deserialize(codeRanges.serialize()));
//        System.out.println(Arrays.toString(CodeRanges.deserialize(codeRanges.serialize()).serialize()));
        inputStream = new FileInputStream("test.txt");
        OutputStream outputStream = new FileOutputStream("test.arif");
        Coder coder = new Coder(map, outputStream, inputStream, BLOCK_SIZE);
        coder.encode();
        inputStream = new FileInputStream("test.arif");
        outputStream = new FileOutputStream("test_out.txt");
        Decoder decoder = new Decoder(outputStream, inputStream);
        decoder.decode();
    }
}
