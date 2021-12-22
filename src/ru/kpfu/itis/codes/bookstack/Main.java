package ru.kpfu.itis.codes.bookstack;

import java.io.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("input.txt");
        OutputStream outputStream = new FileOutputStream("test.book");
        Coder coder = new Coder(inputStream, outputStream);
        coder.encode();
        inputStream = new FileInputStream("test.book");
        outputStream = new FileOutputStream("test_out.txt");
        Decoder decoder = new Decoder(inputStream, outputStream);
        decoder.decode();

    }
}
