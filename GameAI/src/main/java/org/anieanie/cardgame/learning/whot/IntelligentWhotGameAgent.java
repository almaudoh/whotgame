package org.anieanie.cardgame.learning.whot;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.agent.SimpleWhotGameAgent;
import org.anieanie.cardgame.dl.normalization.GameState;
import org.anieanie.cardgame.dl.normalization.CompactableUtility;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.whot.WhotGameRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Double.NEGATIVE_INFINITY;
import static org.anieanie.cardgame.gameplay.GameClient.*;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.VAR_TOP_CARD;

/**
 * This connects to the game client.
 */
public class IntelligentWhotGameAgent extends SimpleWhotGameAgent {

    private DeepQNetwork dqn;

    // The queue of the last five moves made.
    private CardSet top5;

    private GameState prevState;

    private Card prevAction;

    private double prevReward;

    private double epsilon = 0.8;

    private Random random;

    private volatile boolean keepTraining = true;

    public IntelligentWhotGameAgent(GameClient gameClient) {
        super(gameClient, "smart-" + Math.round(Math.random() * 10000));
        top5 = new CardSet();
        prevState = getGameState(gameClient.getCards(), top5, gameClient.getEnvironment());
//        prevState.set("card", null);
        random = new Random(1234);
        // @todo Fix concurrent modification error.
//        dqn = new DeepQNetwork(prevState.getVector().length, 1);
        dqn = new DeepQNetwork(190, 1);

        // Start a new thread for training the DQN learner
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (keepTraining) {
                    // Sleep for 5 seconds, then wake up and learn.
                    threadSleep(5000);
                    try {
                        dqn.learnFromMemory();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    dqn.updateTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void refresh() {
        // Pause for a while for DQN to update itself, the ask it to stop learning.
        threadSleep(5000);
        keepTraining = false;
    }

    /** Need to track the last five moves */
    @Override
    public void updateEnvironment(GameEnvironment environment) {
        // Check that the top card has actually changed.
        Card topCard = WhotCard.fromString(environment.get(VAR_TOP_CARD));
        if (!top5.contains(topCard)) {
            top5.addFirst(topCard);
            // The queue should only be five moves long.
            if (top5.size() > 5) {
                top5.removeLast();
            }
        }
        prevReward = calculateMoveReward(environment);
    }

    private double calculateMoveReward(GameEnvironment environment) {
        switch (gameClient.getClientStatus()) {
            case STATUS_GAME_WON:
                if (gameClient.getGameWinner().equals(getName())) {
                    // I am the winner. yay!!
                    return  10;
                }
                else {
                    // I lost???
                    return  -10;
                }
            default:
                // No reward for other actions.
                // @todo Might need to review this later.
                return  0;
        }
    }

    /** Using the DQN, select the best move to play. */
    @Override
    protected String getMoveFromEnvironment(CardSet cards, GameEnvironment environment) {
        cards = WhotGameRule.filterValidMoves(cards, environment);
        if (cards.size() > 0) {
            return getBestMove(cards, environment);
        }
        // If we reach here, then we don't have a card to play.
        return "MARKET";
    }

    private String getBestMove(CardSet cards, GameEnvironment environment) {
        GameState state = getGameState(cards, top5, environment);
        state.setCompactSequence(Arrays.asList("card", "top5", "env", "cards"));
        saveMiniBatch(state, environment);

        if (random.nextDouble() > epsilon) {
            // Use epsilon-greedy explore-learn split.
            prevAction = cards.remove(random.nextInt(cards.size()));
        }
        else {
            prevAction = argMaxQ(state, cards);
        }
        return prevAction.toString();
    }

    private void saveMiniBatch(GameState state, GameEnvironment env) {
        double totalReward;
        if (prevAction != null && prevState != null) {
            if (gameClient.getClientStatus() == STATUS_GAME_WON) {
                totalReward = prevReward;
            }
            else {
                WhotCardSet fullCardSet = new WhotCardSet();
                fullCardSet.initialize();
                fullCardSet.remove(WhotCard.fromString(env.get(VAR_TOP_CARD)));
                // @todo A little issue here around what to use for a' in Q(s',a'). Currently using all possible valid
                // moves, not just what is available to the player currently.
                totalReward = prevReward + maxQ(state, WhotGameRule.filterValidMoves(fullCardSet, env));
            }
            prevState.set("card", CompactableUtility.fromWhotCards(prevAction));
            dqn.saveReplay(state.getVector(), new double[]{totalReward});
        }
    }

    private double maxQ(GameState state, CardSet cards) {
        double[] values = new double[cards.size()];
        double maxQ = NEGATIVE_INFINITY;
        int i = 0;
        for (Card card : cards) {
            state.set("card", CompactableUtility.fromWhotCards(card));
            values[i] = dqn.output(state.getVector());
            if (values[i] > maxQ) {
                maxQ = values[i];
            }
            i++;
        }
        return maxQ;
    }

    private Card argMaxQ(GameState state, CardSet cards) {
        double[] values = new double[cards.size()];
        double maxQ = NEGATIVE_INFINITY;
        int i = 0;
        Card argMaxQ = null;
        for (Card card : cards) {
            state.set("card", CompactableUtility.fromWhotCards(card));
            values[i] = dqn.output(state.getVector());
            if (values[i] > maxQ) {
                maxQ = values[i];
                argMaxQ = card;
            }
            i++;
        }
        return argMaxQ;
    }

    private GameState getGameState(CardSet cards, CardSet top5, GameEnvironment environment) {
        GameState state = new GameState();
        state.set("top5", CompactableUtility.fromFixedSizeCardSet(top5, 5));
        state.set("env", CompactableUtility.fromGameEnvironment(environment));
        state.set("cards", CompactableUtility.fromWhotCardSet(cards));
        return state;
    }

}
