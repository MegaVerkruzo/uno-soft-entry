package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        List<List<String>> result = transformInputToResult(inputRows);
        long resultGroupsAmount = getGroupsAmountWithMoreThanOneNode(result);
        writeFile(outputFilePath, result, resultGroupsAmount);

        long endTime = System.currentTimeMillis();
        printMessageResults(resultGroupsAmount, start, endTime);
    }

    private static long getGroupsAmountWithMoreThanOneNode(List<List<String>> result) {
        return result
                .stream()
                .filter(list -> list.size() > 1)
                .count();
    }

    private static List<List<String>> transformInputToResult(List<String[]> input) {
        // :TODO
        return null;
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

    private static void writeFile(Path filePath, List<List<String>> result, long resultGroupsAmount) {
        // :TODO
    }

    private static void printMessageResults(long resultGroupsAmount, long startTime, long endTime) {
        System.out.println("Groups with more than one " + resultGroupsAmount);
        System.out.println("Time in millis is " + (endTime - startTime));
    }
}
