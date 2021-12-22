package ru.kpfu.itis.codes.fano;


import ru.kpfu.itis.codes.utils.Analyzer;

import java.io.*;
import java.util.Map;

public class Main {

    public static final int BLOCK_SIZE = 2;

    public static void main(String[] args) throws IOException {
        Reader reader = new FileReader("input.txt");
        Analyzer analyzer = new Analyzer(reader);
        Map<String, Integer> map = analyzer.analyze(BLOCK_SIZE);
        CodeTree codeTree = CodeTreeBuilder.buildTree(map, BLOCK_SIZE);
        reader = new FileReader("input.txt");
        Coder coder = new Coder(codeTree, reader, "output.fano", BLOCK_SIZE);
        coder.encode();
        Decoder decoder = new Decoder(new FileInputStream("output.fano"), new FileWriter("new_input.txt"));
        decoder.decode();
    }


}
