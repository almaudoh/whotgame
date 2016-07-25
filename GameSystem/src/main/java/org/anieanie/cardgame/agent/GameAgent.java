package org.anieanie.cardgame.agent;

import org.anieanie.card.Card;
import org.anieanie.cardgame.gameplay.GameEnvironment;

/**
 * Defines an interface for whot agents
 */
public interface GameAgent extends Runnable {

    // Returns the name of this agent.
    String getName();

    // Requests the game agent to refresh the client status so it can take necessary action.
    void refresh();

    // Updates the game environment when it changes.
    default void updateEnvironment(GameEnvironment environment) {
        // Default implementation does nothing.
    }

    default void moveRejected(Card card) {
        // Default implementation does nothing.
    }

    default void moveAccepted(Card card) {
        // Default implementation does nothing.
    }
}
