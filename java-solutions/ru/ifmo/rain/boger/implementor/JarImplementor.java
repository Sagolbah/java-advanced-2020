package ru.ifmo.rain.boger.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarImplementor extends BaseImplementor implements JarImpler {

    /**
     * Runs {@link Implementor} with given arguments:
     * <code>className rootDir</code> - executes {@link #implement(Class, Path)} with given arguments
     * If error is occurred during implementation or arguments are invalid, prints to error stream corresponding message
     *
     * @param args arguments for running {@link Implementor}
     */
    public static void main(String[] args) {
        if (args == null || args.length != 3) {
            System.out.println("Usage: java JarImplementor -jar <classPath> <outputPath>");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("Arguments must be non-null");
                return;
            }
        }
        try {
            if (args.length == 3) {
                new JarImplementor().implement(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Specified class not found");
        } catch (InvalidPathException e) {
            System.err.println("Invalid path");
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }

}
