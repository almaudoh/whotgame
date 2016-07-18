package org.anieanie.cardgame.gameplay.logging;

import org.anieanie.card.CardSet;
import org.anieanie.cardgame.gameplay.GameEnvironment;

/**
 * A logger for game steps.
 */
public interface GameLogger {
    // Logs the move to the game logger.
    void logMove(String move, GameEnvironment environment, CardSet cards);

    // Flush what has already been written to file.
    void flush();
}
