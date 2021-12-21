package ru.kpfu.itis.codes.fano;

import java.util.*;
import java.util.stream.Collectors;

public class CodeTreeBuilder {
    public static CodeTree buildTree(Map<String, Integer> statistic, int blockSize){
        int entrySum = statistic.values().stream().reduce((sum, a) -> sum += a).orElse(0);
        List<Map.Entry<String, Double>> sortedEntries =
                statistic.entrySet()
                        .stream()
                        .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed())
                        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), (double) e.getValue() / (double) entrySum))
                        .collect(Collectors.toList());
        System.out.println("Statistic:");
        for(Map.Entry<String, Double> e : sortedEntries){
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        CodeTree codeTree = new CodeTree();
        codeTree.setEntriesCount(sortedEntries.size());
        codeTree.setBlockSize(blockSize);
        Queue<NodeRange> rangesQueue = new ArrayDeque<>();
        rangesQueue.add(new NodeRange(0, sortedEntries.size(), 1.0, codeTree.getRootNode()));
        while (!rangesQueue.isEmpty()){
            NodeRange nodeRange = rangesQueue.poll();
            if(nodeRange.end - nodeRange.begin <= 1){
                nodeRange.currentNode.setValue(sortedEntries.get(nodeRange.begin).getKey());
            }
            else {
                calculateRanges(sortedEntries, rangesQueue, nodeRange);
            }
        }
        return codeTree;
    }

    private static void calculateRanges(List<Map.Entry<String, Double>> sortedEntries, Queue<NodeRange> rangesQueue, NodeRange nodeRange) {
        double passedSize = 0;
        double targetSize = nodeRange.size / 2;

        for(int i = nodeRange.begin; i < nodeRange.end; i++){
            if(sortedEntries.get(i).getValue() > targetSize){
                CodeTree.Node node1 = new CodeTree.Node();
                CodeTree.Node node2 = new CodeTree.Node();
                nodeRange.currentNode.setNode1(node1);
                nodeRange.currentNode.setNode2(node2);
                if(sortedEntries.get(i).getValue() / 2 > targetSize){
                    System.out.printf("processed [%d, %d) with size %f\n", nodeRange.begin, nodeRange.end, nodeRange.size);
                    System.out.printf("added [%d, %d) with size %f and [%d, %d) with size %f\n",
                            nodeRange.begin, i, passedSize, i, nodeRange.end, nodeRange.getSize() - passedSize);
                    rangesQueue.add(new NodeRange(nodeRange.begin, i, passedSize, node1));
                    rangesQueue.add(new NodeRange(i, nodeRange.end, nodeRange.getSize() - passedSize, node2));
                }
                else {
                    passedSize += sortedEntries.get(i).getValue();
                    System.out.printf("processed [%d, %d) with size %f\n", nodeRange.begin, nodeRange.end, nodeRange.size);
                    System.out.printf("added [%d, %d) with size %f and [%d, %d) with size %f\n",
                            nodeRange.begin, i + 1, passedSize, i + 1, nodeRange.end, nodeRange.getSize() - passedSize);
                    rangesQueue.add(new NodeRange(nodeRange.begin, i + 1, passedSize, node1));
                    rangesQueue.add(new NodeRange(i + 1, nodeRange.end, nodeRange.getSize() - passedSize, node2));
                }
                break;
            }
            else {
                passedSize += sortedEntries.get(i).getValue();
                targetSize -= sortedEntries.get(i).getValue();
            }
        }
    }

    private static class NodeRange{
        private final int begin;
        private final int end;
        private final double size;
        private final CodeTree.Node currentNode;

        public NodeRange(int begin, int end, double size, CodeTree.Node currentNode) {
            this.begin = begin;
            this.end = end;
            this.size = size;
            this.currentNode = currentNode;
        }

        public int getBegin() {
            return begin;
        }

        public int getEnd() {
            return end;
        }

        public double getSize() {
            return size;
        }

        public CodeTree.Node getCurrentNode() {
            return currentNode;
        }
    }
}
