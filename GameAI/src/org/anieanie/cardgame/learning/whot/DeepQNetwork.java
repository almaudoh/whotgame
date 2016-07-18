package org.anieanie.cardgame.learning.whot;

import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.training.whot.WhotCardNormalizer;
import org.canova.api.io.data.FloatWritable;
import org.canova.api.io.data.IntWritable;
import org.canova.api.writable.Writable;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;

/**
 * A Q-Provider backed by a Deep Neural Network.
 */
public class DeepQNetwork implements QProvider {

    @Override
    public List<QValue> qValues(State state) {
        List<Action> actions = this.applicableActions(state);
        List<QValue> qs = new ArrayList<QValue>(actions.size());
        //create a Q-value for each action
        for(Action a : actions){
            //add q with initialized value
            qs.add(new QValue(state, a, this.qValue(state, a)));
        }
        return qs;
    }


    private List<Action> applicableActions(State state) {
        return null;
    }

    @Override
    public double qValue(State s, Action a) {
        return this.learnQValue(s, a);
    }

    private double learnQValue(State state, Action action) {
        return 0;
    }

    @Override
    public double value(State s) {
        return Helper.maxQ(this, s);
    }

    private Collection<Writable> getNormalizedInput(Card move, State state) {
        List<Writable> ret = new ArrayList<>();
        // The first feature is the move encoded in 12 inputs.
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(move), ret);
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoLabelSpace(move), ret);

        // The last five cards that have been played.
        Queue<Card> top5 = (Queue<Card>) state.get(WhotGameEnvironment.VAR_TOP_CARDS);
        for (Card card : top5) {
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(card), ret);
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoLabelSpace(card), ret);
        }

        // For the third state condition (CalledCard) we only add the value for the shape.
        try {
            Card calledCard = WhotCard.fromString(state.get(WhotGameEnvironment.VAR_CALLED_CARD).toString() + " 1");
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(calledCard), ret);
        }
        catch (IllegalArgumentException e) {
            fillWhotCardFeatures(WhotCardNormalizer.blankWhotCardFeatures(WhotCard.N_SHAPES), ret);
        }

        // Features array for the current market condition (either Normal, PickTwo or General).
        String marketMode = state.get(WhotGameEnvironment.VAR_MARKET_MODE).toString();
        ret.add(new IntWritable(marketMode.equals("Normal") ? 1 : 0));
        ret.add(new IntWritable(marketMode.equals("PickTwo") ? 1 : 0));
        ret.add(new IntWritable(marketMode.equals("General") ? 1 : 0));

        // Player count.
        ret.add(new IntWritable(Integer.parseInt(state.get(WhotGameEnvironment.VAR_PLAYERS).toString())));

        // Features for the whot cards in hand.
        CardSet myCards = (CardSet) state.get(WhotGameEnvironment.VAR_CARDS);
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoCompressedCardSpace(myCards.toArray(new Card[myCards.size()])), ret);
        return ret;
    }

    // Fills the specified buffer with the features that have been generated.
    private void fillWhotCardFeatures(INDArray features, List<Writable> buffer) {
        // Add the card features array.
        for (float feature : features.data().asFloat()) {
            buffer.add(new FloatWritable(feature));
        }
    }

}
