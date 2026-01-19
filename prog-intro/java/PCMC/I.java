import java.lang.Math;
import java.util.Scanner;

public class I {
    public static void main(String[] args) {
        int n;
        long nCoord, sCoord, wCoord, eCoord;
        Scanner scan = new Scanner(System.in);
        n = scan.nextInt();
        long x, y, h;
        x = scan.nextLong();
        y = scan.nextLong();
        h = scan.nextLong();
        nCoord = y - h;
        sCoord = y + h;
        wCoord = x - h;
        eCoord = x + h;
        for(int i = 1; i < n; i++) {           
            x = scan.nextLong();
            y = scan.nextLong();
            h = scan.nextLong();
            if(nCoord > y - h) {
                nCoord = y - h;
            }
            if(sCoord < y + h) {
                sCoord = y + h;
            }
            if(wCoord > x - h) {
                wCoord = x - h;
            }
            if(eCoord < x + h) {
                eCoord = x + h;
            }
        }
        scan.close();
        System.out.print((eCoord + wCoord) / 2 + " " + (sCoord + nCoord) / 2 + " " + (java.lang.Math.max((eCoord - wCoord), (sCoord - nCoord)) + 1) / 2);
    }
}