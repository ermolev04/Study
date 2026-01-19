package reverse;

import java.io.*;
import java.util.Arrays;
import static java.lang.Integer.parseInt;

public class ReverseMinRAbc {
    public static final int DEF_SIZE = 100;
    private static int abcToInt(String a) {
        int ans = 0;
        for (int i = 0; i < a.length(); i++) {
            if ((int) a.charAt(i) <= (int) 'j' && (int) a.charAt(i) >= (int) 'a') {
                ans *= 10;
                ans += (int) a.charAt(i) - (int) 'a';
            }            
        }
        if(a.charAt(0) == '-') {
            ans *= -1;
        }
        return ans;
    }

    private static String intToAbc(int a) {
        boolean sign = false;
        if(a == 0) {
            return "a";
        }
        StringBuilder ans = new StringBuilder();
        if(a < 0) {
            sign = true;
            a *= -1;
        }
        while(a > 0) {
            ans.insert(0, (char) (a % 10 + (int) 'a'));
            a /= 10;
        }
        if(sign) {
            ans.insert(0, '-');
        }
        return ans.toString();
    }

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

                for(int i = 0; i < mainArray.length ; i++) {
                    int min = Integer.MAX_VALUE;
                    for(int j = 0; j < mainArray[i].length; j++) {
                        if(min > abcToInt(mainArray[i][j])) {
                            min = abcToInt(mainArray[i][j]);
                        }                       
                        System.out.print(intToAbc(min) + " ");
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