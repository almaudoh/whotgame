package org.anieanie.cardgame.gameplay;

import org.anieanie.card.Card;

/**
 * This encapsulates the game rules that applies to any game.
 */
public interface GameRule {

    /**
     * Determines if the specified move is valid, given the specified game environment.
     */
    boolean isValidMove(Card move, GameEnvironment env);

}
