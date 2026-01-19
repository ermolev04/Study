package reverse;

import reverse.MyScannerRev;

import java.io.*;
import java.util.Arrays;
import static java.lang.Integer.parseInt;

public class Reverse {
    public static final int DEF_SIZE = 100;
    public static void main(String[] args) {
        try {
            MyScannerRev scanLine = new MyScannerRev();
            String[][] mainArray = new String[DEF_SIZE][];
            int mainSize = 0, localSize;
            try {
                while (scanLine.hasNextLine()) {
                    if (mainArray.length <= mainSize) {
                        mainArray = Arrays.copyOf(mainArray, mainArray.length * 3 / 2);
                    }
                    mainArray[mainSize] = scanLine.intOfLine();
                    mainSize++;    
                }

                mainArray = Arrays.copyOf(mainArray, mainSize);

                for(int i = mainArray.length - 1; i >= 0 ; i--) {
                    for(int j = mainArray[i].length - 1; j >= 0; j--) {
                        System.out.print(mainArray[i][j] + " ");
                    }
                    System.out.println();
                }
            } finally {
               scanLine.close(); 
            }            
        } catch (IOException e) {
            System.out.println("Error in reading: " + e);
        }
        
        
    }   
}