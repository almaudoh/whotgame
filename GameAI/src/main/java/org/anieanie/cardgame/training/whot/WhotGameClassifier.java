package org.anieanie.cardgame.training.whot;

import org.anieanie.cardgame.dl.training.utils.NetworkPersister;
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

public class WhotGameClassifier {

    private static Logger log = LoggerFactory.getLogger(WhotGameClassifier.class);

    private static final String SAVED_NETS_DIR = "transient/saved_nets";

    private String name;
    private MultiLayerNetwork model;

    public static void main(String[] args) throws Exception {
        WhotGameClassifier classifier = new WhotGameClassifier("classifier");
        classifier.init();
        classifier.train();
    }

    public WhotGameClassifier(String name) {
        this.name = name;
    }

    public void init() throws IOException, InterruptedException {
        // Customizing params
        Nd4j.MAX_SLICES_TO_PRINT = -1;
        Nd4j.MAX_ELEMENTS_PER_SLICE = -1;
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;

        int labelCount = 22;
        int listenerFreq = 1;
        int seed = 123;
        int numFeatures = 101;
        int iterations = 1;
        double learningRate = 0.01;

        log.info("Build model....");
        model = loadOrGetNeuralNetwork(labelCount, numFeatures, iterations, learningRate, seed);
        model.init();
        model.setListeners(new ScoreIterationListener(listenerFreq));
    }

    public void train() throws IOException, InterruptedException {
        int labelField = 0;
        int labelCount = 22;
        int maxEpochs = 500;
        double minError = 0.1;
        int labelIndexTo = 21;
        int batchSize = 1000;
        int labelIndexFrom = 0;
        double trainFraction = 0.8;

        log.info("Load data....");
        RecordReader reader = new WhotGameRecordReader();
        reader.initialize(new FileSplit(new File(String.format("%s/%s.saved_moves.txt", SAVED_NETS_DIR, name))));

        log.info("Train model....");
        DataSetIterator iter = new RecordReaderDataSetIterator(reader, batchSize, labelIndexFrom, labelIndexTo, true);
        DataSet next;
        while (iter.hasNext()) {
            log.info("Load batch....");
            next = iter.next();
            next.shuffle();

            log.info("Split data....");
            SplitTestAndTrain testAndTrain = next.splitTestAndTrain(trainFraction);
            DataSet train = testAndTrain.getTrain();
            DataSet test = testAndTrain.getTest();

            int j = 0;
            do {
                model.fit(train);
                j++;
            } while (model.score() > minError && j < maxEpochs);

            log.info("Save model...");
            saveMultiLayerNetwork();

            log.info("Evaluate model....");
            Evaluation eval = new Evaluation(labelCount);
            eval.eval(test.getLabels(), model.output(test.getFeatureMatrix(), Layer.TrainingMode.TEST));
            log.info(eval.stats());
        }

        DataSet evalset = iter.next();
        System.out.println(WhotCardResultDisplay.formatOutput(evalset, model.output(evalset.getFeatureMatrix())));
//        System.out.println("");
//        System.out.println(WhotCardResultDisplay.formatOutput(train, model.output(train.getFeatureMatrix())));
    }

    private MultiLayerNetwork loadOrGetNeuralNetwork(int labelCount, int numFeatures, int iterations, double learningRate, int seed) {
        try {
            return loadMultiLayerNetwork();
        } catch (IOException e) {
//            e.printStackTrace();
            return new MultiLayerNetwork(getNNConfiguration(labelCount, numFeatures, iterations, learningRate, seed));
        }
    }

    private void saveMultiLayerNetwork() throws IOException {
        log.info("Backup old config....");
        try {
            String filePrefix = String.format("%s/%s", SAVED_NETS_DIR, name);
            Files.move(Paths.get(filePrefix + ".conf.json"), Paths.get(filePrefix + ".conf.bak.json"), StandardCopyOption.REPLACE_EXISTING);
            Files.move(Paths.get(filePrefix + ".coeff.bin"), Paths.get(filePrefix + ".coeff.bak.bin"), StandardCopyOption.REPLACE_EXISTING);
            NetworkPersister.saveNet(model, filePrefix + ".conf.json", filePrefix + ".coeff.bin");
        } catch (IOException e) {
            // Just continue if we couldn't backup.
        }
    }

    private MultiLayerNetwork loadMultiLayerNetwork() throws IOException {
        String filePrefix = String.format("%s/%s", SAVED_NETS_DIR, name);
        return NetworkPersister.loadNet(filePrefix + ".conf.json", filePrefix + ".coeff.bin");
    }

    private MultiLayerConfiguration getNNConfiguration(int labelCount, int numFeatures, int iterations, double learningRate, int seed) {
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
