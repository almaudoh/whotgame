package org.anieanie.cardgame.learning.whot;

import org.anieanie.cardgame.training.PersistibleMultiLayerNetwork;
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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.util.*;

/**
 * A DQN
 */
public class DeepQNetwork {

    private static final String SAVED_NETS_DIR = "GameAI/resources/saved_nets";
    private final MultiLayerNetwork learner;
    private final int numInputs;
    private final int numOutputs;

    private List<double[]> replayMemoryFeatures;
    private List<double[]> replayMemoryLabels;

    private PersistibleMultiLayerNetwork target;

    private Map<String, Object> hyperParams;

    // Thread lock for synchronization.
    private final Object lock = new Object();

    public DeepQNetwork(int inputSize, int outputSize) {
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        numInputs = inputSize;
        numOutputs = outputSize;
        replayMemoryFeatures = new ArrayList<double[]>();
        replayMemoryLabels = new ArrayList<double[]>();
        loadReplayFromFile();
        hyperParams = initializeHyperParameters();
        try {
            // Try to load existing network.
            target = PersistibleMultiLayerNetwork.load(SAVED_NETS_DIR, "dqn");
        } catch (FileNotFoundException e) {
            // If it fails, then create a new network.
            target = new PersistibleMultiLayerNetwork(getNetworkConfiguration(), "dqn");
            target.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        learner = new MultiLayerNetwork(target.getLayerWiseConfigurations());
        learner.init();
        learner.setParams(target.params());
    }

    public void updateTarget() throws IOException {
        target.setParams(learner.params());
        target.save(SAVED_NETS_DIR);
    }

    public double output(double[] featureVector) {
        INDArray output = target.output(Nd4j.create(featureVector, new int[]{1, featureVector.length}), false);
        return output.getDouble(0);
    }

    public void addToReplayMemory(double[] features, double[] labels) {
        synchronized (lock) {
            replayMemoryFeatures.add(features);
            replayMemoryLabels.add(labels);
        }
    }

    // The learning network learns continually from the stored replay memory.
    public void learnFromMemory() {
        // No need to try to learn if enough samples have not been taken.
        if (replayMemoryFeatures.size() > 10) {
            double minError = (Double) hyperParams.get("minIterationError");
            int maxIterations = (Integer) hyperParams.get("maxIterations");

            // Pull stored data from replay memory and use it to carry out network training.
            DataSet replay;
            synchronized (lock) {
                replay = new DataSet(fromINDArray(replayMemoryFeatures), fromINDArray(replayMemoryLabels));
            }
            replay.shuffle();
            SplitTestAndTrain split = replay.splitTestAndTrain(10);

            int j = 0;
            do {
                learner.fit(split.getTrain());
                j++;
            } while (learner.score() > minError && j < maxIterations);
        }
    }

    // Converts an array list of doubles to an INDArray object.
    private INDArray fromINDArray(List<double[]> data) {
        int numElements = data.size();
        return Nd4j.create(data.toArray(new double[numElements][]));
    }

    private Map<String, Object> initializeHyperParameters() {
        Map<String, Object> returnVal = new HashMap<String, Object>();
        returnVal.put("seed", 123);
        returnVal.put("iterations", 123);
        returnVal.put("learningRate", 0.01);
        returnVal.put("hiddenSize", 500);
        returnVal.put("minIterationError", 0.05);
        returnVal.put("maxIterations", 500);
        return returnVal;
    }

    /**
     * Sets the hyper parameters for this DQN.
     */
    public DeepQNetwork setHyperParameter(String name, Object value) {
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
    protected void finalize() throws Throwable {
        super.finalize();
        saveReplayToFile();
    }

    public void resetReplayMemory() {
        synchronized (lock) {
            replayMemoryFeatures.clear();
            replayMemoryLabels.clear();
        }
    }

    public void saveReplayToFile() {
        synchronized (lock) {
            // Save the replay memory.
            try {
                PrintWriter pr = new PrintWriter(new FileOutputStream(new File(SAVED_NETS_DIR + "/dqn.replaymem.txt")));
                for (int i = 0; i < replayMemoryFeatures.size(); i++) {
                    for (double val : replayMemoryFeatures.get(i)) {
                        pr.printf("%s,", val);
                    }
                    pr.printf("%s%n", replayMemoryLabels.get(i)[0]);
                }
                pr.flush();
                pr.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadReplayFromFile() {
        synchronized (lock) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(SAVED_NETS_DIR + "/dqn.replaymem.txt"))));
                replayMemoryFeatures = new ArrayList<double[]>();
                replayMemoryLabels = new ArrayList<double[]>();
                while (br.ready()) {
                    String[] line = br.readLine().split(",");
                    double[] row = new double[line.length - 1];
                    for (int i = 0; i < line.length - 1; i++) {
                        row[i] = Double.parseDouble(line[i]);
                    }
                    replayMemoryFeatures.add(row);
                    replayMemoryLabels.add(new double[]{Double.parseDouble(line[line.length - 1])});
                }
            } catch (FileNotFoundException e) {
                System.out.println("replaymem.txt not found in filesystem.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
