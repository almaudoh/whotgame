package org.anieanie.cardgame.training.whot;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.dl.normalization.CompactableUtility;
import org.anieanie.cardgame.dl.normalization.GameState;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.whot.WhotGameRule;
import org.anieanie.cardgame.learning.SkewedNeuralLearner;

import java.io.IOException;
import java.util.Random;

import static org.anieanie.cardgame.gameplay.GameEnvironment.VAR_CURRENT_PLAYER;
import static org.anieanie.cardgame.gameplay.GameEnvironment.VAR_PLAYER_COUNT;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.*;

/**
 *
 */
public class ValidMoveClassifier {

    public static void main(String[] args) {
        ValidMoveClassifier classifier = new ValidMoveClassifier();
        classifier.run(100);
    }

    private final SkewedNeuralLearner net;

    ValidMoveClassifier() {
        net = new SkewedNeuralLearner("trained_validator");
        net.setHyperParameter("minCycleError", 0.01)
                .setHyperParameter("maxCycles", 50)
                .setHyperParameter("learningRate", 0.01)
                .setHyperParameter("replaySizeToLearn", -1); // Learn from all replay memory.
    }

    public void run(int epochs) {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < epochs; i++) {
            Card topCard = randomWhotCard(new WhotCardSet(), random);
            GameEnvironment env = randomGameEnvironment(random, topCard);
            CardSet without = new WhotCardSet();
            without.add(topCard);
            CardSet hand = randomWhotCardSet(random.nextInt(10), without, random);
            GameState state = buildGameState(hand, env);

            state.put("move", CompactableUtility.fromWhotMove(WhotCard.MARKET));
            net.init(state.getVector().length, 1);

            WhotGameRule rule = new WhotGameRule();
            for (Card move : allCardsWithMarket(env)) {
                state.put("move", CompactableUtility.fromWhotMove(move));
                net.addToReplayMemory(state.getVector(), hand.contains(move) && rule.isValidMove(move, env));
            }

            // Learn from memory and then update the target network.
            net.learnFromMemory();
            try {
                net.updateTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Test net now.
        CardSet hand = new WhotCardSet();
        hand.add(WhotCard.fromString("Star 1"));
        hand.add(WhotCard.fromString("Star 7"));
        hand.add(WhotCard.fromString("Whot 20"));
        hand.add(WhotCard.fromString("Triangle 4"));
        GameEnvironment env = buildGameEnvironment(WhotCard.fromString("Cross 1"), "", 0, 2, "", "Player1");
        GameState state = buildGameState(hand, env);

        // Test game set.
        CardSet testSet = new WhotCardSet();
        testSet.addAll(hand);
        testSet.add(WhotCard.MARKET);
        testSet.add(WhotCard.fromString("Cross 11"));

        for (Card card : testSet) {
            state.put("move", CompactableUtility.fromWhotMove(card));
            System.out.printf("Move: %s; valid: %s%n", card, net.output(state.getVector()));
        }
    }

    // All cards available in the WhotCard including the market, but removing the top card.
    private WhotCardSet allCardsWithMarket(GameEnvironment env) {
        WhotCardSet selectActions = new WhotCardSet();
        selectActions.initialize();
        selectActions.add(WhotCard.MARKET);
        return selectActions;
    }

    private GameEnvironment randomGameEnvironment(Random random, Card topCard) {
        return buildGameEnvironment(
                topCard,
                topCard.getShape() == WhotCard.WHOT ? WhotCard.SHAPES.get(random.nextInt(6)) : "",
                random.nextInt(3),
                2,
                random.nextBoolean()  ? "Player1" : "Player2",
                random.nextBoolean() ? "Player1" : "Player2"
             );
    }

    private GameEnvironment buildGameEnvironment(Card topCard, String calledCard, int marketMode, int playerCount, String generalMarketPlayer, String currentPlayer) {
        GameEnvironment ret = new GameEnvironment();
        ret.put(VAR_TOP_CARD, topCard.toString());
        ret.put(VAR_CALLED_CARD, calledCard);
        ret.put(VAR_MARKET_MODE, marketMode == 2 ? "General" : marketMode == 1 ? "PickTwo" : "Normal");
        ret.put(VAR_PLAYER_COUNT, String.valueOf(playerCount));
        ret.put(VAR_GENERAL_MARKET_PLAYER, generalMarketPlayer);
        ret.put(VAR_CURRENT_PLAYER, currentPlayer);
        return ret;
    }

    private Card randomWhotCard(CardSet without, Random random) {
        WhotCardSet set = new WhotCardSet();
        set.initialize();
        set.removeAll(without);
        set.shuffle(random, 30);
        return set.getFirst();
    }

    private CardSet randomWhotCardSet(int size, CardSet without, Random random) {
        WhotCardSet set = new WhotCardSet();
        set.initialize();
        set.removeAll(without);
        while (set.size() > size) {
            set.shuffle(random);
            set.removeFirst();
        }
        return set;
    }

    private GameState buildGameState(CardSet hand, GameEnvironment environment) {
        GameState state = new GameState();
        state.set("top", CompactableUtility.fromWhotCards(WhotCard.fromString(environment.get(VAR_TOP_CARD))));
        state.set("env", CompactableUtility.fromGameEnvironment(environment));
        state.set("hand", CompactableUtility.fromWhotCardSet(hand));
        state.setCompactSequence("move", "top", "env", "hand");
        return state;
    }

}
