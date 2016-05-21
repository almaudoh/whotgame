// this class is a specific subclass of the CardSet superclass and represents
// a complete set of WhotCards containing all shapes and all numbers for each shape
package org.anieanie.whot;

import java.util.*;
import org.anieanie.cardgame.*;

public class WhotCardSet extends CardSet {
    // this class contains just a default constructor used to create
    // a new sorted pack of 54 WhotCards. Note that the constants used here are defined
    // in the org.anieanie.Whot.WhotCard class
    
    // Constructor
    public WhotCardSet() {
        for (int shape = WhotCard.STAR; shape < WhotCard.WHOT; shape++) {
            for (int label = WhotCard.L_LIMIT; label <= WhotCard.U_LIMIT; label++) {
                if (WhotCard.isIllegal(shape, label))
                    continue;
                else add(new WhotCard(shape, label));
                // add a new WhotCard object to the WhotCardSet if the shape and label
                // are legal
            }
        }
        
        //    WhotCard.reset_whots(); // this is necessary to allow 5 identical Whot 20's to
        //            // to be inserted into the CardSet (which doesn't allow duplicate entries)
        for (int i = 0; i < WhotCard.N_WHOT; i++) {
            // add the whots themselves (ie. the jokers)
            add(new WhotCard(WhotCard.WHOT, 20));
        }
        
    }
 
    // private constructor for clone method
    protected WhotCardSet(LinkedList list) {
        cardlist = list;
    }
    
     // public instance methods
    public boolean add(Card card) {
        if (cardlist.contains(card)) {
            if (card.getShape() == WhotCard.WHOT && countWhots() < 5) {
                return cardlist.add(card);
            }
            else
                throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
            //return false;
        } else {
            return cardlist.add(card);
        }
    }
    
    public void add(int index, Card card) {
        if (cardlist.contains(card)) {
            if (card.getShape() == WhotCard.WHOT && countWhots() < 5) {
                cardlist.add(index, card);
            }
            else
                throw new RuntimeException("Attempt to insert duplicate entries in Set object");
        } else {
            cardlist.add(index, card);
        }
    }
    
    public void addFirst(Card card) {
        if (cardlist.contains(card)) {
            if (card.getShape() == WhotCard.WHOT && countWhots() < 5) {
                cardlist.addFirst(card);
            }
            else
                throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
        } else {
            cardlist.addFirst(card);
        }
    }
    
    public void addLast(Card card) {
        if (cardlist.contains(card)) {
            if (card.getShape() == WhotCard.WHOT && countWhots() < 5) {
                cardlist.addLast(card);
            }
            else
                throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
        } else {
            cardlist.addLast(card);
        }
    }
    
    private int countWhots() {
        int numwhots = 0;
        for (Iterator it = cardlist.iterator(); it.hasNext();) {
            if (((Card)it.next()).getShape() == WhotCard.WHOT) numwhots++;
        }
        return numwhots;
    }
    
    public Object clone() {
        Object element;
        LinkedList list = new LinkedList();
        for (Iterator i = cardlist.iterator(); i.hasNext();) {
            element = ((WhotCard)i.next()).clone();
            if (list.contains(element)) {
                if (((Card)element).getShape() == WhotCard.WHOT) {
                    
                    int numwhots = 0;
                    for (Iterator it = list.iterator(); it.hasNext();) {
                        if (((Card)it.next()).getShape() == WhotCard.WHOT) numwhots++;
                    }
                    if (numwhots < 5) {
                        list.add(element);
                    }
                    else
                        throw new RuntimeException("Cannot clone CardSet, duplicate entries found.");
                }
                else
                    throw new RuntimeException("Cannot clone CardSet, duplicate entries found.");
            } else {
                list.add(element);
            }
        }
        return new WhotCardSet(list);
    }   
}