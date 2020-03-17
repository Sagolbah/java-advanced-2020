package ru.ifmo.rain.boger.implementor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Class extending {@link SimpleFileVisitor} for deleting directories and files recursively
 */
public class CleaningVisitor extends SimpleFileVisitor<Path> {
    /**
     * Deletes file with given {@link Path}
     *
     * @param path file path
     * @param attributes basic attributes of file
     * @return {@link FileVisitResult#CONTINUE}
     * @throws IOException if I/O error happened while deleting the file
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
        Files.delete(path);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Deletes current directory after visiting all its subdirectories/files
     * @param dir current visited directory
     * @param e <code>null</code> if deletion done without errors or {@link IOException} that happened
     * @return {@link FileVisitResult#CONTINUE}
     * @throws IOException if I/O error happened while deleting the directory
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }
}
