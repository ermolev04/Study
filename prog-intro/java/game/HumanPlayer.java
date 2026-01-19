package game;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class HumanPlayer implements Player {
    private final PrintStream out;
    private final Scanner in;

    public HumanPlayer(final PrintStream out, final Scanner in) {
        this.out = out;
        this.in = in;
    }

    public HumanPlayer() {
        this(System.out, new Scanner(System.in));
    }

    @Override
    public Move move(final Position position, final Cell cell) throws IOException {
        out.println("Position");
        out.println(position.toString());
        out.println(cell + "'s move");
        out.println("Enter row and column");
        while (true) {
            boolean flag = true;
            int[] values = new int[2];
            for(int i = 0; i < 2; i++) {
                if(!in.hasNextInt()) {
                    if (in.hasNext()) {
                        in.next();
                    } else {
                        throw new IOException("You broke this game :( Please don't press ctrl+D");
                    }
                    System.out.println("You write unreadable values. Check and write again.");
                    flag = false;
                    break;
                }
                values[i] = in.nextInt();
            }

            if (flag && position.getRow() >= values[0] &&
                values[0] > 0 && values[1] > 0 &&
                    position.getCol() >= values[1]) {
                return new Move(values[0] - 1, values[1] - 1, cell);
            } else {
                System.out.println("You must write natural values in board size.");
            }
        }
    }
}
