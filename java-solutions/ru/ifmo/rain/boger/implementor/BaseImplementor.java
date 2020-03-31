package ru.ifmo.rain.boger.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;


/**
 * Core implementor class with all implementing methods and tools.
 *
 * @author Daniil Boger (github.com/Sagolbah)
 */
public class BaseImplementor {
    /**
     * Tabulator symbol for generated implementation
     */
    private static final String TAB = "    ";
    /**
     * Line separator for generated implementation
     */
    private static final String EOL = System.lineSeparator();
    /**
     * Space symbol for generated implementation
     */
    private static final String SPACE = " ";
    /**
     * Instance of {@link CleaningVisitor} for deleting temporary directories in {@link #implementJar}
     */
    private static final CleaningVisitor CLEANER = new CleaningVisitor();

    /**
     * Creates {@link BaseImplementor} instance
     */
    public BaseImplementor() {
    }

    /**
     * Returns {@link String} consisting of <code>count</code> tabulator symbols
     *
     * @param count amount of needed tabs
     * @return {@link String} consisting of specified amount of tabs.
     */
    private String getMultipleTabs(final int count) {
        return TAB.repeat(Math.max(0, count));
    }

    /**
     * Converts {@link String} to unicode-escaped representation
     *
     * @param str {@link String} to convert
     * @return {@link String} with unicode escaping
     */
    private String getUnicodeEscapedString(final String str) {
        StringBuilder builder = new StringBuilder();
        for (final char c : str.toCharArray()) {
            builder.append(c >= 128 ? String.format("\\u%04X", (int) c) : c);
        }
        return builder.toString();
    }

    /**
     * Checks if given class can be implemented
     *
     * @param token class token
     * @throws ImplerException if token cannot be implemented
     */
    private void checkToken(final Class<?> token) throws ImplerException {
        if (token.isPrimitive() || token.equals(Enum.class) || token.isArray() || Modifier.isFinal(token.getModifiers())
                || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Class token is incorrect and cannot be implemented");
        }
    }

    /**
     * Returns {@link String} describing default <code>return</code> statement of given {@link Method}
     *
     * @param method method to get default return statement
     * @return {@link String} consisting of <code>return</code>,
     * default value for return type (empty if <code>void</code>) and semicolon
     */
    private String getReturnValue(final Method method) {
        Class<?> returnType = method.getReturnType();
        String result;
        if (returnType.equals(void.class)) {
            result = ";";
        } else if (returnType.isPrimitive()) {
            result = returnType == boolean.class ? " false;" : " 0;";
        } else {
            result = " null;";
        }
        return "return" + result;
    }

    /**
     * Returns code fragment for returning default value or calling superclass constructor, depending of given {@link Executable}.
     * If given {@link Executable} is instance of {@link Method} returns code for returning default value of its return type.
     * Otherwise, returns code for calling superclass constructor
     *
     * @param executable given {@link Executable}
     * @return {@link String} representing body of generated {@link Executable} implementation
     */
    private String getBody(final Executable executable) {
        return executable instanceof Method ? getReturnValue((Method) executable) :
                ("super" + getExecutableParameters(executable, false) + ";");
    }

    /**
     * Returns beginning part of the class that implements/extends given interface/class,
     * consists of package name, implementation class name, base class (or interface) name, opening brace and end of line
     * The implementation class name is {@link #getClassName} value of given class/interface + <code>Impl</code>
     *
     * @param token base class or interface to extend/implement
     * @return {@link String} representing beginning part (described above) of implementation class
     */
    private String getHeader(final Class<?> token) {
        return "package " +
                token.getPackageName() +
                ';' +
                EOL +
                EOL +
                "public class " +
                getClassName(token) +
                SPACE +
                (token.isInterface() ? "implements " : "extends ") +
                token.getCanonicalName() +
                SPACE +
                "{" +
                EOL;
    }

    /**
     * Returns {@link String} consisting of given class simple name with <code>Impl</code> suffix
     *
     * @param token given class
     * @return {@link String} consisting of given class simple name + <code>Impl</code>
     */
    private String getClassName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Creates parent directories for file with specified {@link Path}
     *
     * @param path path of file to create parent directories
     * @throws ImplerException if directories cannot be created due to error
     */
    private void createDirectories(final Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can't create directories for output location", e);
            }
        }
    }

    /**
     * If given {@link Executable} is instance of {@link Method} returns return type and name of it separated with space,
     * otherwise returns name of generated class
     *
     * @param executable given {@link Executable}
     * @return {@link String} consisting of return type and name or only name of generated class
     */
    private String getReturnTypeAndName(final Executable executable) {
        if (executable instanceof Method) {
            Method tmp = (Method) executable;
            return tmp.getReturnType().getCanonicalName() + SPACE + tmp.getName();
        } else {
            return getClassName(((Constructor<?>) executable).getDeclaringClass());
        }
    }

    /**
     * Returns name of given {@link Parameter}, optionally with its type
     *
     * @param parameter   parameter to get name
     * @param specifyType flag for adding type of given parameter (true for enabled adding)
     * @return {@link String} consisting of parameter name. If type is added, typename and space symbol are the prefix
     */
    private String wrapParameter(final Parameter parameter, final boolean specifyType) {
        return (specifyType ? parameter.getType().getCanonicalName() + SPACE : "") + parameter.getName();
    }

    /**
     * Returns {@link String} consisting of given {@link Executable} parameters (optionally with its types),
     * separated with commas and surrounded by round braces
     *
     * @param executable  given {@link Executable}
     * @param specifyType flag for adding types of given {@link Executable} parameters (true for enabled adding)
     * @return {@link String} representing list of parameters, surrounded by round braces and separated with commas
     */
    private String getExecutableParameters(final Executable executable, final boolean specifyType) {
        return Arrays.stream(executable.getParameters()).map(
                parameter -> wrapParameter(parameter, specifyType)).collect(
                Collectors.joining("," + SPACE, "(", ")"));
    }

    /**
     * Returns {@link String}, representing exceptions that given {@link Executable} may throw
     * If given {@link Executable} may not throw any exceptions, returns empty {@link String}.
     * Otherwise, returns {@link String} <code>throws</code> + names of exceptions
     * that given {@link Executable} may throw, separated with commas
     *
     * @param executable {@link Executable} to get exceptions from
     * @return {@link String} describing exceptions that given {@link Executable} may throw
     */
    private String getThrows(final Executable executable) {
        Class<?>[] exceptions = executable.getExceptionTypes();
        if (exceptions.length == 0) {
            return "";
        }
        return SPACE + "throws" + SPACE + Arrays.stream(exceptions).map(Class::getCanonicalName)
                .collect(Collectors.joining("," + SPACE));
    }

    /**
     * Returns {@link String} that consists of implementations of all abstract methods
     * (including inherited) of given {@link Class} that can be implemented
     *
     * @param tokenArg given {@link Class} to implement abstract methods
     * @return {@link String} with implementations of abstract methods of given {@link Class}
     */
    private String implementAbstract(final Class<?> tokenArg) {
        Class<?> token = tokenArg;
        final Predicate<Method> predicate = (method -> Modifier.isAbstract(method.getModifiers()));
        Set<MethodStorage> methods = Arrays.stream(token.getMethods()).filter(predicate).map(MethodStorage::new).collect(Collectors.toSet());
        Set<MethodStorage> finals = Arrays.stream(token.getDeclaredMethods()).filter(
                method -> Modifier.isFinal(method.getModifiers())).map(MethodStorage::new).collect(Collectors.toSet());
        while (token != null) {
            List<MethodStorage> currentTokenMethods = Arrays.stream(token.getDeclaredMethods()).
                    filter(predicate).map(MethodStorage::new).collect(Collectors.toList());
            methods.addAll(currentTokenMethods);
            token = token.getSuperclass();
        }
        methods.removeAll(finals);
        StringBuilder result = new StringBuilder();
        for (MethodStorage method : methods) {
            result.append(getExecutableBody(method.getData()));
        }
        return result.toString();
    }

    /**
     * Returns {@link String} that consists of implementations of all constructors of given {@link Class}
     *
     * @param token given {@link Class} to implement constructors
     * @return {@link String} with implementations of constructors of given {@link Class}
     * @throws ImplerException if given {@link Class} has no non-private constructors.
     */
    private String implementConstructors(Class<?> token) throws ImplerException {
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors()).filter(
                ctor -> !Modifier.isPrivate(ctor.getModifiers())).collect(Collectors.toList());
        if (constructors.isEmpty()) {
            throw new ImplerException("No non-private constructors found in " + token.getName());
        }
        StringBuilder result = new StringBuilder();
        for (Constructor<?> ctor : constructors) {
            result.append(getExecutableBody(ctor));
        }
        return result.toString();
    }

    /**
     * Returns full implementation of given {@link Executable}
     * If given {@link Executable} is instance of {@link Constructor}, the generated implementation will call
     * its superclass constructor.
     * Otherwise, the generated implementation will return default value of return type of such {@link Method}. If this
     * {@link Method} return type is <code>void</code>, implemented method body will be <code>return;</code>
     *
     * @param executable the specified {@link Executable}
     * @return {@link String} consisting of complete implementation of given {@link Executable}
     */
    private String getExecutableBody(final Executable executable) {
        final int modifiers = executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
        return getMultipleTabs(1) +
                Modifier.toString(modifiers) +
                SPACE +
                getReturnTypeAndName(executable) +
                getExecutableParameters(executable, true) +
                getThrows(executable) +
                SPACE +
                "{" +
                EOL +
                getMultipleTabs(2) +
                getBody(executable) +
                EOL +
                getMultipleTabs(1) +
                "}" +
                EOL;
    }

    /**
     * Returns path to file with generated implementation with specified extension. This file is located in specified
     * parent directory
     *
     * @param token  class/interface that is extended/implemented
     * @param root   parent directory of generated implementation
     * @param ending file extension
     * @return {@link Path} representing path to file with generated implementation
     */
    private Path getPath(final Class<?> token, final Path root, final String ending) {
        return root.resolve(token.getPackageName().replace(".", File.separator)).resolve(
                getClassName(token) + ending);
    }

    /**
     * Returns {@link String} representing the path where given {@link Class} is loaded from
     *
     * @param token given {@link Class}
     * @return {@link String} consists of path where given {@link Class} is loaded from
     * @throws ImplerException if {@link CodeSource} of given {@link Class} can't be converted to {@link java.net.URI}
     */
    private String getLoadLocation(final Class<?> token) throws ImplerException {
        try {
            CodeSource src = token.getProtectionDomain().getCodeSource();
            if (src == null) {
                return null;
            }
            return Paths.get(src.getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("Can't convert URL to URI", e);
        }
    }

    /**
     * Generates implementation for given {@link Class} and saves it into specified {@link Path}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException if the given class cannot be implemented because of one or multiple reasons:
     *                         <ul>
     *                             <li>Given {@link Class} is array, primitive type or {@link Enum}</li>
     *                             <li>Given {@link Class} is private or final</li>
     *                             <li>Given {@link Class} is not an interface and does not have any non-private constructors</li>
     *                             <li>Some arguments are <code>null</code></li>
     *                             <li>Some errors occurred during I/O</li>
     *                         </ul>
     */
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkToken(token);
        Path outputLocation = getPath(token, root, ".java");
        createDirectories(outputLocation);
        List<String> codeParts = new ArrayList<>();
        try (BufferedWriter writer = Files.newBufferedWriter(outputLocation)) {
            codeParts.add(getHeader(token));
            if (!token.isInterface()) {
                codeParts.add(implementConstructors(token));
            }
            codeParts.add(implementAbstract(token));
            codeParts.add("}" + EOL);
            for (String part : codeParts) {
                writer.write(getUnicodeEscapedString(part));
            }
        } catch (IOException e) {
            throw new ImplerException("Can't create writer for output", e);
        }
    }

    /**
     * Creates <code>.jar</code> file with generated implementation of given {@link Class}
     * During this process there is a temporary directory created to store <code>.java</code> and <code>.class</code>
     * files. If the program fails to delete this directory, the {@link ImplerException} will be thrown to warn user.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <code>.jar</code> file.
     * @throws ImplerException if some of these conditions are true:
     *                         <ul>
     *                             <li>Errors occurred while executing {@link #implement(Class, Path)}</li>
     *                             <li>{@link JavaCompiler} could not compile generated <code>.java</code> file</li>
     *                             <li>Some errors occurred during I/O</li>
     *                             <li>Some arguments are <code>null</code></li>
     *                             <li>Program has failed to delete temporary directory</li>
     *                         </ul>
     */
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Class token and path must be non-null");
        }
        Path temporaryDir;
        createDirectories(jarFile);
        try {
            temporaryDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tmp");
        } catch (IOException e) {
            throw new ImplerException("Can't create temporary directory", e);
        }
        try {
            implement(token, temporaryDir);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            List<String> args = new ArrayList<>();
            args.add(getPath(token, temporaryDir, ".java").toString());
            String targetClassPath = getLoadLocation(token);
            if (targetClassPath != null) {
                args.add("-cp");
                args.add(targetClassPath);
            }
            if (compiler == null || compiler.run(null, null, null, args.toArray(String[]::new)) != 0) {
                throw new ImplerException("Can't compile generated implementation");
            }
            Manifest manifest = new Manifest();
            Attributes manifestAttributes = manifest.getMainAttributes();
            manifestAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            String localPath = token.getPackageName().replace('.', '/') + "/" + getClassName(token) + ".class";
            try (JarOutputStream target = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                ZipEntry entry = new ZipEntry(localPath);
                target.putNextEntry(entry);
                Files.copy(getPath(token, temporaryDir, ".class"), target);
            } catch (IOException e) {
                throw new ImplerException("Can't write to .jar file", e);
            }
        } finally {
            try {
                Files.walkFileTree(temporaryDir, CLEANER);
            } catch (IOException e) {
                System.err.println("Can't delete temporary directory");
            }
        }
    }


    /**
     * Class used for representing {@link Method} in {@link Set} collections.
     */
    private static class MethodStorage {
        /**
         * Base for hashcode calculation
         */
        private static final long BASE = 37;
        /**
         * Modulo for hashcode calculation
         */
        private static final long MOD = (long) (1e9 + 7);
        /**
         * Encapsulated instance of {@link Method}
         */
        private final Method data;

        /**
         * Constructs a storage for given {@link Method}
         *
         * @param other {@link Method} to be stored
         */
        MethodStorage(final Method other) {
            data = other;
        }

        /**
         * Compares given {@link Object} with stored method for equality. Methods are equal if and only if when they have
         * equal name, parameters types and return types.
         *
         * @param object to be compared with stored {@link Method}
         * @return <code>true</code> if given {@link Object} is equal to stored {@link Method}, <code>false</code> otherwise
         */
        public boolean equals(final Object object) {
            if (object == null) {
                return false;
            }
            if (!(object instanceof MethodStorage)) {
                return false;
            }
            final Method otherData = ((MethodStorage) object).getData();
            return data.getReturnType().equals(otherData.getReturnType()) &&
                    data.getName().equals(otherData.getName()) &&
                    Arrays.equals(data.getParameterTypes(), otherData.getParameterTypes());
        }

        /**
         * Calculates hashcode for stored {@link Method} using parameters types, return type and {@link Method} name
         *
         * @return hashcode for stored {@link Method}
         */
        public int hashCode() {
            return (int) (Arrays.hashCode(data.getParameterTypes()) + ((data.getName().hashCode()) * BASE) % MOD +
                    (data.getReturnType().hashCode() * BASE * BASE) % MOD);
        }

        /**
         * Returns stored {@link Method}
         *
         * @return stored {@link Method}
         */
        public Method getData() {
            return data;
        }

    }
}
