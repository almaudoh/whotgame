package org.anieanie.cardgame.learning.whot;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.training.PersistibleMultiLayerNetwork;
import org.anieanie.cardgame.training.whot.WhotCardNormalizer;
import org.canova.api.io.data.FloatWritable;
import org.canova.api.io.data.IntWritable;
import org.canova.api.writable.Writable;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * A DQN
 */
public class DeepQLearner {

    private static final String SAVED_NETS_DIR = "resources/saved_nets";
    private final MultiLayerNetwork learner;

    private DataSet replayMemory;

    private PersistibleMultiLayerNetwork target;

    private Map<String, Object> hyperParams;

    public DeepQLearner() {
        replayMemory = new DataSet();
        hyperParams = initializeHyperParameters();
        try {
            // Try to load existing network.
            target = PersistibleMultiLayerNetwork.load(SAVED_NETS_DIR, "dqn");
        } catch (FileNotFoundException e) {
            // If it fails, then create a new network.
            target = new PersistibleMultiLayerNetwork(getNetworkConfiguration(), "dqn");
        } catch (IOException e) {
            e.printStackTrace();
        }
        learner = new MultiLayerNetwork(target.getLayerWiseConfigurations());
        learner.init();
        learner.setParams(target.params());
    }

    public

    // The learning network learns continually from the stored replayMemory.
    private void learnFromMemory() {
        double minError = (Double) hyperParams.get("minIterationError");
        int maxIterations = (Integer) hyperParams.get("maxIterations");


        // Calculate objective dataset by using the loss function.
        replayMemory.


        replayMemory.shuffle();
        replayMemory.get(10);


//        log.info("Split data....");
        SplitTestAndTrain testAndTrain = replayMemory.splitTestAndTrain(0.8);
        DataSet train = testAndTrain.getTrain();
        DataSet test = testAndTrain.getTest();

        int j = 0;
        do {
            learner.fit(train);
            j++;
        } while (learner.score() > minError && j < maxIterations);

    }

    public double value(GameEnvironment env) {
        List<Action> actions = this.applicableActions(state);
        List<QValue> qs = new ArrayList<QValue>(actions.size());
        //create a Q-value for each action
        for(Action a : actions){
            //add q with initialized value
            qs.add(new QValue(state, a, this.qValue(state, a)));
        }
        return qs;
    }

    public QValue qValue(State s, Action a) {

    }


    private List<Action> applicableActions(State state) {
        return null;
    }

    @Override
    public double value(State s, Action a) {
        return this.learnQValue(s, a);
    }

    private double learnQValue(State state, Action action) {
        return 0;
    }

    @Override
    public double value(State s) {
        return QProvider.Helper.maxQ(this, s);
    }

    private Collection<Writable> getNormalizedInput(Card move, State state) {
        List<Writable> ret = new ArrayList<>();
        // The first feature is the move encoded in 12 inputs.
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(move), ret);
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoLabelSpace(move), ret);

        // The last five cards that have been played.
        Queue<Card> top5 = (Queue<Card>) state.get(GameState.VAR_TOP_CARDS);
        for (Card card : top5) {
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(card), ret);
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoLabelSpace(card), ret);
        }

        // For the third state condition (CalledCard) we only add the value for the shape.
        try {
            Card calledCard = WhotCard.fromString(state.get(GameState.VAR_CALLED_CARD).toString() + " 1");
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(calledCard), ret);
        }
        catch (IllegalArgumentException e) {
            fillWhotCardFeatures(WhotCardNormalizer.blankWhotCardFeatures(WhotCard.N_SHAPES), ret);
        }

        // Features array for the current market condition (either Normal, PickTwo or General).
        String marketMode = state.get(GameState.VAR_MARKET_MODE).toString();
        ret.add(new IntWritable(marketMode.equals("Normal") ? 1 : 0));
        ret.add(new IntWritable(marketMode.equals("PickTwo") ? 1 : 0));
        ret.add(new IntWritable(marketMode.equals("General") ? 1 : 0));

        // Player count.
        ret.add(new IntWritable(Integer.parseInt(state.get(GameState.VAR_PLAYERS).toString())));

        // Features for the whot cards in hand.
        CardSet myCards = (CardSet) state.get(GameState.VAR_CARDS);
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

    private Map<String, Object> initializeHyperParameters() {
        Map<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("seed", 123);
        returnVal.put("iterations", 123);
        returnVal.put("learningRate", 0.01);
        returnVal.put("hiddenSize", 500);
        return returnVal;
    }

    private MultiLayerConfiguration getNetworkConfiguration() {
        int seed = (Integer) hyperParams.get("seed");
        int iterations = (Integer) hyperParams.get("iterations");
        double learningRate = (Double) hyperParams.get("learningRate");
        int hiddenSize = (Integer) hyperParams.get("hiddenSize");
        int numFeatures = 120;
        int numOutputs = 1;

        return new NeuralNetConfiguration.Builder()
                .seed(seed) // Locks in weight initialization for tuning
                .iterations(iterations) // # training iterations predict/classify & backprop
                .learningRate(learningRate) // Optimization step size
//                .learningRateDecayPolicy(LearningRatePolicy.Exponential)
//                .lrPolicyDecayRate(0.001)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // Backprop to calculate gradients
                .weightInit(WeightInit.XAVIER) // Weight initialization
                .updater(Updater.NESTEROVS).momentum(0.9)
//                .regularization(true).l1(1E-6)
//                .useDropConnect(true).dropOut(0.5)
                .list() // # NN layers (doesn't count input layer)
                .layer(0, new DenseLayer.Builder()
                        .nIn(numFeatures) // # input nodes
                        .nOut(hiddenSize) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .activation("tanh") // Activation function type
                        .build()
                )
                .layer(1, new DenseLayer.Builder()
                        .nIn(hiddenSize) // # input nodes
                        .nOut(hiddenSize) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .activation("elu") // Activation function type
                        .build()
                )
                .layer(2, new DenseLayer.Builder()
                        .nIn(hiddenSize) // # input nodes
                        .nOut(hiddenSize) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .activation("elu") // Activation function type
                        .build()
                )
                .layer(3, new DenseLayer.Builder()
                        .nIn(hiddenSize) // # input nodes
                        .nOut(hiddenSize) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .activation("elu") // Activation function type
                        .build()
                )
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(hiddenSize) // # input nodes
                        .nOut(numOutputs) // # output nodes
                        .activation("identity")
                        .build()
                )
                .backprop(true).pretrain(false).build();
    }

}
