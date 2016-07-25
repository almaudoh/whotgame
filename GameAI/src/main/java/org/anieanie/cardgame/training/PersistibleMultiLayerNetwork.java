package org.anieanie.cardgame.training;

import org.anieanie.cardgame.dl.training.utils.NetworkPersister;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;

/**
 * This class defines a neural network that can be persisted (i.e. saved) and reloaded for continuous training.
 */
public class PersistibleMultiLayerNetwork extends MultiLayerNetwork {

    private String name;

    public PersistibleMultiLayerNetwork(MultiLayerConfiguration conf, String name) {
        super(conf);
        this.name = name;
    }

    public PersistibleMultiLayerNetwork(String conf, INDArray params, String name) {
        super(conf, params);
        this.name = name;
    }

    public PersistibleMultiLayerNetwork(MultiLayerConfiguration conf, INDArray params, String name) {
        super(conf, params);
        this.name = name;
    }

    public void save(String path) throws IOException {
        NetworkPersister.saveNet(this, String.format("%s/%s.conf.json", path, name), String.format("%s/%s.coeff.bin", path, name));
    }

    public static PersistibleMultiLayerNetwork load(String path, String name) throws IOException {
        PersistibleMultiLayerNetwork model = NetworkPersister.loadNet(String.format("%s/%s.conf.json", path, name), String.format("%s/%s.coeff.bin", path, name), name);
        model.name = name;
        return model;
    }

}
