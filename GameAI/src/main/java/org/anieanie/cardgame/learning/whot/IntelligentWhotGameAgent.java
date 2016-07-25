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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.anieanie.cardgame.gameplay.GameClient.*;
import static org.anieanie.cardgame.gameplay.GameEnvironment.VAR_PLAYER_COUNT;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.VAR_CALLED_CARD;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.VAR_MARKET_MODE;
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

    private double epsilon = 0.8;

    private Random random;

    private volatile boolean keepTraining = true;

    private double gamma = 0.9;

    // A flag to keep whether move was accepted or not. For calculating rewards.
    private volatile boolean moveAccepted = true;

    private volatile long movesRejected = 0;
    private volatile long movesAccepted = 0;

    public IntelligentWhotGameAgent(GameClient gameClient) {
        this(gameClient, 0.5, 0.9);
    }

    public IntelligentWhotGameAgent(GameClient gameClient, double epsilon, double gamma) {
        super(gameClient, "smart-" + Math.round(Math.random() * 10000));
        this.epsilon = epsilon;
        this.gamma = gamma;
        top5 = new CardSet();
        random = new Random(1234);
        prevState = buildGameState(gameClient.getCards(), top5, dummyEnvironment());
        prevState.set("move", CompactableUtility.fromWhotMove(WhotCard.MARKET));
        dqn = new DeepQNetwork(prevState.getVector().length, 1);

        // Start a new thread for training the DQN learner
        new Thread(new Runnable() {
            @Override
            public void run() {

                int iterations = 0;
                while (keepTraining) {
                    // Sleep for 1 seconds, then wake up and learn.
                    threadSleep(500);
                    try {
                        dqn.learnFromMemory();
                        if (iterations > 100) {
                            // Update learnings every 100 iterations.
                            updateDQNTarget();
                            printMetrics();
                            resetMetrics();
                            iterations = 0;
                        }
                        iterations++;
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

    private void resetMetrics() {
        movesAccepted = 0;
        movesRejected = 0;
    }

    private void printMetrics() {
        // Print the metrics of the current dqn target.
        if (movesAccepted + movesRejected > 0) {
            System.out.printf("Moves rejected: %s, moves accepted: %s, reject %%: %s%n", movesAccepted, movesRejected, movesRejected * 100 / (movesRejected + movesAccepted));
        }
        System.out.printf("Games won: %s, games lost: %s, win %%: %s%n", "n/a", "n/a", "n/a");
        dqn.scores();
    }

    @Override
    public void run() {
        super.run();
        try {
            while (gameClient.getClientStatus() != STATUS_GAME_WON && gameClient.getClientStatus() != STATUS_TERMINATE) {
                Thread.sleep(100);
            }
            // After completion of the playing loop, learning needs to be done for the terminal condition.
            GameState state = buildGameState(gameClient.getCards(), top5, gameClient.getEnvironment());
            saveMiniBatch(allCardsWithMarket(gameClient.getEnvironment()), state, gameClient.getEnvironment());
            updateDQNTarget();
            printMetrics();
            resetMetrics();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Updates the target network from what the learning network has learnt.
    private void updateDQNTarget() throws IOException {
        dqn.learnFromMemory();
        dqn.updateTarget();
        dqn.resetReplayMemory();
        dqn.saveReplayToFile();
    }

    @Override
    public void refresh() {
        // Pause for a while for DQN to update itself, the ask it to stop learning.
        threadSleep(5000);
        keepTraining = false;
    }

    @Override
    public void moveRejected(Card card) {
        moveAccepted = false;
        movesRejected++;
    }

    @Override
    public void moveAccepted(Card card) {
        moveAccepted = true;
        movesAccepted++;
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
    }

    /** Using the DQN, select the best move to play. */
    @Override
    protected String getMoveFromEnvironment(CardSet cards, GameEnvironment environment) {
//        cards = WhotGameRule.filterValidMoves(cards, environment);
        // Add market to the full card set.
//        if (cards.size() > 0) {
//            return getBestMove(cards, environment);
            return getBestMove(environment);
//        }
        // If we reach here, then we don't have a card to play.
//        return WhotGameRule.GO_MARKET;
    }

    private String getBestMove(GameEnvironment environment) {
        GameState state = buildGameState(gameClient.getCards(), top5, environment);
        WhotCardSet selectActions = allCardsWithMarket(environment);

        // @todo A little issue here around what to use for a' in Q(s',a'). Currently using all possible
        // moves, not just what is available to the player currently.
        saveMiniBatch(selectActions, state, environment);

        prevState = state;
        prevAction = simpleEpsilonGreedy(state, selectActions);
        return prevAction.toString();
    }

    private Card simpleEpsilonGreedy(GameState state, WhotCardSet selectActions) {
        // Now choose the action (which is also saved as the previousAction for the next iteration.
        if (random.nextDouble() > epsilon) {
            // Use epsilon-greedy explore-learn split.
            selectActions.shuffle(random, 19);
//            System.out.println("epsilon");
            return selectActions.getFirst();
        }
        else {
//            System.out.println("Q-value");
            return argMaxQ(qValues(state, selectActions));
        }
    }

    private Card improvedEpsilonQValue(GameState state, WhotCardSet selectActions) {
        Map<Card, Double> tempQ = qValues(state, selectActions);
        if (random.nextDouble() < epsilon) {
            double mag = Math.max(Math.abs(minQ(tempQ)), Math.abs(maxQ(tempQ)));
            for (Card card : selectActions) {
                // Add random values to all the actions, then recalculate maxQ.
                tempQ.put(card, tempQ.get(card) + random.nextDouble() * mag - .5 * mag);
//                q =[q[i] + random.random() * mag - .5 * mag for i in range(len(self.actions))]
            }
        }
        // @todo What if there are more than one max q value?
//        System.out.println("improved epsilon Q-value");
        return argMaxQ(tempQ);
    }

    // All cards available in the WhotCard including the market, but removing the top card.
    private WhotCardSet allCardsWithMarket(GameEnvironment env) {
        WhotCardSet selectActions = new WhotCardSet();
//        WhotGameRule.filterValidMoves(selectActions, environment);
        selectActions.initialize();
        selectActions.remove(WhotCard.fromString(env.get(VAR_TOP_CARD)));
        selectActions.add(WhotCard.MARKET);
        return selectActions;
    }

    private void saveMiniBatch(CardSet nextActions, GameState state, GameEnvironment env) {
        double totalReward;
        double prevReward = calculateMoveReward(env);
        if (prevAction != null && prevState != null) {
            if (gameClient.getClientStatus() == STATUS_GAME_WON || gameClient.getClientStatus() == STATUS_TERMINATE) {
                totalReward = prevReward;
            }
            else {
                // tt = rr + maxQ(s', a'| a')
//                totalReward = prevReward + gamma * maxQ(state, WhotGameRule.filterValidMoves(fullCardSet, env));
                totalReward = prevReward + gamma * maxQ(qValues(state, nextActions));
            }
            prevState.set("move", CompactableUtility.fromWhotMove(prevAction));
            dqn.addToReplayMemory(prevState.getVector(), new double[]{totalReward});
        }
    }

    private Map<Card, Double> qValues(GameState state, CardSet selectActions) {
        Map<Card, Double> values = new HashMap<Card, Double>();
        for (Card card : selectActions) {
            state.set("move", CompactableUtility.fromWhotMove(card));
            values.put(card, dqn.output(state.getVector()));
        }
        return values;
    }

    private double minQ(Map<Card, Double> qVals) {
        double minQ = POSITIVE_INFINITY;
        for (Card card : qVals.keySet()) {
            if (qVals.get(card) < minQ) {
                minQ = qVals.get(card);
            }
        }
        return minQ;
    }

    private double maxQ(Map<Card, Double> qVals) {
        double maxQ = NEGATIVE_INFINITY;
        for (Card card : qVals.keySet()) {
            if (qVals.get(card) > maxQ) {
                maxQ = qVals.get(card);
            }
        }
        return maxQ;
    }

    private Card argMaxQ(Map<Card, Double> qVals) {
        double maxQ = NEGATIVE_INFINITY;
        // Use the first action as default. This is to avoid the null pointer error that happens
        // when all the values are NaNs.
        Card argMaxQ = qVals.keySet().iterator().next();
        for (Card card : qVals.keySet()) {
            if (qVals.get(card) > maxQ) {
                maxQ = qVals.get(card);
                argMaxQ = card;
            }
        }
        return argMaxQ;
    }

    private GameState buildGameState(CardSet cards, CardSet top5, GameEnvironment environment) {
        GameState state = new GameState();
        state.set("top5", CompactableUtility.fromFixedSizeCardSet(top5, 5));
        state.set("env", CompactableUtility.fromGameEnvironment(environment));
        state.set("cards", CompactableUtility.fromWhotCardSet(cards));
        state.setCompactSequence(Arrays.asList("move", "top5", "env", "cards"));
        return state;
    }

    private double calculateMoveReward(GameEnvironment environment) {
        switch (gameClient.getClientStatus()) {
            case STATUS_GAME_WON:
            case STATUS_TERMINATE:
                if (gameClient.getGameWinner().equals(getName())) {
                    // I am the winner. yay!!
                    return  10.;
                }
                else if (!gameClient.getGameWinner().equals("")) {
                    // I lost???
                    return  -10.;
                }
            default:
                // Rejected moves have high negative reward also.
                return moveAccepted ? 0. : -10.;
        }
    }

    // Returns a dummy environment for calculating vector width.
    private GameEnvironment dummyEnvironment() {
        GameEnvironment env = new GameEnvironment();
        env.put(VAR_CALLED_CARD, "");
        env.put(VAR_TOP_CARD, WhotCard.MARKET.toString());
        env.put(VAR_MARKET_MODE, "Normal");
        env.put(VAR_PLAYER_COUNT, "0");
        return env;
    }

}
