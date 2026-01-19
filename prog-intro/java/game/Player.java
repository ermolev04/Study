package game;

import java.io.IOException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface Player {
    Move move(Position position, Cell cell) throws IOException;
}
