package org.anieanie.cardgame.training.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCardSet;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.split.FileSplit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 */
public class LogSave {

    public static void main(String[] args) throws Exception {
//        turnMovesToLog();
        generateCardLog();
    }

    private static void generateCardLog() throws FileNotFoundException {
        WhotCardSet cards = new WhotCardSet();
        cards.initialize();
        cards.shuffle(10);
        PrintWriter pr = new PrintWriter(new File("cardlog.csv"));
        for (Card card : cards) {
            // The card's spatial position is added as the first feature.
            pr.write(Integer.toString(WhotCardNormalizer.cardSpacePosition(card)));
            pr.write(',');

            // The card's label and shape are added to the feature list.
            pr.write(Integer.toString(card.getShape()));
            pr.write(',');
            pr.write(Integer.toString(card.getLabel()));
            pr.write(',');

            // Features array for the cards the player is holding.
            boolean first = true;
            for (double pos : WhotCardNormalizer.expandIntoCardSpace(card)) {
                // Prepend the commas.
                if (first)
                    first = false;
                else
                    pr.write(',');
                pr.write(Double.toString(pos));
            }
            pr.write('\n');
        }
        pr.flush();
        pr.close();
    }

    private static void turnMovesToLog() throws IOException, InterruptedException {
        RecordReader reader = new WhotGameRecordReader();
        reader.initialize(new FileSplit(new File("transient/machine_moves.txt")));
        PrintWriter pr = new PrintWriter(new File("gamelog.csv"));
        while (reader.hasNext()) {
            Object[] row = reader.next().toArray();
            boolean first = true;
            for (Object cell : row) {
                // Prepend the commas.
                if (first)
                    first = false;
                else
                    pr.append(',');
                pr.append(cell.toString());
            }
            pr.append('\n');
        }
        pr.flush();
        pr.close();
    }


}
