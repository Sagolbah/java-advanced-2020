package ru.ifmo.rain.boger.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Class for generating default implementations
 *
 * @author Daniil Boger (github.com/Sagolbah)
 */
public class Implementor extends BaseImplementor implements Impler {

    /**
     * Runs {@link Implementor} with given arguments:
     * <code>className rootDir</code> - executes {@link #implement(Class, Path)} with given arguments
     * If error is occurred during implementation or arguments are invalid, prints to error stream corresponding message
     *
     * @param args arguments for running {@link Implementor}
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: java Implementor <classPath> <outputPath>");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("Arguments must be non-null");
                return;
            }
        }
        try {
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ClassNotFoundException e) {
            System.err.println("Specified class not found");
        } catch (InvalidPathException e) {
            System.err.println("Invalid path");
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
