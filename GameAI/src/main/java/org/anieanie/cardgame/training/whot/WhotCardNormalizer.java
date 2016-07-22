package org.anieanie.cardgame.training.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;

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

    public static double[] expandIntoShapeSpace(Card card) {
        double[] features = new double[WhotCard.N_SHAPES];
        features[card.getShape()] = 1;
        return features;
    }

    public static double[] expandIntoLabelSpace(Card card) {
        double[] features = new double[15];
        if (card.getLabel() == 20) {
            features[14] = 1;
        }
        else {
            features[card.getLabel() - 1] = 1;
        }
        return features;
    }

    /**
     * Generates a set of features based on card shape and label.
     *
     * There will be N classes for each type of shape and M classes for each separate card label making a total
     * of N+M features.
     *
     * @return An array of dimension [1 x (N+M)]
     */
    public static double[] expandIntoShapeLabelSpace(Card card) {
        double[] features = new double[WhotCard.N_SHAPES + 15];
        // Shapes in the right positions.
        features[card.getShape()] = 1.;
        // Whot 20 label should be in the 15th feature class since 1 - 14 is for the other cards.
        if (card.getShape() == WhotCard.WHOT) {
            features[WhotCard.N_SHAPES + 14] = 1.;
        }
        else {
            features[WhotCard.N_SHAPES + card.getLabel() - 1] = 1.;
        }
        return features;
    }

    /**
     * Generates a set of features for each card supplied and combines them into one
     */
    public static double[] expandIntoShapeLabelSpace(Card... cards) {
        double[] buffer = new double[cards.length * (WhotCard.N_SHAPES + 15)];
        int i = 0;
        for (Card card : cards) {
            for (double value : expandIntoShapeLabelSpace(card)) {
                buffer[i] = value;
                i++;
            }
        }
        return buffer;
    }

    /**
     * Generates a set of features of a fixed size corresponding to the space that would be taken by the specified
     * number of cards.
     * if cards.length > fixedSize, then excess cards are truncated but if cards.length < fixedSize, zeroes are filled
     * at the end.
     */
    public static double[] expandIntoShapeLabelSpace(int fixedSize, Card... cards) {
        double[] buffer = new double[fixedSize * (WhotCard.N_SHAPES + 15)];
        int i = 0, cardCount = 0;
        for (Card card : cards) {
            for (double value : expandIntoShapeLabelSpace(card)) {
                buffer[i] = value;
                i++;
            }
            if (++cardCount > fixedSize) break;
        }
        return buffer;
    }

    public static double[] expandIntoCardSpace(Card... cards) {
        // Features array for the cards the player is holding.
        int shapeSpace = 14 * (WhotCard.N_SHAPES - 1);
        double[] cardFeatures = new double[shapeSpace + 1];
        int countWhots = 0;
        for (Card card : cards) {
            if (card.getShape() == WhotCard.WHOT) {
                cardFeatures[shapeSpace] = countWhots;
                countWhots++;
            }
            else {
                cardFeatures[cardSpacePosition(card)] = 1.;
            }
        }
        // Assert that the number of cards match the number of features.
        assertNonZeroCount(cardFeatures, cards.length);
        return cardFeatures;
    }

    public static double[] expandIntoCardSpace(String[] cards, int offset) {
        return expandIntoCardSpace(cards, offset, cards.length - offset);
    }

    public static double[] expandIntoCardSpace(String[] cards, int offset, int length) {
        // Features array for the cards the player is holding.
        int shapeSpace = 14 * (WhotCard.N_SHAPES - 1);
        double[] cardFeatures = new double[shapeSpace + 1];
        int countWhots = 0;
        for (int i = 0; i < length; i++) {
            try {
                WhotCard card = WhotCard.fromString(cards[i + offset]);
                if (card.getShape() == WhotCard.WHOT) {
                    cardFeatures[shapeSpace] = countWhots;
                    countWhots++;
                } else {
                    cardFeatures[cardSpacePosition(card)] = 1.;
                }
            }
            catch (Exception e) {
                // Do nothing.
            }
        }
        // Assert that the number of cards match the number of features.
        assertNonZeroCount(cardFeatures, cards.length - 4);
        return cardFeatures;
    }

    /**
     * A version that uses a cardspace of 54 slots (total number of whot cards) instead of 71.
     */
    public static double[] expandIntoCompressedCardSpace(Card... cards) {
        double[] cardFeatures = new double[fullCardSet.size()];
        for (Card card : cards) {
            cardFeatures[fullCardSet.indexOf(card)] += 1;
        }
        return cardFeatures;
    }

    public static double[] blankWhotCardFeatures(int numberOfLabels) {
        return new double[numberOfLabels];
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

    private static void assertNonZeroCount(double[] cardFeatures, int length) {
        int nonZeroCount = 0;
        for (double item : cardFeatures) {
            if (item != 0) nonZeroCount++;
        }
        if (nonZeroCount != length) {
            throw new AssertionError("Non-zero elements in " + Arrays.toString(cardFeatures) + " is different from " + length);
        }
    }

}
