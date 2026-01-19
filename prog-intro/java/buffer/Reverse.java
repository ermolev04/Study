import java.util.Arrays;
import java.util.Scanner;

public class Reverse {
    public static final int DEF_SIZE = 100;
    public static void main(String[] args) {
        java.util.Scanner scanLine = new Scanner(System.in);    
        java.util.Scanner scanInt;
        int[][] mainArray = new int[DEF_SIZE][];
        int mainSize = 0, localSize;
        while (scanLine.hasNextLine()) {
            if (mainArray.length <= mainSize) {
                mainArray = Arrays.copyOf(mainArray, mainArray.length * 3 / 2);
            }
            mainArray[mainSize] = new int[DEF_SIZE];
            scanInt =  new Scanner(scanLine.nextLine());
            localSize = 0;
            while (scanInt.hasNextInt()) {
                if (mainArray[mainSize].length <= localSize) {
                    mainArray[mainSize] = Arrays.copyOf(mainArray[mainSize], mainArray[mainSize].length * 3 / 2);
                }
                mainArray[mainSize][localSize] = scanInt.nextInt();
                localSize++;
            }
            scanInt.close();		
            mainArray[mainSize] = Arrays.copyOf(mainArray[mainSize], localSize);
            mainSize++;
        }
        scanLine.close();
        mainArray = Arrays.copyOf(mainArray, mainSize);

        for(int i = mainArray.length - 1; i >= 0 ; i--) {
            for(int j = mainArray[i].length - 1; j >= 0; j--) {
                System.out.print(mainArray[i][j] + " ");
            }
            System.out.println();
        }
    }   
}