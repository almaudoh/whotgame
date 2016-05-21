package org.anieanie.suit;

import java.util.*;

public class SuitCard {
    public enum Rank { DEUCE, THREE, FOUR, FIVE, SIX,
        SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }

    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    private final Rank rank;
    private final Suit suit;
    private SuitCard(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank rank() { return rank; }
    public Suit suit() { return suit; }
    public String toString() { return rank + " of " + suit; }

    private static final List<SuitCard> protoDeck = new ArrayList<SuitCard>();

    // Initialize prototype deck
    static {
        for (Suit suit : Suit.values())
            for (Rank rank : Rank.values())
                protoDeck.add(new SuitCard(rank, suit));
    }

    public static ArrayList<SuitCard> newDeck() {
        return new ArrayList<SuitCard>(protoDeck); // Return copy of prototype deck
    }
}
