package org.anieanie.cardgame.dl.training.utils;

import org.anieanie.cardgame.training.PersistibleMultiLayerNetwork;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Saves neural nets and configuration to files.
 */
public class NetworkPersister {

    public static void saveNet(MultiLayerNetwork model, String configFile, String weightsFile) throws IOException {
        // Backup old versions of files.
        try {
            Files.move(Paths.get(configFile), Paths.get(configFile + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            Files.move(Paths.get(weightsFile), Paths.get(weightsFile + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Just keep going if for some reason we couldn't backup.
        }

        // Save new files.
        OutputStream fos = Files.newOutputStream(Paths.get(weightsFile));
        DataOutputStream dos = new DataOutputStream(fos);
        Nd4j.write(model.params(), dos);
        dos.flush();
        dos.close();
        FileUtils.writeStringToFile(new File(configFile), model.getLayerWiseConfigurations().toJson());
    }

    public static MultiLayerNetwork loadNet(String configFile, String weightsFile) throws IOException {
        MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(configFile)));
        DataInputStream dis = new DataInputStream(new FileInputStream(weightsFile));
        INDArray newParams = Nd4j.read(dis);
        dis.close();
        MultiLayerNetwork savedNetwork = new MultiLayerNetwork(confFromJson);
        savedNetwork.init();
        savedNetwork.setParams(newParams);
        return savedNetwork;
    }

    public static PersistibleMultiLayerNetwork loadNet(String configFile, String weightsFile, String name) throws IOException {
        MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(configFile)));
        DataInputStream dis = new DataInputStream(new FileInputStream(weightsFile));
        INDArray newParams = Nd4j.read(dis);
        dis.close();
        PersistibleMultiLayerNetwork savedNetwork = new PersistibleMultiLayerNetwork(confFromJson, name);
        savedNetwork.init();
        savedNetwork.setParams(newParams);
        return savedNetwork;
    }

}
