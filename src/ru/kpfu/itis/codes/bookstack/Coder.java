package ru.kpfu.itis.codes.bookstack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Coder {
    private InputStream inputStream;
    private OutputStream outputStream;
    private List<Integer> list;
    private Map<Integer, Code> dict;

    public Coder(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.dict = new HashMap<>();
        list = new LinkedList<>();
        int len = 1;
        int maxModifier = 2;
        int modifier = 1;
        for(int i = 0; i < 256; i++){
            list.add(i);
            while (!checkModifier(modifier, len)){
                modifier++;
                if(modifier == maxModifier){
                    modifier = 0;
                    maxModifier *=2;
                    len++;
                }
            }
            Code code = new Code();
            int tempModifier = modifier;
            for(int j = 0; j < len; j++){
                code.addCodeFirst((byte) (tempModifier % 2));
                tempModifier /= 2;
            }
            code.addCode((byte) 0);
            code.addCode((byte) 0);
            dict.put(i, code);
            modifier++;
            if(modifier == maxModifier){
                modifier = 0;
                maxModifier *=2;
                len++;
            }
        }
        System.out.println("ok");
    }

    public void encode(){
        try{
            BitReceiver receiver = new BitReceiver();
            int in = 0;
            int index;
            int value;
            Code code;
            while ((in = inputStream.read()) != -1){
                index = list.indexOf(in);
                value = list.get(index);
                list.remove(index);
                list.add(0, value);
                code = dict.get(index);
                for(int i = 0; i < code.size(); i++){
                    receiver.add(code.getCode(i), 1);
                }
            }
            receiver.flush();
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
            throw new IllegalArgumentException(e);
        }
    }

    private static boolean checkModifier(int m, int l){
        if(m % 2 == 0){
            return false;
        }
        m /= 2;
        int lastBit = -1;
        int bit = 0;
        for(int i = 1; i < l; i++){
            bit = m % 2;
            if(lastBit == 0 && bit ==  0){
                return false;
            }
            lastBit = bit;
            m /= 2;
        }
        return true;
    }

    private class BitReceiver{
        int currentBitPosition = 0;
        int currentByte = 0;

        public void add(byte b, int size){
            currentByte = currentByte << size;
            currentByte |= (b & 0xFF);
            currentBitPosition += size;
            if(currentBitPosition >= 8){
                byte newByte = (byte) (currentByte >> (currentBitPosition - 8));
                try {
                    System.out.println(Integer.toBinaryString((byte)newByte));
                    outputStream.write(newByte);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                currentBitPosition -= 8;
            }
        }

        public void flush(){
            if(currentBitPosition > 0){
                int newByte = currentByte << (8 - currentBitPosition);
                try {
                    System.out.println(Integer.toBinaryString(newByte));
                    outputStream.write(newByte);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
