import java.util.Arrays;
import java.util.Scanner;

public class ReverseMinR {
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

        for(int i = 0; i < mainArray.length; i++) {
            int min = Integer.MAX_VALUE;
            for(int j = 0; j < mainArray[i].length; j++) {
                if(mainArray[i][j] < min) {
                    min = mainArray[i][j];
                }
                System.out.print(min + " ");
            }
            System.out.println();
        }
    }   
}