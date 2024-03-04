package org.example;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
        long start = System.currentTimeMillis();
        Map<Integer, List<String>> input = loadFile(inputFilePath);

        List<List<String>> result = getResult(input);
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

    private static List<List<String>> getResult(Map<Integer, List<String>> input) {
        // :TODO
        return null;
    }

    private static Map<Integer, List<String>> loadFile(Path filePath) {
        // :TODO
        return null;
    }

    private static void writeFile(Path filePath, List<List<String>> result, long resultGroupsAmount) {
        // :TODO
    }

    private static void printMessageResults(long resultGroupsAmount, long startTime, long endTime) {
        System.out.println("Groups with more than one " + resultGroupsAmount);
        System.out.println("Time in millis is " + (endTime - startTime));
    }
}
