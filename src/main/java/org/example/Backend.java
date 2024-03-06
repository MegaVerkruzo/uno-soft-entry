package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class Backend implements Runnable {
    private final Path inputFilePath;
    private final Path outputFilePath;

    private Backend(Path inputFilePath, Path outputFilePath) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
    }

    public static Backend createBackend(Path inputFilePath, Path outputFilePath) {
        return new Backend(inputFilePath, outputFilePath);
    }

    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        final List<String[]> inputRows;
        try {
            inputRows = loadFile(inputFilePath);
        } catch (IOException e) {
            System.err.println("Something incorrect with I/O: " + e);
            System.exit(1);
            return;
        }

        TreeMap<KeySizeParent, Set<String>> result = transformInputToResult(inputRows);
        long resultGroupsAmount = getGroupsAmountWithMoreThanOneNode(result);
        try {
            writeFile(outputFilePath, result, resultGroupsAmount);
        } catch (IOException e) {
            System.err.println("Thrown exception while making print output: + " + e);
            System.exit(1);
            return;
        }

        long endTime = System.currentTimeMillis();
        printMessageResults(resultGroupsAmount, start, endTime);
    }

    private static long getGroupsAmountWithMoreThanOneNode(TreeMap<KeySizeParent, Set<String>> result) {
        return result.keySet().stream().filter(key -> key.size > 1).count();
    }

    private static List<String[]> loadFile(Path filePath) throws IOException {
        List<String> allLines = Files.readAllLines(filePath);
        List<String[]> result = new ArrayList<>(allLines.size());
        for (String line : allLines) {
            String[] row = line.split(";");
            boolean isCorrectRow = true;
            for (String columnWithSpaces : row) {
                String column = columnWithSpaces.trim();
                if (column.length() < 2
                        || (column.charAt(0) != '"' || column.charAt(column.length() - 1) != '"')
                        || column.substring(1, column.length() - 1).contains("\"")
                ) {
                    isCorrectRow = false;
                    break;
                }
            }

            if (isCorrectRow) {
                result.add(row);
            }
        }
        return result;
    }

    private static TreeMap<KeySizeParent, Set<String>> transformInputToResult(List<String[]> input) {
        List<Node> dsu = getNodes(input);
        Map<Integer, List<Node.NodeElement>> anotherRepresentation = getColumnWordsMap(input, dsu);
        unionNodesByDSU(anotherRepresentation);

        TreeMap<KeySizeParent, Set<String>> resultGroup = new TreeMap<>(KeySizeParent.comparator);
        dsu.forEach(node -> {
            KeySizeParent key = new KeySizeParent(node.getSize(), Node.getParent(node).getNumber());
            resultGroup.putIfAbsent(key, new HashSet<>());
            resultGroup.get(key).add(node.getRow());
        });
        return resultGroup;
    }

    private static void unionNodesByDSU(Map<Integer, List<Node.NodeElement>> anotherRepresentation) {
        for (Map.Entry<Integer, List<Node.NodeElement>> entry : anotherRepresentation.entrySet()) {
            Map<String, Node.NodeElement> map = new HashMap<>();
            for (final Node.NodeElement element : entry.getValue()) {
                if (!map.containsKey(element.getData())) {
                    map.put(element.getData(), element);
                    continue;
                }

                Node.unionNodes(
                        map.get(element.getData()).getParentNode(),
                        element.getParentNode()
                );
            }
        }
    }

    private static List<Node> getNodes(List<String[]> input) {
        List<Node> snm = new ArrayList<>(input.size());
        for (int i = 0; i < input.size(); ++i) {
            snm.add(Node.createNode(String.join(";", input.get(i)), i));
        }
        return snm;
    }

    private static Map<Integer, List<Node.NodeElement>> getColumnWordsMap(List<String[]> input, List<Node> snm) {
        Map<Integer, List<Node.NodeElement>> anotherRepresentation = new HashMap<>();
        for (int i = 0; i < input.size(); ++i) {
            String[] currentRow = input.get(i);
            for (int column = 0; column < currentRow.length; ++column) {
                Objects.requireNonNull(anotherRepresentation.putIfAbsent(column, List.of()))
                        .add(Node.NodeElement.createElement(
                                currentRow[column].substring(1, currentRow[column].length() - 1), snm.get(i)
                        ));
            }
        }
        return anotherRepresentation;
    }

    private static void writeFile(Path filePath, TreeMap<KeySizeParent, Set<String>> result, long resultGroupsAmount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW
        )) {
            writer.write(resultGroupsAmount + "\n");
            int groupNumber = 1;
            for (Map.Entry<KeySizeParent, Set<String>> group : result.entrySet()) {
                writer.write("Группа " + (groupNumber++) + "\n");
                for (String row : group.getValue()) {
                    writer.write(row + "\n");
                }
                writer.newLine();
            }
        }
    }

    private static void printMessageResults(long resultGroupsAmount, long startTime, long endTime) {
        System.out.println("Groups with more than one " + resultGroupsAmount);
        System.out.println("Time in millis is " + (endTime - startTime));
    }

    private record KeySizeParent(int size, int parentNumber) {

        // in desc order by size
        // then asc order by parentNumber
        private static final Comparator<KeySizeParent> comparator = (o1, o2) -> (o1.size != o2.size)
                ? o2.size - o1.size
                : o1.parentNumber - o2.parentNumber;
    }
}
