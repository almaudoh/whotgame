package org.anieanie.cardgame.training.whot;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Custom displays of training results.
 */
public class WhotCardResultDisplay {
    static String formatOutput(DataSet input, INDArray output) {
        StringBuilder string = new StringBuilder("[\n");
        for (int i = 0; i < output.rows(); i++) {
            string
                .append("\t[")
                .append(getTop3CardShapes(output.getRow(i)))
                .append(" ")
                .append(getTop3CardLabels(output.getRow(i)))
                .append("] ")
                .append(getTopCardShape(input.getLabels().getRow(i)))
                .append(" ")
                .append(getTopCardLabel(input.getLabels().getRow(i)))
                .append("\n");
        }
        return string.append("\n]").toString();
    }

    private static String getTop5CardSpace(INDArray row) {
        INDArray[] top5 = Nd4j.sortWithIndices(row, 1, false);
        return String.format("%2d|%5.1f%%, %2d|%5.1f%%, %2d|%5.1f%%, %2d|%5.1f%%, %2d|%5.1f%%",
                top5[0].getInt(0), top5[1].getFloat(0) * 100,
                top5[0].getInt(1), top5[1].getFloat(1) * 100,
                top5[0].getInt(2), top5[1].getFloat(2) * 100,
                top5[0].getInt(3), top5[1].getFloat(3) * 100,
                top5[0].getInt(4), top5[1].getFloat(4) * 100
            );
    }

    private static String getTop3CardShapes(INDArray row) {
        INDArray[] top3 = Nd4j.sortWithIndices(row.getColumns(0,1,2,3,4,5,6), 1, false);
        return String.format("shapes( %2d|%5.1f%%, %2d|%5.1f%% , %2d|%5.1f%% )",
                top3[0].getInt(0), top3[1].getFloat(0) * 100,
                top3[0].getInt(1), top3[1].getFloat(1) * 100,
                top3[0].getInt(2), top3[1].getFloat(2) * 100
        );
    }

    private static String getTop3CardLabels(INDArray row) {
        INDArray[] top3 = Nd4j.sortWithIndices(row.getColumns(7,8,9,10,11,12,13,14,15,16,17,18,19,20,21), 1, false);
        if (top3[1].getFloat(0) == 0.) {
            return ""; // This means market was picked.
        }
        else {
            return String.format("labels( %2d|%5.1f%%, %2d|%5.1f%% , %2d|%5.1f%% )",
                    top3[0].getInt(0), top3[1].getFloat(0) * 100,
                    top3[0].getInt(1), top3[1].getFloat(1) * 100,
                    top3[0].getInt(2), top3[1].getFloat(2) * 100
            );
        }
    }

    private static String getTopCardShape(INDArray row) {
        INDArray[] top3 = Nd4j.sortWithIndices(row.getColumns(0,1,2,3,4,5,6), 1, false);
        if (top3[0].getInt(0) == 0) { // MARKET
            return "market";
        }
        else {
            return String.format("%2d", top3[0].getInt(0));
        }
    }

    private static String getTopCardLabel(INDArray row) {
        INDArray[] top = Nd4j.sortWithIndices(row.getColumns(7,8,9,10,11,12,13,14,15,16,17,18,19,20,21), 1, false);
        if (top[1].getFloat(0) == 0.) {
            return ""; // This means market was picked.
        }
        else {
            return String.format("%2d", top[0].getInt(0));
        }
    }

    private static String showLabelName(INDArray labels) {
        INDArray[] top1 = Nd4j.sortWithIndices(labels, 1, false);
        int index = top1[0].getInt(0);
        try {
            return String.format("%2d (%s)", index, WhotCardNormalizer.cardFromCardPosition(index));
        }
        catch (IllegalArgumentException e) {
            return String.format("%2d (Illegal card)", index);
        }
    }
}
