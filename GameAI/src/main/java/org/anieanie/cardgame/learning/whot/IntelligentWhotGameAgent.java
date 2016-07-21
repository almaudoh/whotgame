package org.anieanie.cardgame.learning.whot;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.agent.SimpleWhotGameAgent;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.whot.WhotGameRule;
import org.anieanie.cardgame.learning.CompactableGameEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This connects to the game client.
 */
public class IntelligentWhotGameAgent extends SimpleWhotGameAgent {

    private DeepQLearner learner = new DeepQLearner();

    // The queue of the last five moves made.
    private CardSet moveQueue;


    public IntelligentWhotGameAgent(GameClient gameClient) {
        super(gameClient, "smart-" + Math.round(Math.random() * 10000));
        moveQueue = new CardSet();
    }

    /** Using the DQN, select the best move to play. */
    @Override
    protected String getMoveFromEnvironment(CardSet cards, GameEnvironment environment) {
        cards = WhotGameRule.filterValidMoves(cards, environment);
        return getBestMove(cards, environment);
    }

    private String getBestMove(CardSet cards, GameEnvironment environment) {
        GameState state = new GameState();
        state.set("cards", (CompactableWhotCardSet) cards);
        state.set("env", (CompactableGameEnvironment) environment);
        state.setSequence(Arrays.asList("env", "cards"));
        return learner.getBestMove(cards, state);
    }


}
