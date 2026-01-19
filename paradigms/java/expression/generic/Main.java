package expression.generic;

public class Main {
    public static void main(String[] args) {
        GenericTabulator gen = new GenericTabulator();
        Object[][][] ans = gen.tabulate(args[0], args[1], -2, 2, -2, 2, -2, 2);
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 5; j++) {
                for(int k = 0; k < 5; k++) {
                    System.out.println("x: " + (i - 2) + ", y: " + (j - 2) + ", z: " + (k - 2) + " = " + ans[i][j][k]);
                }
            }
        }
    }

}
