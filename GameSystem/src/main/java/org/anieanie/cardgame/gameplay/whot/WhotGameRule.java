package org.anieanie.cardgame.gameplay.whot;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.GameRule;

/**
 * Provides guidance on game rules.
 */
public class WhotGameRule implements GameRule {

    public static final String VAR_TOP_CARD = "TopCard";
    public static final String VAR_CALLED_CARD = "CalledCard";
    public static final String VAR_MARKET_MODE = "MarketMode";
    public static final String MARKET_MODE_GENERAL = "General";
    public static final String MARKET_MODE_PICK_TWO = "PickTwo";
    public static final String MARKET_MODE_NORMAL = "Normal";
    public static final String VAR_GENERAL_MARKET_PLAYER = "GeneralMarketPlayer";

    // The number that is used as the pick two number.
    public static final int PICK_TWO_LABEL = 7;

    // The number that is used as the general market number.
    public static final int GENERAL_MARKET_LABEL = 4;

    // The number that is used for suspension.
    public static final int SUSPENSION_LABEL = 8;

    // The number that is used as hold on.
    public static final int HOLD_ON_LABEL = 1;

    public static final String GO_MARKET = "MARKET";

    public static CardSet filterValidMoves(CardSet hand, GameEnvironment env) {
        WhotCard topCard = WhotCard.fromString(env.get(VAR_TOP_CARD));
        if (topCard != null) {
            // Different playing compulsions and scenarios.
            if (env.get(VAR_MARKET_MODE).equals(MARKET_MODE_PICK_TWO)) {
                if (hand.containsLabel(PICK_TWO_LABEL)) {
                    return hand.containingLabel(PICK_TWO_LABEL);
                }
                else {
                    // Have to go market.
                    return new CardSet();
                }
            }
            else if (env.get(VAR_MARKET_MODE).equals(MARKET_MODE_GENERAL)) {
                // For general market, nothing can be played.
                return new CardSet();
            }
            else if (topCard.getShape() == WhotCard.WHOT) {
                // If whot 20 is played, then look at the called card and return the match.
                return hand.containingShape(WhotCard.getShapeInt(env.get(VAR_CALLED_CARD)));
            }
            else {
                // Otherwise, choose all cards that match the top card either shape or label.
                CardSet cards = hand.containingShape(topCard.getShape());
                cards.addAll(hand.containingLabel(topCard.getLabel()));
                cards.addAll(hand.containingShape(WhotCard.WHOT));
                return cards;
            }
        }
        // Nothing will match an empty TOP_CARD.
        throw new IllegalArgumentException(VAR_TOP_CARD + " not specified");
    }

    @Override
    public boolean isValidMove(Card move, GameEnvironment env) {
        return matchesTopCard(move, env) && isPickTwoCounter(move, env)
                && (!env.get(VAR_MARKET_MODE).equals(MARKET_MODE_GENERAL)
                || env.get(GameEnvironment.VAR_CURRENT_PLAYER).equals(env.get(WhotGameRule.VAR_GENERAL_MARKET_PLAYER)));
    }

    private boolean matchesTopCard(Card move, GameEnvironment env) {
        Card top = WhotCard.fromString(env.get(VAR_TOP_CARD));
        return move.getLabel() == top.getLabel() || move.getShape() == top.getShape() ||
                move.getShape() == WhotCard.WHOT || WhotCard.SHAPES.get(move.getShape()).equals(env.get(VAR_CALLED_CARD));
    }

    private boolean isPickTwoCounter(Card move, GameEnvironment env) {
        return !env.get(VAR_MARKET_MODE).equals(MARKET_MODE_PICK_TWO) || move.getLabel() == PICK_TWO_LABEL;
    }

}
