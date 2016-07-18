package org.anieanie.cardgame.learning.whot;

import burlap.behavior.singleagent.learning.modellearning.LearnedModel;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import org.anieanie.cardgame.training.PersistibleMultiLayerNetwork;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.List;

/**
 * A learned model that approximates the transition probabilities using a Deep Neural network.
 *
 * In this case, transition probabilities is considered the probability of the next state the
 * agent will see after playing a certain move. Not the state immediately after its move but rather the state that
 * results after all other agents have played their own moves.
 */
public class DeepLearnedModel implements LearnedModel {

    private PersistibleMultiLayerNetwork model;

    public DeepLearnedModel() {
        model = null;
    }

    @Override
    public void updateModel(EnvironmentOutcome eo) {
        // This should check the DeepQNetwork and update it appropriately.
    }

    @Override
    public void resetModel() {

    }

    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        return null;
    }

    @Override
    public EnvironmentOutcome sample(State s, Action a) {
        return null;
    }

    @Override
    public boolean terminal(State s) {
        return false;
    }

    private MultiLayerConfiguration getModelConfiguration() {
        int seed = 123;
        int iterations = 1;
        double learningRate = 0.1;
        int numFeatures = 45;
        int hiddenSize = 500;
        int labelCount = 10;
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
                        .nOut(labelCount) // # output nodes
                        .activation("identity")
                        .build()
                )
                .backprop(true).pretrain(false).build();
    }
}
