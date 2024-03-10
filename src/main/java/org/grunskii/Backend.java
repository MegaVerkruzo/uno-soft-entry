package org.grunskii;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Backend implements Runnable {
    private static final int MAX_COLUMNS_COUNT = 15;
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
        final Set<String> inputRows;
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

    private static Set<String> loadFile(Path filePath) throws IOException {
        List<String> allLines = Files.readAllLines(filePath);
        Set<String> result = new HashSet<>(allLines.size());
        for (String line : allLines) {
            List<String> row = Arrays.stream(line.split(";")).map(String::trim).toList();
            boolean isCorrectRow = row
                    .stream()
                    .noneMatch(str -> str.length() < 2
                            || (str.charAt(0) != '"' || str.charAt(str.length() - 1) != '"')
                            || str.substring(1, str.length() - 1).contains("\"")
                    );

            if (isCorrectRow) {
                result.add(String.join(";", row));
            }
        }
        return result;
    }

    private static TreeMap<KeySizeParent, Set<String>> transformInputToResult(Set<String> input) {
        List<Node> dsu = getNodes(input);
        List<List<Node.NodeElement>> anotherRepresentation = getColumnWordsMap(input, dsu);
        unionNodesByDSU(anotherRepresentation);

        TreeMap<KeySizeParent, Set<String>> resultGroup = new TreeMap<>(KeySizeParent.comparator);
        dsu.forEach(node -> {
            KeySizeParent key = new KeySizeParent(node.getSize(), node.getParent().getNumber());
            resultGroup.putIfAbsent(key, new HashSet<>());
            resultGroup.get(key).add(node.getRow());
        });
        return resultGroup;
    }

    private static void unionNodesByDSU(List<List<Node.NodeElement>> anotherRepresentation) {
        for (List<Node.NodeElement> entry : anotherRepresentation) {
            Map<String, Node.NodeElement> map = new HashMap<>();
            for (final Node.NodeElement element : entry) {
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

    private static List<Node> getNodes(Set<String> input) {
        List<Node> snm = new ArrayList<>(input.size());
        int index = 0;
        for (final String row : input) {
            snm.add(Node.createNode(row, index++));
        }
        return snm;
    }

    private static List<List<Node.NodeElement>> getColumnWordsMap(Set<String> input, List<Node> snm) {
        List<List<Node.NodeElement>> anotherRepresentation = Collections.nCopies(MAX_COLUMNS_COUNT, new ArrayList<>());
        List<String[]> rows = input.stream().map(str -> str.split(";")).toList();
        for (int i = 0; i < rows.size(); ++i) {
            String[] row = rows.get(i);
            for (int column = 0; column < row.length; ++column) {
                if (row[column].substring(1, row[column].length() - 1).isBlank()) {
                    continue;
                }
                anotherRepresentation
                        .get(column)
                        .add(Node.NodeElement.createElement(row[column], snm.get(i)));
            }
        }
        return anotherRepresentation;
    }

    private static void writeFile(
            Path filePath,
            TreeMap<KeySizeParent, Set<String>> result,
            long resultGroupsAmount
    ) throws IOException {
        Files.deleteIfExists(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
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
        System.out.println("Groups with more than one row: " + resultGroupsAmount);
        System.out.println("Time in millis: " + (endTime - startTime));
    }

    private record KeySizeParent(int size, int parentNumber) {

        // in desc order by size
        // then asc order by parentNumber
        private static final Comparator<KeySizeParent> comparator = (o1, o2) -> (o1.size != o2.size)
                ? o2.size - o1.size
                : o1.parentNumber - o2.parentNumber;
    }
}
