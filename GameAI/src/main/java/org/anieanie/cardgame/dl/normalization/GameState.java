package org.anieanie.cardgame.dl.normalization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState extends HashMap<String, Compactable> implements Compactable {

    private List<String> sequence;

    private List<String> defaultSequence = new ArrayList<String>();

    public GameState copyWithName(String objectName) {
        return (GameState) super.clone();
//        return new GameState(topCard, calledCard, marketMode, playerCount, objectName);
    }

    public void setCompactSequence(List<String> sequence) {
        this.sequence = sequence;
    }

    @Override
    public Compactable put(String key, Compactable value) {
        // Add to the default sequence.
        defaultSequence.add(key);
        return super.put(key, value);
    }

    @Override
    public Compactable get(Object key) {
        defaultSequence.add((String) key);
        return super.get(key);
    }

    public GameState set(String variableKey, Compactable value) {
        // Add to the default sequence.
        this.put(variableKey, value);
        return this;
    }

    public GameState copy() {
        return (GameState) super.clone();
//        return new GameState(topCard, calledCard, marketMode, playerCount, name);
    }

    @Override
    public double[] getVector() {
        if (sequence == null) {
            sequence = defaultSequence;
        }

        // Get the total length of all the features.
        int length = 0, i = 0;
        double[][] parts = new double[sequence.size()][];
        for (String key : sequence) {
            parts[i] = get(key).getVector();
            length += parts[i].length;
            i++;
        }

        double[] buffer = new double[length];
        int j = 0;
        for (double[] part : parts) {
            for (double value : part) {
                buffer[j] = value;
                j++;
            }
        }
        return buffer;
    }

}
