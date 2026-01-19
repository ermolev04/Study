package info.kgeorgiy.ja.ermolev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.tools.JarImpler;
import org.junit.jupiter.api.Assertions;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface implementation {@link info.kgeorgiy.java.advanced.implementor.tools.JarImpler}.
 * This class can realize interface and deflate it to jar.
 */
public class Implementor implements JarImpler {

    /**
     * Default constructor for {@code Implementor}.
     * This constructor initialize {@code Implementor}.
     */
    public Implementor() {
    }

    /**
     * Just main class
     *
     * @param args command line data
     */
    public static void main(String args[]) {
        try {
            if(args.length == 2) {
                new Implementor().implement(Class.forName(args[0].replace('/', '.')), Path.of(args[1]));
            }
            if (args.length == 3 && args[0].equals("-jar")) {
                new Implementor().implementJar(Class.forName(args[1]), Path.of(args[2]));
            }

        } catch (ImplerException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Realize interface and save him in root
     *
     * @param token interface
     * @param root directory for file
     * @throws ImplerException if we can't realize token
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if(token.isInterface() && (token.getModifiers() & Modifier.PRIVATE) == 0) {
            List<Method> methodList = new ArrayList<>(List.of(token.getMethods()));
            List<Package> packageList = new ArrayList<>(List.of(token.getPackage()));
            List<Class> interfaceList = new ArrayList<>(List.of(token.getInterfaces()));
            String fileName =  token.getSimpleName() + "Impl" + ".java";
            try {
                Path packagePath = root.resolve(token.getPackageName().replace('.', '/'));
                Files.createDirectories(packagePath);
                Path filePath = packagePath.resolve(fileName);
                writePackage(packageList, filePath);
                writeInterface(filePath, token.getSimpleName(), token.getCanonicalName());
                writeMethod(methodList, filePath);
            } catch (IOException e) {
                throw new ImplerException("We can't work with this Path: " + e);
            }
        } else {
            throw new ImplerException("We can't create class without interface!!!");
        }
    }

    /**
     * Write mothods in file
     *
     * @param methodList method list from token
     * @param filePath file path
     * @throws ImplerException If we can't write in file.
     */
    private void writeMethod(List<Method> methodList, Path filePath) throws ImplerException {
        try {
            StringBuilder outBuffer = new StringBuilder();
            for (Method method : methodList) {
                outBuffer.append("public " + method.getReturnType().getCanonicalName() + " " + method.getName() + "(");
            int check = 0;
            for (Class cls : method.getParameterTypes()) {
                    if(check == 0) {
                        outBuffer.append(cls.getCanonicalName() + " var");
                        check++;
                    } else {
                        outBuffer.append(", " + cls.getCanonicalName() + " var" + check++);
                    }
                }
                outBuffer.append(") {" + System.lineSeparator());
                outBuffer.append("return " + getDefaultValue(method.getReturnType()) + System.lineSeparator());
                outBuffer.append("}" + System.lineSeparator());
            }
            outBuffer.append("}" + System.lineSeparator());
            Files.writeString(filePath, outBuffer.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ImplerException("We can't write in file", e);
        }
    }

    /**
     * Return default value for any type.
     *
     * @param returnType return type
     * @return default value for return type.
     */
    private String getDefaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) return "null;";
        if (returnType.equals(void.class)) return ";";
        if (returnType.equals(boolean.class)) return "false;";
        if (returnType.equals(char.class)) return "'\\0';";
        return "0;";
    }

    /**
     * Write class interfaces and main block
     *
     * @param className class single name
     * @param fullName class canonical name
     * @param filePath file path
     * @throws ImplerException If we can't write in file.
     */
    private void writeInterface(Path filePath, String className, String fullName) throws ImplerException {
        try {
            StringBuilder outBuffer = new StringBuilder("public class " + className + "Impl implements " + fullName + " {" + System.lineSeparator());
            Files.writeString(filePath, outBuffer.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ImplerException("We can't write in file", e);
        }
    }
    /**
     * Write class packagee
     *
     * @param packageList package list
     * @param filePath file path
     * @throws ImplerException If we can't write in file.
     */
    private void writePackage(List<Package> packageList, Path filePath) throws ImplerException {
        try {
            StringBuilder outBuffer = new StringBuilder();
            for(Package pack : packageList) {
                outBuffer.append("package " + pack.getName() + ";" + System.lineSeparator());
            }
            if (!outBuffer.isEmpty()) {
                Files.writeString(filePath, outBuffer.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new ImplerException("We can't write in file", e);
        }
    }

    /**
     * Compile List of files
     *
     * @param files list file for compiling
     * @param dependencies List of dependencies
     * @param charset charset file's
     * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
     */
    public static void compile(
            final List<Path> files,
            final List<Class<?>> dependencies,
            final Charset charset
    ) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assertions.assertNotNull(compiler, "Could not find java compiler, include tools.jar to classpath");
        final String classpath = getClassPath(dependencies).stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
        final String[] args = Stream.concat(
                Stream.of("-cp", classpath, "-encoding", charset.name()),
                files.stream().map(Path::toString)
        ).toArray(String[]::new);
        final int exitCode = compiler.run(null, null, null, args);
        Assertions.assertEquals(0, exitCode, "Compiler exit code");
    }

    /**
     * Get class path from dependencies
     *
     * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
     * @param dependencies List of dependencies
     * @return Return class path.
     */
    private static List<Path> getClassPath(final List<Class<?>> dependencies) {
        return dependencies.stream()
                .map(dependency -> {
                    try {
                        return Path.of(dependency.getProtectionDomain().getCodeSource().getLocation().toURI());
                    } catch (final URISyntaxException e) {
                        throw new AssertionError(e);
                    }
                })
                .toList();
    }

    /**
     * Realize interface and deflate it to JAR
     *
     * @param token interface for realization
     * @param jarFile jar directory
     * @throws ImplerException throw if we can't create JAR
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path workDir = jarFile.getParent().resolve(token.getPackageName().replace('.', '/'));
        implement(token, workDir);

        Path rawFile = workDir.resolve(token.getSimpleName() + "Impl.java");

        compile(List.of(rawFile), List.of(token), StandardCharsets.UTF_8);

        Path compiledFile = workDir.resolve(token.getSimpleName() + "Impl.class");

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile), createManifest())) {
            String entryName = token.getPackageName().replace('.', '/') + "/" + token.getSimpleName() + "Impl.class";
            jar.putNextEntry(new JarEntry(entryName));
            Files.copy(compiledFile, jar);
            Files.delete(compiledFile);
        } catch (IOException e) {
            throw new ImplerException("Error creating JAR file", e);
        }
    }

    /**
     * Create Manifest
     *
     * @return object {@link Manifest}
     */
    private Manifest createManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }
}

//note -- падают оба скрипта
//note -- нужно сгенерить документацию


//note -- опять падают скрипты

//erzherzog@Aorus-hrz:/mnt/c/Users/erzhe/Desktop/Workspace/mpp/ja-testing/java-advanced-private/test/__current-repo/scripts$ ./1.sh
//error: file not found: ../../tasks/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/*Impler*.java
//Usage: javac <options> <source files>
//use --help for a list of possible options
//info/kgeorgiy/ja/ermolev/implementor/*.class : no such file or directory
//info/kgeorgiy/java/advanced/implementor/*.class : no such file or directory
//info/kgeorgiy/java/advanced/implementor/tools/*.class : no such file or directory

//erzherzog@Aorus-hrz:/mnt/c/Users/erzhe/Desktop/Workspace/mpp/ja-testing/java-advanced-private/test/__current-repo/scripts$ ./2.sh
//Loading source file ../java-solutions/info/kgeorgiy/ja/ermolev/implementor/Implementor.java...
//error: File not found: "../../tasks/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java"
//1 error
