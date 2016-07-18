package org.anieanie.cardgame.agent;

import org.anieanie.cardgame.gameplay.GameEnvironment;

/**
 * Defines an interface for whot agents
 */
public interface GameAgent extends Runnable {

    // Returns the name of this agent.
    public String getName();

    // Requests the game agent to refresh the client status so it can take necessary action.
    public void refresh();

//    public String getMove(GameEnvironment environment);
}
