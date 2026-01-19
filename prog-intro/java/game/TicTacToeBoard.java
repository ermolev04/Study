package game;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import static java.lang.Math.max;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class TicTacToeBoard implements Board, Position {
    private static final Map<Cell, Character> SYMBOLS = Map.of(
            Cell.X, 'X',
            Cell.O, 'O',
            Cell.E, '.'
    );

    private final Cell[][] cells;
    private final int n, m, k;
    private int empty = 0;
    private Cell turn;

    public TicTacToeBoard(int n, int m, int k) {
        this.n = n;
        this.m = m;
        this.k = k;
        empty = n * m;
        this.cells = new Cell[n][m];
        for (Cell[] row : cells) {
            Arrays.fill(row, Cell.E);
        }
        turn = Cell.X;
    }

    public TicTacToeBoard() throws IOException {
        Scanner in = new Scanner(System.in);
        int[] values = new int[3];
        System.out.println("Enter the field size and the row length required to win.");
        while(true) {
            boolean flag = true;
            for(int i = 0; i < 3; i++) {
                if(!in.hasNextInt()) {
                    if (in.hasNext()) {
                        in.next();
                    } else {
                        throw new IOException("You broke this game :( Please don't press ctrl+D");
                    }
                    flag = false;
                    System.out.println("You write unreadable values. Check and write again");
                    break;
                }
                values[i] = in.nextInt();
            }
            if(flag && (values[2] <= 0 || values[1] <= 0 || values[0] <= 0)) {
                System.out.println("You must write natural values.");
                flag = false;
            }
            if(flag && (values[0] * values[1] > 100000)) {
                System.out.println("Your board is very big!");
                flag = false;
            }
            if(flag && values[2] > max(values[0], values[1])) {
                System.out.println("You never can win, because your \"row-to-win\" more when field's size.");
                flag = false;
            }
            if(flag) {
                n = values[0];
                m = values[1];
                k = values[2];
                break;
            }
        }
        empty = n * m;
        this.cells = new Cell[values[0]][values[1]];
        for (Cell[] row : cells) {
            Arrays.fill(row, Cell.E);
        }
        turn = Cell.X;
    }

    public TicTacToeBoard(Cell[][] cells, int n, int m, int k) {
        this.cells = new Cell[n][];
        for(int i = 0; i < n; i++) {
            this.cells[i] = Arrays.copyOf(cells[i], cells[i].length);
        }
        this.n = n;
        this.m = m;
        this.k = k;
    }

    @Override
    public Position getPosition() {
        return new TicTacToeBoard(cells, n, m, k);
    }

    @Override
    public Cell getCell() {
        return turn;
    }

    @Override
    public int getRow() {
        return n;
    }

    @Override
    public int getCol() {
        return m;
    }

    @Override
    public int getRTW() {
        return k;
    }

    private void printFinal() {
        System.out.println("Final position:");
        System.out.println(this);
    }
    @Override
    public Result makeMove(final Move move) {
        if (!isValid(move)) {
            printFinal();
            return Result.LOSE;
        }
        empty--;

        int x = move.getRow(), y = move.getColumn();
        Cell type = move.getValue();
        cells[x][y] = type;

        if(check(x, y,1, 0, type) || check(x, y,0, 1, type) || check(x, y,1, 1, type) || check(x, y,1, -1, type)){
            printFinal();
            return Result.WIN;
        }

        if (empty == 0) {
            printFinal();
            return Result.DRAW;
        }

        turn = turn == Cell.X ? Cell.O : Cell.X;
        return Result.UNKNOWN;
    }

    private boolean check(int indX, int indY, int shtX, int shtY, Cell type) {
        int ans = 0, i = 0;
        while(ans < k && indX + i * shtX < n && indX + i * shtX >= 0
                && indY + i * shtY < m && indY + i * shtY >= 0 && cells[indX + i * shtX][indY + i * shtY] == type){
            i++;
            ans++;
        }
        if(ans >= k) {
            return true;
        }
        i = 1;
        while(ans < k && indX + i * -shtX < n && indX + i * -shtX >= 0
                && indY + i * -shtY < m && indY + i * -shtY >= 0 && cells[indX + i * -shtX][indY + i * -shtY] == type){
            i++;
            ans++;
        }
        return ans >= k;
    }


    @Override
    public boolean isValid(final Move move) {
        return 0 <= move.getRow() && move.getRow() < n
                && 0 <= move.getColumn() && move.getColumn() < m
                && cells[move.getRow()][move.getColumn()] == Cell.E
                && turn == getCell();
    }

    @Override
    public Cell getCell(final int r, final int c) {
        return cells[r][c];
    }

    @Override
    public String toString() {
        if(m > 99 || n > 99) {
            return "Fild so big, i can't write it in terminal :(";
        }
        final StringBuilder sb = new StringBuilder(" ");
        if(n > 9) {
            sb.append(" ");
        }
        sb.append('|');
        for(int i = 1; i <= m; i++) {
            sb.append(" ");
            sb.append(i);
            if (m > 9 && i < 10) {
                sb.append(" ");
            }
        }
        sb.append(System.lineSeparator());
        sb.append("-");
        if(n > 9) {
            sb.append("-");
        }
        sb.append("+");
        for(int i = 1; i <= m; i++) {
            sb.append("--");
            if (m > 9) {
                sb.append("-");
            }
        }
        for (int r = 1; r <= n; r++) {
            sb.append(System.lineSeparator());
            if(r < 10 && n > 9) {
                sb.append(" ");
            }
            sb.append(r);
            sb.append("|");
            for (int c = 0; c < m; c++) {
                sb.append(" ");
                sb.append(SYMBOLS.get(cells[r - 1][c]));
                if(m > 9) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
}
