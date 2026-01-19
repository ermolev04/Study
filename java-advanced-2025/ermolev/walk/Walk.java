package info.kgeorgiy.ja.ermolev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.HexFormat;

public class Walk {
    private static final String ERROR = "0".repeat(16);
    private static final int BUFFER_SIZE = 1024;
    private static final byte[] BUFFER = new byte[BUFFER_SIZE];

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("ERROR: You need write two argument \"inputfile\" \"outputfile\" ");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.err.println("ERROR: You write wrong argument!");
            return;
        }

        try {
            Path input = Paths.get(args[0]);
            try {
                Path output = Paths.get(args[1]);
                try {
                    if (output.getParent() != null) {
                        Files.createDirectories(output.getParent());
                    }
                } catch (FileAlreadyExistsException ignored) {
                } catch (IOException | SecurityException e) {
                    System.err.println("ERROR: We can't create output file: " + e.getMessage());
                    return;
                }

                try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
                     BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {

                    String fileName;
                    while ((fileName = reader.readLine()) != null) {
                        try {
                            writer.write(fileHash(fileName) + " " + fileName);
                        } catch (IOException | SecurityException e) {
                            writer.write(ERROR + " " + fileName);
                        } finally {
                            writer.newLine();
                        }
                    }
                } catch (IOException | SecurityException e) {
                    System.err.println("ERROR: We can't work with I/O files: " + e.getMessage());
                }
            } catch (InvalidPathException e) {
                System.err.println("ERROR: invalid path for output file: " + e.getMessage());
            }
        } catch (InvalidPathException e) {
            System.err.println("ERROR: invalid path for input file: " + e.getMessage());
        }
    }

    private static String fileHash(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            int count;
            while ((count = inputStream.read(BUFFER)) != -1) {
                digest.update(BUFFER, 0, count);
            }
            byte[] hash = digest.digest();
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("ERROR: We can't found SHA-256 algorithm");
            return ERROR;
        } catch (IOException | InvalidPathException | SecurityException e) {
            return ERROR;
        }
    }
}
