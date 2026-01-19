package game;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface Board {
    Position getPosition();
    Cell getCell();

    int getRow();
    int getCol();
    int getRTW(); // Row To Win
    Result makeMove(Move move);
}
