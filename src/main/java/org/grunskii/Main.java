package org.grunskii;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args == null || args.length != 1 || args[0] == null) {
            System.err.println("""
                    Incorrect input!
                    Usage: java -jar {name_project}.jar {file_name}.txt
                    """);
            return;
        }

        final Path inputFilePath = Paths.get(args[0]);
        final Path outputFilePath = Paths.get("output.txt");
        if (Files.notExists(inputFilePath)) {
            System.err.println("""
                    Input file not exists!
                    """);
            return;
        }

        final Backend backend = Backend.createBackend(inputFilePath, outputFilePath);
        backend.run();
    }
}