package ru.kpfu.itis.codes.fano;


import org.w3c.dom.Node;

import java.util.*;

public class CodeTree {

    private final Node rootNode;

    private Node currentNode;

    private int entriesCount;

    private int blockSize;

    public CodeTree() {
        rootNode = new Node();
        currentNode = rootNode;
    }

    public String move(byte bit){
        if(bit == 0){
            currentNode = currentNode.getNode1();
        }
        else {
            currentNode = currentNode.getNode2();
        }
        if(currentNode.getValue() == null){
            return null;
        }
        else {
            String result = currentNode.getValue();
            currentNode = rootNode;
            return result;
        }
    }

    public int getDepth(){
        return getDepth(rootNode, 0);
    }

    private int getDepth(Node node, int currentDepth){
        if(node.getValue() != null){
            return currentDepth + 1;
        }
        return Math.max(getDepth(node.getNode1(), currentDepth + 1), getDepth(node.getNode2(), currentDepth + 1));
    }

    public Map<String, Code> getDictionary(){
        Map<String, Code> dict = new HashMap<>();
        getCodes(dict, rootNode, 0, 0);
        return dict;
    }

    private void getCodes(Map<String, Code> dict, Node node, int prefix, int depth){
        if(node.getValue() != null){
            byte[] bytes = new byte[(int) Math.ceil(depth / 8.0)];
            for(int i = 0; i < bytes.length; i++){
                bytes[i] = (byte) prefix;
                prefix = prefix >> 8;
            }
            dict.put(node.getValue(), new Code(depth, bytes));
            return;
        }
        int leftPrefix = prefix << 1;
        int rightPrefix = (prefix << 1) + 1;
        getCodes(dict, node.getNode1(), leftPrefix, depth + 1);
        getCodes(dict, node.getNode2(), rightPrefix, depth + 1);
    }


    public byte[] serialize(){
        int depth = getDepth();
        Map<String, Code> dict = getDictionary();
        int codeBytes = (int) Math.ceil(((double) depth) / 8);
        TreeBuffer buffer = new TreeBuffer(codeBytes, blockSize * Character.BYTES, dict.size());
        for(Map.Entry<String, Code> e : getDictionary().entrySet()){
            buffer.addEntry(e.getKey(), e.getValue().value, e.getValue().size);
        }
        return buffer.getBytes();
    }

    public static CodeTree deserialize(byte[] serialized){
        TreeBuffer buffer = new TreeBuffer(serialized);
        CodeTree tree = new CodeTree();
        while (buffer.hasNextEntry()){
            CodeEntry entry = buffer.getNextEntry();
            byte[] bytes = entry.getCode();
            int length = entry.getLength() - 1;
            Node currentNode = tree.rootNode;
            while (length > -1){
                int bit = (bytes[length / 8] & 0xFF) >> length % 8;
                bytes[length / 8] &= (1 << length % 8) - 1;
                length--;
                if(bit == 0){
                    if(currentNode.getNode1() == null){
                        currentNode.setNode1(new Node());
                    }
                    currentNode = currentNode.getNode1();
                }
                if(bit == 1){
                    if(currentNode.getNode2() == null){
                        currentNode.setNode2(new Node());
                    }
                    currentNode = currentNode.getNode2();
                }
            }
            currentNode.setValue(entry.getValue());
        }
        tree.blockSize = buffer.getValueSize() / Character.BYTES;
        return tree;
    }





    public Node getRootNode() {
        return rootNode;
    }

    public int getEntriesCount() {
        return entriesCount;
    }

    public void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    static class Node{
        private String value;
        private Node node1;
        private Node node2;

        public Node(String value) {
            this.value = value;
        }

        public Node() {
        }

        public Node getNode1() {
            return node1;
        }

        public void setNode1(Node node1) {
            this.node1 = node1;
        }

        public Node getNode2() {
            return node2;
        }

        public void setNode2(Node node2) {
            this.node2 = node2;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    static class Code{
        private final int size;
        private final byte[] value;

        public Code(int size, byte[] value) {
            this.size = size;
            this.value = value;
        }

        public int getSize() {
            return size;
        }

        public byte[] getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Code{" +
                    "size=" + size +
                    ", value=" + Arrays.toString(value) +
                    '}';
        }
    }

}
