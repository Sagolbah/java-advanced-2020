package ru.ifmo.rain.boger.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {

    private void visitPaths(final String input, final String output) throws WalkerException {
        Path inputPath = makePath(input);
        Path outputPath = makePath(output);
        outputPath.normalize();
        Path outputParent = outputPath.getParent();
        if (outputParent != null && Files.notExists(outputParent)) {
            try {
                Files.createDirectories(outputParent);
            } catch (IOException e) {
                throw new WalkerException("Can't create parent directories for " + output);
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                final FNVVisitor hashingVisitor = new FNVVisitor(writer);
                String nextPath;
                while ((nextPath = reader.readLine()) != null) {
                    try {
                        Files.walkFileTree(Paths.get(nextPath), hashingVisitor);
                    } catch (InvalidPathException e) {
                        writer.write("00000000 " + nextPath + System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                throw new WalkerException("Can't write to output file " + output);
            }
        } catch (IOException e) {
            throw new WalkerException("Can't read input file " + input);
        }
    }

    private Path makePath(final String location) throws WalkerException {
        try {
            return Paths.get(location);
        } catch (InvalidPathException e) {
            throw new WalkerException("Invalid path: " + location);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java Walker {input file} {output file}");
        } else {
            try {
                new RecursiveWalk().visitPaths(args[0], args[1]);
            } catch (WalkerException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
