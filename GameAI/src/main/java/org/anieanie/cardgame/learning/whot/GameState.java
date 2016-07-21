package org.anieanie.cardgame.learning.whot;

import org.anieanie.cardgame.learning.Compactable;

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

    public void setSequence(List<String> sequence) {
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
        int length = 0;
        for (String key : sequence)
            length += sequence.size();

        double[] buffer = new double[length];
        int i = 0;
        for (String key : sequence) {
            for (double value : get(key).getVector()) {
                buffer[i] = value;
                i++;
            }
        }
        return buffer;
    }

}
