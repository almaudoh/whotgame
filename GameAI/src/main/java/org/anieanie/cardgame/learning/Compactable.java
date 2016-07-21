package org.anieanie.cardgame.learning;

/**
 * Interface that describes an object that can be compacted into an array of ints, floats or doubles suitable for
 * use as the input to a neural network.
 */
public interface Compactable {

    /**
     * @return
     *   An array of doubles obtained by compacting the elements of this compactable.
     */
    double[] getVector();

    // Combines all the doubles in the provided list of double arrays into one array of doubles
    static double[] combineElements(double[]... featureSets) {
        // Add the card features array.
        int length = 0;
        for (double[] featureSet : featureSets)
            length += featureSet.length;

        double[] buffer = new double[length];
        int i = 0;
        for (double[] featureSet : featureSets) {
            for (double feature : featureSet) {
                buffer[i] = feature;
                i++;
            }
        }
        return buffer;
    }

}
