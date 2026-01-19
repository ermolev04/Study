package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputTest {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();
        while (true) {
            try {
                int c = in.read();
                if (c != 4)  // ASCII 4 04 EOT (end of transmission) ctrl D, I may be wrong here
                    System.out.println("Hui");
                else
                    break;
            } catch (IOException e) {
                System.err.println ("Error reading input");
            }
        }
        System.out.println(out.toString());
    }
}