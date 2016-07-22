package org.anieanie.cardgame.dl.normalization;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.dl.normalization.Compactable;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.training.whot.WhotCardNormalizer;

import static org.anieanie.cardgame.gameplay.GameEnvironment.VAR_PLAYER_COUNT;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.VAR_CALLED_CARD;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.VAR_MARKET_MODE;
import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.VAR_TOP_CARD;

/**
 * Helper class to provide an easily reproducible compactable cardset of fixed size.
 */
public class CompactableUtility {

    public static Compactable fromFixedSizeCardSet(final CardSet cards, final int size) {
        return new Compactable() {
            @Override
            public double[] getVector() {
                return WhotCardNormalizer.expandIntoShapeLabelSpace(size, cards.toArray(new Card[cards.size()]));
            }
        };
    }

    public static Compactable fromGameEnvironment(final GameEnvironment environment) {
        return new Compactable() {
            @Override
            public double[] getVector() {
                // For the third state condition (CalledCard) we only add the value for the shape.
                double[] calledCard;
                try {
                    calledCard = WhotCardNormalizer.expandIntoShapeSpace(WhotCard.fromString(environment.get(VAR_CALLED_CARD) + " 1"));
                }
                catch (IllegalArgumentException e) {
                    calledCard = new double[WhotCard.N_SHAPES];
                }
                String marketMode = environment.get(VAR_MARKET_MODE);
                double[] mode = new double[]{marketMode.equals("Normal") ? 1 : 0, marketMode.equals("PickTwo") ? 1 : 0, marketMode.equals("General") ? 1 : 0};
                double[] playerCount = new double[]{Double.parseDouble(environment.get(VAR_PLAYER_COUNT))};

                return Compactable.combineElements(calledCard, mode, playerCount);
            }
        };
    }

    public static Compactable fromWhotCardSet(CardSet cards) {
        return new Compactable() {
            @Override
            public double[] getVector() {
                return WhotCardNormalizer.expandIntoCompressedCardSpace(cards.toArray(new Card[cards.size()]));
            }
        };
    }

    public static Compactable fromWhotCards(Card... card) {
        return new Compactable() {
            @Override
            public double[] getVector() {
                return WhotCardNormalizer.expandIntoShapeLabelSpace(card);
            }
        };
    }

}
