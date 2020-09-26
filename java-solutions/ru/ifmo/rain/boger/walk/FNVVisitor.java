package ru.ifmo.rain.boger.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FNVVisitor extends SimpleFileVisitor<Path> {

    private final int FNV_32_PRIME = 0x01000193;
    private final int FNV0_HVAL = 0x811c9dc5;
    private final int BLOCK_SIZE = 1024;
    private final BufferedWriter writer;

    public FNVVisitor(final BufferedWriter writer) {
        this.writer = writer;
    }

    private void printResult(final int hash, final Path path) throws IOException {
        writer.write(String.format("%08x %s" + System.lineSeparator(), hash, path.toString()));
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
        int currentHash = FNV0_HVAL;
        int bytesFound;
        try (InputStream is = Files.newInputStream(path)) {
            byte[] block = new byte[BLOCK_SIZE];
            while ((bytesFound = is.read(block)) >= 0) {
                for (int i = 0; i < bytesFound; i++) {
                    currentHash *= FNV_32_PRIME;
                    currentHash ^= (block[i] & 0xFF);
                }
            }
        } catch (IOException e) {
            currentHash = 0;
        }
        printResult(currentHash, path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        printResult(0, path);
        return FileVisitResult.CONTINUE;
    }

}
