package org.anieanie.cardgame.learning.whot;

import org.anieanie.card.CardSet;
import org.anieanie.cardgame.agent.SimpleWhotGameAgent;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.whot.WhotGameRule;

/**
 * This connects to the game client.
 */
public class IntelligentWhotGameAgent extends SimpleWhotGameAgent {

    public IntelligentWhotGameAgent(GameClient gameClient) {
        super(gameClient, "smart-" + Math.round(Math.random() * 10000));
    }

    /** Using the DQN, select the best move to play. */
    @Override
    protected String getMoveFromEnvironment(CardSet cards, GameEnvironment environment) {
        cards = WhotGameRule.filterValidMoves(cards, environment);
        if (cards.size() > 0) {
            cards.shuffle();
            return cards.getFirst().toString();
        }
        // If we reach here, then we don't have a card to play.
        return "MARKET";
    }

}
