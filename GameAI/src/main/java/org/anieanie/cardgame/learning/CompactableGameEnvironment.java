package org.anieanie.cardgame.learning;

import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.training.whot.WhotCardNormalizer;
import org.nd4j.linalg.factory.Nd4j;

import static org.anieanie.cardgame.gameplay.whot.WhotGameRule.*;

/**
 * .
 */
public class CompactableGameEnvironment extends GameEnvironment implements Compactable {
    @Override
    public double[] getVector() {

        String marketMode = get(VAR_MARKET_MODE);

        return Compactable.combineElements(
            // TopCard.
            WhotCardNormalizer.expandIntoShapeLabelSpace(WhotCard.fromString(get(VAR_TOP_CARD))),

            // CalledCard.
            WhotCardNormalizer.expandIntoShapeSpace(WhotCard.fromString(get(VAR_CALLED_CARD) + " 1")),

            // MarketMode.
            new double[]{marketMode.equals("Normal") ? 1 : 0, marketMode.equals("PickTwo") ? 1 : 0, marketMode.equals("General") ? 1 : 0},

            // PlayerCount.
            new double[]{Double.parseDouble(get(VAR_PLAYER_COUNT))}

        );
    }

}
