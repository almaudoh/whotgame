package org.anieanie.cardgame.learning.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.learning.Compactable;
import org.anieanie.cardgame.training.whot.WhotCardNormalizer;

/**
 * Creates a version of the WhotCardSet that can be compacted for neural networks.
 */
public class CompactableWhotCardSet extends WhotCardSet implements Compactable {
    @Override
    public double[] getVector() {
        return WhotCardNormalizer.expandIntoCompressedCardSpace(cardlist.toArray(new Card[cardlist.size()]));
    }
}
