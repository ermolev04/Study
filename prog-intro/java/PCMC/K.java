import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

public class K {
	public static void main(String[] args) {
		int n, m, aX = 0, aY = 0, aPosX = 0, aPosY = 0, maxSize = 0; 
        Scanner scan = new Scanner(System.in);
        n = scan.nextInt();
        m = scan.nextInt();
        char[][] map = new char[n][m];
        String str = new String();
        for(int i = 0; i < n; i++) {
            str = scan.next();
            for(int j = 0; j < m; j++) {
                if(str.charAt(j) == 'A') {
                    aX = i;
                    aY = j;
                }
                map[i][j] = str.charAt(j);
            }  
        }

        Pair[][] dm = new Pair[n][m];

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                dm[i][j] = new Pair();
            }
        }

        
        
        if(map[0][0] == '.' || map[0][0] == 'A') {
            dm[0][0].x = 1;
            dm[0][0].y = 1;
        } else {
            dm[0][0].x = 0;
            dm[0][0].y = 0;
        }

        for(int j = 1; j < m; j++) {
            if(map[0][j] == '.' || map[0][j] == 'A') {
                dm[0][j].x = 1;
                dm[0][j].y = dm[0][j - 1].y + 1;
            } else {
                dm[0][j].x = 0;
                dm[0][j].y = 0;
            }
        }

        for(int i = 1; i < n; i++) {
            if(map[i][0] == '.' || map[i][0] == 'A') {
                dm[i][0].x = dm[i - 1][0].x + 1;
                dm[i][0].y = 1;
            } else {
                dm[i][0].x = 0;
                dm[i][0].y = 0;
            }
        }

        for(int i = 1; i < n; i++) {
            for(int j = 1; j < m; j++) {
                if(map[i][j] == '.' || map[i][j] == 'A') {
                    if(map[i - 1][j] != '.' && map[i - 1][j] != 'A' || map[i][j - 1] != '.' && map[i][j - 1] != 'A') {
                        dm[i][j].x = dm[i - 1][j].x + 1;
                        dm[i][j].y = dm[i][j - 1].y + 1;

                    } else {
                        if(map[i - 1][j - 1] != '.' && map[i - 1][j - 1] != 'A') {
                            
                        }
                        dm[i][j].x = java.lang.Math.min((int) (dm[i][j - 1].x  - 1), (int) (dm[i - 1][j].x)) + 1;
                        dm[i][j].y = java.lang.Math.min((int) (dm[i][j - 1].y), (int) (dm[i - 1][j].y - 1)) + 1;
                    }
                    
                } else {
                    dm[i][j].x = 0;
                    dm[i][j].y = 0;
                }
            }
        }
        

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                System.out.println(i + " | " + j + " | " + dm[i][j].x + " | " + dm[i][j].y);
            }
        }

        for(int i = aX; i < n; i++) {
            for(int j = aY; j < m; j++) {
                // System.out.println(i + " | " + j + " | " + maxSize);
                // System.out.println(dm[i][j].x + " | " + dm[i][j].y + " | " + maxSize);
                if(dm[i][j].x - 1 + i >= aX && dm[i][j].y - 1 + j >= aY){
                    if(dm[i][j].x * dm[i][j].y > maxSize) {
                        maxSize = dm[i][j].x * dm[i][j].y;
                        aPosX = i;
                        aPosY = j;
                    }
                }
            }
        }
        
        for(int i = aPosX - dm[aPosX][aPosY].x + 1; i <= aPosX; i++) {
            for(int j = aPosY - dm[aPosX][aPosY].y + 1; j <= aPosY; j++) {
                if(map[i][j] != 'A') {
                    map[i][j] = 'a';
                }
            }
        }

        for(int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }

	}
}

class Pair {
    public int x;
    public int y;
}