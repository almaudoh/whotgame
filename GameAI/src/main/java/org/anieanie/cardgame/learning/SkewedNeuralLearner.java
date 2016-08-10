package org.anieanie.cardgame.learning;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.util.ArrayUtil;

import java.io.*;
import java.util.*;

/**
 * A neural network classifier that improves learning where one of the labels is very sparse.
 *
 * The sparse label is aggregated so its number and effect is increased in the training set.
 */
public class SkewedNeuralLearner extends DeepQNetwork {

    private List<double[]> replayMemoryPositive;
    private List<double[]> replayMemoryNegative;

    // Thread lock for synchronization.
    private final Object lock = new Object();

    public SkewedNeuralLearner(String name) {
        super(name);
        replayMemoryPositive = new ArrayList<double[]>();
        replayMemoryNegative = new ArrayList<double[]>();
    }

    @Override
    public void addToReplayMemory(double[] features, double[] labels) {
        addToReplayMemory(features, labels[0] == 1.);
    }

    public void addToReplayMemory(double[] features, boolean positive) {
        synchronized (lock) {
            if (positive) {
                replayMemoryPositive.add(features);
            }
            else {
                replayMemoryNegative.add(features);
            }
            System.out.printf("replayMemory: Positive (%s), Negative (%s)%n", replayMemoryPositive.size(), replayMemoryNegative.size());
        }
    }

    // The learning network learns continually from the stored replay memory.
    @Override
    public boolean learnFromMemory() {
        // No need to try to learn if enough samples have not been taken.
        int replaySizeToLearn = (Integer) hyperParams.get("replaySizeToLearn");
        if (replayMemoryPositive.size() > 0 && (replayMemoryPositive.size() > replaySizeToLearn || replaySizeToLearn == -1)) {
            double minCycleError = (Double) hyperParams.get("minCycleError");
            int maxCycles = (Integer) hyperParams.get("maxCycles");

            // Pull stored data from replay memory and use it to carry out network training.
            DataSet replay;
            synchronized (lock) {
                if (replaySizeToLearn == -1) replaySizeToLearn = replayMemoryPositive.size();
                replay = combineDataSet(replaySizeToLearn, 0.5f);
                replay.shuffle();

                int j = 0;
                do {
                    System.err.println("fit");
                    learner.fit(replay);
                    j++;
                } while (learner.score() > minCycleError && j < maxCycles);
                System.out.printf("[%s] learned from memory, learner score %s%n", name, learner.score());
            }
            return true;
        }
        return false;
    }

    private DataSet combineDataSet(int numPositive, float negativeToPositiveRatio) {
        long seed = System.currentTimeMillis();
        INDArray positive = fromINDArray(replayMemoryPositive);
        INDArray negative = fromINDArray(replayMemoryNegative);
        Nd4j.shuffle(positive, new Random(seed), ArrayUtil.range(1, positive.rank()));
        Nd4j.shuffle(negative, new Random(seed), ArrayUtil.range(1, negative.rank()));

        // Combine as per specified ratio.
        int numNegative = Math.round(numPositive * negativeToPositiveRatio);
        INDArray combinedFeatures = Nd4j.vstack(positive.get(NDArrayIndex.interval(0,numPositive)), negative.get(NDArrayIndex.interval(0,numNegative)));
        INDArray combinedLabels = Nd4j.vstack(Nd4j.ones(numPositive, 1), Nd4j.zeros(numNegative, 1));
        return new DataSet(combinedFeatures, combinedLabels);
    }

    /**
     * Sets the hyper parameters for this DQN.
     */
    @Override
    public SkewedNeuralLearner setHyperParameter(String name, Object value) {
        hyperParams.put(name, value);
        return this;
    }

    private MultiLayerConfiguration getNetworkConfiguration() {
        int seed = (Integer) hyperParams.get("seed");
        int iterations = (Integer) hyperParams.get("iterations");
        double learningRate = (Double) hyperParams.get("learningRate");
        int hiddenSize = (Integer) hyperParams.get("hiddenSize");

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
                        .nIn(numInputs) // # input nodes
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

    @Override
    public void resetReplayMemory() {
        synchronized (lock) {
            replayMemoryPositive.clear();
            replayMemoryNegative.clear();
        }
    }

    @Override
    public void saveReplayToFile() {
        synchronized (lock) {
            // Save the replay memory.
            try {
                StringBuilder data = new StringBuilder();
                for (double[] row : replayMemoryPositive) {
                    data.append(1.);
                    for (double val : row) {
                        data.append(',').append(val);
                    }
                    data.append('\n');
                }
                for (double[] row : replayMemoryNegative) {
                    data.append(0.);
                    for (double val : row) {
                        data.append(',').append(val);
                    }
                    data.append('\n');
                }
                FileUtils.writeStringToFile(new File(SAVED_NETS_DIR + "/" + name + ".replaymem.txt"), data.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void loadReplayFromFile() {
        synchronized (lock) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(SAVED_NETS_DIR + "/" + name + ".replaymem.txt"))));
                replayMemoryPositive = new ArrayList<double[]>();
                replayMemoryNegative = new ArrayList<double[]>();
                while (br.ready()) {
                    String[] line = br.readLine().split(",");
                    double[] row = new double[line.length - 1];
                    for (int i = 1; i < line.length; i++) {
                        row[i - 1] = Double.parseDouble(line[i]);
                    }
                    if (Double.parseDouble(line[0]) == 1) {
                        replayMemoryPositive.add(row);
                    }
                    else {
                        replayMemoryNegative.add(row);
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.printf("[%s] replaymem.txt not found in filesystem.%n", name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
