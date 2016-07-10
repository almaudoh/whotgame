// this class is a specific subclass of the CardSet superclass and represents
// a complete set of WhotCards containing all shapes and all numbers for each shape
package org.anieanie.card.whot;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;

import java.util.LinkedList;

public class WhotCardSet extends CardSet {
    // this class contains just a default constructor used to create
    // a new sorted pack of 54 WhotCards. Note that the constants used here are defined
    // in the org.anieanie.Whot.WhotCard class

    // Constructor
    public WhotCardSet() {}

    private WhotCardSet(LinkedList<Card> list) {
        cardlist = list;
    }

    public void initialize() {
        for (int shape = WhotCard.STAR; shape < WhotCard.WHOT; shape++) {
            for (int label = WhotCard.L_LIMIT; label <= WhotCard.U_LIMIT; label++) {
                if (!WhotCard.isIllegal(shape, label)) {
                    // add a new WhotCard object to the WhotCardSet if the shape and label
                    // are legal
                    add(new WhotCard(shape, label));
                }
            }
        }
        for (int i = 0; i < WhotCard.N_WHOT; i++) {
            // add the whots themselves (ie. the jokers)
            add(new WhotCard(WhotCard.WHOT, 20));
        }
    }

    @Override
    protected CardSet newInstance(LinkedList<Card> list) {
        return new WhotCardSet(list);
    }

    public boolean isDuplicate(Card card) {
        // WhotCards are not considered as duplicate unless they are more than 5.
        return cardlist.contains(card) && (card.getShape() != WhotCard.WHOT || countWhots() >= 5);
    }

    @SuppressWarnings("unchecked")
    public CardSet clone() {
        return newInstance((LinkedList<Card>)this.cardlist.clone());
    }

    private int countWhots() {
        int numwhots = 0;
        for (Card card : cardlist) {
            if ((card).getShape() == WhotCard.WHOT) numwhots++;
        }
        return numwhots;
    }

}
