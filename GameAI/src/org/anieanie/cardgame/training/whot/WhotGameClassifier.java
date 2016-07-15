package org.anieanie.cardgame.training.whot;

import org.anieanie.dl.training.utils.NetworkPersister;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.split.FileSplit;
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

public class WhotGameClassifier {

    private static Logger log = LoggerFactory.getLogger(WhotGameClassifier.class);

    private static final String RESOURCE_DIR = "GameAI/resources";
    private static final String SAVED_NETS_DIR = RESOURCE_DIR + "/saved_nets";

    public static void main(String[] args) throws Exception {
        // Customizing params
        Nd4j.MAX_SLICES_TO_PRINT = -1;
        Nd4j.MAX_ELEMENTS_PER_SLICE = -1;
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;

        int maxEpochs = 1000;
        double minError = 0.0001;
        int listenerFreq = 1;
        int labelField = 0;
        int labelCount = 22;
        int labelIndexFrom  = 0;
        int labelIndexTo = 21;
        int batchSize = 1000;
        int splitTrainNum = (int) (batchSize * .8);
        int seed = 123;

        log.info("Load data....");
        RecordReader reader = new WhotGameRecordReader();
        reader.initialize(new FileSplit(new File(RESOURCE_DIR + "/saved_moves.txt")));
        DataSetIterator iter = new RecordReaderDataSetIterator(reader, batchSize, labelIndexFrom, labelIndexTo, true);
        DataSet next = iter.next();
        next.shuffle();

        log.info("Split data....");
        SplitTestAndTrain testAndTrain = next.splitTestAndTrain(splitTrainNum, new Random(seed));
        DataSet train = testAndTrain.getTrain();
        DataSet test = testAndTrain.getTest();

        log.info("Build model....");
        MultiLayerNetwork model = loadOrGetNeuralNetwork(labelCount, seed);
        model.init();
        model.setListeners(new ScoreIterationListener(listenerFreq));

        log.info("Backup old config....");
        Files.move(Paths.get(SAVED_NETS_DIR + "/conf.json"), Paths.get(SAVED_NETS_DIR + "/conf.bak.json"), StandardCopyOption.REPLACE_EXISTING);
        Files.move(Paths.get(SAVED_NETS_DIR + "/coefficients.bin"), Paths.get(SAVED_NETS_DIR + "/coefficients.bak.bin"), StandardCopyOption.REPLACE_EXISTING);

        log.info("Train model....");
        int ep = 0;
        do {
            model.fit(train);
            if (ep % 100 == 0) {
                // Save network every 100 epochs in case I abort halfway.
                NetworkPersister.saveNet(model, SAVED_NETS_DIR + "/conf.json", SAVED_NETS_DIR + "/coefficients.bin");
            }

        } while (ep++ < maxEpochs && model.score() > minError);

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(labelCount);
        eval.eval(test.getLabels(), model.output(test.getFeatureMatrix(), Layer.TrainingMode.TEST));
        log.info(eval.stats());

        System.out.println(WhotCardResultDisplay.formatOutput(test, model.output(test.getFeatureMatrix())));
        System.out.println("");
        System.out.println(WhotCardResultDisplay.formatOutput(train, model.output(train.getFeatureMatrix())));
    }

    private static MultiLayerNetwork loadOrGetNeuralNetwork(int labelCount, int seed) {
        try {
            return loadMultilayerNetwork();
        } catch (IOException e) {
            e.printStackTrace();
            return new MultiLayerNetwork(getNNConfiguration(labelCount, seed));
        }
    }

    private static MultiLayerNetwork loadMultilayerNetwork() throws IOException {
        return NetworkPersister.loadNet(SAVED_NETS_DIR + "/conf.json", SAVED_NETS_DIR + "/coefficients.bin");
    }

    private static MultiLayerConfiguration getNNConfiguration(int labelCount, int seed) {
        final int numFeatures = 101;
        int iterations = 1;
        double learningRate = 0.01;
        int hiddenSize = 500;

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
