package org.anieanie.cardgame.training.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

/**
 * Carries out various normalization schemes for WhotCards.
 */
public class WhotCardNormalizer {

    private static final WhotCardSet fullCardSet;

    static {
        fullCardSet = new WhotCardSet();
        fullCardSet.initialize();
    }

    public static INDArray expandIntoShapeSpace(Card card) {
        INDArray features = Nd4j.create(new float[WhotCard.N_SHAPES], new int[]{1, WhotCard.N_SHAPES});
        features.put(0, card.getShape(), 1);
        return features;
    }

    public static INDArray expandIntoLabelSpace(Card card) {
        INDArray features = Nd4j.create(new float[15], new int[]{1, 15});
        if (card.getLabel() == 20) {
            features.put(0, 14, 1);
        }
        else {
            features.put(0, card.getLabel() - 1, 1);
        }
        return features;
    }

    public static INDArray expandIntoCardSpace(Card... cards) {
        // Features array for the cards the player is holding.
        int shapeSpace = 14 * (WhotCard.N_SHAPES - 1);
        INDArray cardFeatures = Nd4j.create(new float[shapeSpace + 1], new int[]{1, shapeSpace + 1});
        int countWhots = 0;
        for (Card card : cards) {
            if (card.getShape() == WhotCard.WHOT) {
                cardFeatures.put(0, shapeSpace, countWhots);
                countWhots++;
            }
            else {
                cardFeatures.put(0, WhotCardNormalizer.cardSpacePosition(card), 1.);
            }
        }
        // Assert that the number of cards match the number of features.
        assert cardFeatures.sumNumber().intValue() == cards.length;
        return cardFeatures;
    }

    public static INDArray expandIntoCardSpace(String[] fields, int offset) {
        return expandIntoCardSpace(fields, offset, fields.length - offset);
    }

    public static INDArray expandIntoCardSpace(String[] fields, int offset, int length) {
        // Features array for the cards the player is holding.
        int shapeSpace = 14 * (WhotCard.N_SHAPES - 1);
        INDArray cardFeatures = Nd4j.create(new float[shapeSpace + 1], new int[]{1, shapeSpace + 1});
        int countWhots = 0;
        for (int i = 0; i < length; i++) {
            try {
                WhotCard card = WhotCard.fromString(fields[i + offset]);
                if (card.getShape() == WhotCard.WHOT) {
                    cardFeatures.put(0, shapeSpace, countWhots);
                    countWhots++;
                } else {
                    cardFeatures.put(0, WhotCardNormalizer.cardSpacePosition(card), 1.);
                }
            }
            catch (Exception e) {
                // Do nothing.
            }
        }
        // Assert that the number of cards match the number of features.
        assert cardFeatures.sumNumber().intValue() == fields.length - 4;
        return cardFeatures;
    }

    /**
     * A version that uses a cardspace of 54 slots (total number of whot cards) instead of 71.
     */
    public static INDArray expandIntoCompressedCardSpace(Card... cards) {
        INDArray cardFeatures = Nd4j.create(new float[fullCardSet.size()], new int[]{1, fullCardSet.size()});
        for (Card card : cards) {
            cardFeatures.put(0, fullCardSet.indexOf(card), 1 + cardFeatures.getDouble(0, fullCardSet.indexOf(card)));
        }
        return cardFeatures;
    }

    public static INDArray blankWhotCardFeatures(int numberOfLabels) {
        return Nd4j.create(new float[numberOfLabels], new int[]{1, numberOfLabels});
    }

    /**
     * Returns a 1-indexed number representing the position of the card in a field.
     *
     * where: Star 1 - 14     => 1 - 14
     *        Cross 1 - 14    => 15 - 28
     *        Circle 1 - 14   => 29 - 42
     *        Square 1 - 14   => 43 - 56
     *        Triangle 1 - 14 => 57 - 70
     *        Whot 20         => 71
     */
    public static int cardSpacePosition(Card card) {
        if (card.getShape() == WhotCard.WHOT) {
            return 14 * (WhotCard.N_SHAPES - 1);
        }
        else {
            return 14 * card.getShape() + card.getLabel() - 1;
        }
    }

    public static Card cardFromCardPosition(int i) {
        if (i == 70) {
            return new WhotCard(WhotCard.WHOT, 20);
        }
        else {
            int shape = i / 14;
            int number = (i % 14) + 1;
            return new WhotCard(shape, number);
        }
    }

    /**
     * Generates a set of features based on card shape and label.
     *
     * There will be N classes for each type of shape and M classes for each separate card label making a total
     * of N+M features.
     *
     * @return An array of dimension [1 x (N+M)]
     */
    private static INDArray getWhotCardFeatures(WhotCard card) {
        INDArray features = blankWhotCardFeatures(WhotCard.N_SHAPES + 15);
        // Whot 20 label should be in the 15th feature class since 1 - 14 is for the other cards.
        if (card.getShape() == WhotCard.WHOT) {
            features.put(0, WhotCard.N_SHAPES + 14, 1.);
        }
        else {
            features.put(0, WhotCard.N_SHAPES + card.getLabel() - 1, 1.);
        }
        // Shapes in the right positions.
        features.put(0, card.getShape(), 1.);
        return features;
    }

}
