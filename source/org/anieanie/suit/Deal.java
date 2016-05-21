package org.anieanie.suit;

import java.util.*;

public class Deal {
    public static void main(String args[]) {
        int numHands = Integer.parseInt(args[0]);
        int cardsPerHand = Integer.parseInt(args[1]);
        List<SuitCard> deck  = SuitCard.newDeck();
        Collections.shuffle(deck);
        for (int i=0; i < numHands; i++)
            System.out.println(deal(deck, cardsPerHand));
    }

    public static ArrayList<SuitCard> deal(List<SuitCard> deck, int n) {
         int deckSize = deck.size();
         List<SuitCard> handView = deck.subList(deckSize-n, deckSize);
         ArrayList<SuitCard> hand = new ArrayList<SuitCard>(handView);
         handView.clear();
         return hand;
     }
}

/*  
 *   $ java Deal 4 5
 *   [FOUR of HEARTS, NINE of DIAMONDS, QUEEN of SPADES, ACE of SPADES, NINE of SPADES]
 *   [DEUCE of HEARTS, EIGHT of SPADES, JACK of DIAMONDS, TEN of CLUBS, SEVEN of SPADES]
 *   [FIVE of HEARTS, FOUR of DIAMONDS, SIX of DIAMONDS, NINE of CLUBS, JACK of CLUBS]
 *   [SEVEN of HEARTS, SIX of CLUBS, DEUCE of DIAMONDS, THREE of SPADES, EIGHT of CLUBS]
 */