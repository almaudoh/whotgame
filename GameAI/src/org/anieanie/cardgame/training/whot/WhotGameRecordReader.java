package org.anieanie.cardgame.training.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCard;
import org.canova.api.io.data.FloatWritable;
import org.canova.api.io.data.IntWritable;
import org.canova.api.io.data.Text;
import org.canova.api.records.reader.impl.LineRecordReader;
import org.canova.api.writable.Writable;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads the game logs saved during a whot game and provides a proper dataset with the right labels.
 */
public class WhotGameRecordReader extends LineRecordReader {

    @Override
    public Collection<Writable> next() {
        Text t = (Text) super.next().iterator().next();
        String val = t.toString();
        String[] fields = val.split(",", -1);
        List<Writable> ret = new ArrayList<>();
        // The first state condition is the feature for whether market was picked or not.
        if (fields[0].equalsIgnoreCase("MARKET")) {
            ret.add(new IntWritable(1));
            fillWhotCardFeatures(WhotCardNormalizer.blankWhotCardFeatures(21), ret);
        }
        else {
            ret.add(new IntWritable(0));
            // The card that was predicted.
            Card card = WhotCard.fromString(fields[0]);
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(card), ret);
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoLabelSpace(card), ret);
        }

        // The top card.
        Card card = WhotCard.fromString(fields[1]);
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(card), ret);
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoLabelSpace(card), ret);

        // For the third state condition (CalledCard) we only add the value for the shape.
        // The premise here is that an invalid string will return -1. Verify that premise.
        if (WhotCard.getShapeInt(fields[2]) != -1) {
            fillWhotCardFeatures(WhotCardNormalizer.expandIntoShapeSpace(WhotCard.fromString(fields[2] + " 1")), ret);
        }
        else {
            fillWhotCardFeatures(WhotCardNormalizer.blankWhotCardFeatures(WhotCard.N_SHAPES), ret);
        }

        // Features array for the current market condition (either Normal, PickTwo or General).
        ret.add(new IntWritable(fields[3].equals("Normal") ? 0 : 1));
        ret.add(new IntWritable(fields[3].equals("PickTwo") ? 0 : 1));
        ret.add(new IntWritable(fields[3].equals("General") ? 0 : 1));

        // Features for the whot cards in hand.
        fillWhotCardFeatures(WhotCardNormalizer.expandIntoCardSpace(fields, 4), ret);
        return ret;
    }

    // Fills the specified buffer with the features that have been generated.
    private void fillWhotCardFeatures(INDArray features, List<Writable> buffer) {
        // Add the card features array.
        for (float feature : features.data().asFloat()) {
            buffer.add(new FloatWritable(feature));
        }
    }

    public Collection<Writable> record(URI uri, DataInputStream dataInputStream) throws IOException {
        throw new UnsupportedOperationException("Reading CSV data from DataInputStream not yet implemented");
    }

}
