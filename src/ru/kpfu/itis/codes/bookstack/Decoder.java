package ru.kpfu.itis.codes.bookstack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class Decoder {
    private InputStream inputStream;
    private OutputStream outputStream;
    private List<Integer> list;
    private Map<Integer, Code> dict;

    public Decoder(InputStream inputStream, OutputStream outputStream) {
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

    public void decode(){
        try{
            int in = 0;
            List<Byte> bits = new ArrayList<>();
            byte[] bytes = new byte[1];
            BitSlicer bitSlicer;
            int lastBit = -1;
            int bit = -1;
            Code code = new Code();
            int index = 0;
            while ((in = inputStream.read(bytes)) != -1){
                bitSlicer = new BitSlicer(bytes);
                while (bitSlicer.hasNext()) {
                    while ((lastBit != 0 || bit != 0) && bitSlicer.hasNext()) {
                        lastBit = bit;
                        bit = bitSlicer.nextBit();
                        System.out.println(bit);
                        code.addCode((byte) bit);
                    }
                    if (lastBit == 0 && bit == 0) {
                        if (code.size() > 2) {
                            for (int i = 0; i < dict.size(); i++) {
                                Code iCode = dict.get(i);
                                if (code.equals(iCode)) {
                                    index = i;
                                    break;
                                }
                            }
                            int codeByte = list.get(index);
                            System.out.println(Integer.toBinaryString(codeByte));
                            outputStream.write(codeByte);
                            list.remove(index);
                            list.add(0, codeByte);
                        }
                        lastBit = -1;
                        bit = -1;
                        code = new Code();
                    }
                }
            }
            outputStream.close();
            inputStream.close();
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

    private static class BitSlicer{
        int currentBitPosition = 0;
        int currentBytePosition = 0;
        int endPosition;
        byte[] buffer;

        public BitSlicer(byte[] buffer) {
            this.buffer = buffer;
            endPosition = buffer.length;
        }

        public BitSlicer(byte[] buffer, int endPosition) {
            this.endPosition = endPosition;
            this.buffer = buffer;
        }

        public boolean hasNext(){
            return currentBytePosition < endPosition;
        }

        public byte nextBit(){
            byte bit = (byte) (((buffer[currentBytePosition] << currentBitPosition++) & 128) == 0 ? 0 : 1);
            if(currentBitPosition >= 8){
                currentBytePosition++;
                currentBitPosition = 0;
            }
            return bit;
        }
    }

}
