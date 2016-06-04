package org.anieanie.cardgame;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.ServerCGMPRelay;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Title:        A Complete Whot Playing Environment
 * Description:  A complete Whot playing environment consisting players, spectators and the umpire (or game monitor).~nThe user can play Whot in this program
 * Copyright:    Copyright (c) 1998
 * Company:      KaySoft Intelligent Solutions
 *
 * @author Aniebiet Udoh
 */

public abstract class AbstractGameMonitor implements GameMonitor {
    /**
     * The users participating in this card game.
     */
    protected Hashtable<String, ServerCGMPRelay> users;
    protected ArrayList<String> players;	// the list of all players in the game
    protected ArrayList<String> viewers;	// the list of all those watching the game
    protected boolean gameStarted;	// true if a game is currently going on

    /**
     * The card decks for this game.
     */
    protected CardSet exposed;
    protected CardSet covered;
    protected CardSet dealed;

    protected int stack_nums;


    public AbstractGameMonitor() {
        users = new Hashtable<String, ServerCGMPRelay>();
        players = new ArrayList<String>();
        viewers = new ArrayList<String>();
        initCardDecks();
    }

    protected void initCardDecks() {
        exposed = new CardSet();
        dealed = new CardSet();
        covered = getFullCardSet();
    }

    @Override
    public void addUser(String name, ServerCGMPRelay relay) {
        users.put(name, relay);
    }

    @Override
    public void addPlayer(String identifier) {
        players.add(identifier);
    }

    @Override
    public void addViewer(String identifier) {
        viewers.add(identifier);
    }

    @Override
    public boolean isPlayer(String identifier) {
        return players.contains(identifier);
    }

    @Override
    public boolean isViewer(String identifier) {
        return viewers.contains(identifier);
    }

    @Override
    public boolean canHaveCard(String identifier) {
        return players.contains(identifier);
    }

    @Override
    public boolean canStartGame() {
        return users.size() > 1;
    }

    @Override
    public void startGame() {
        covered.shuffle(20);    // shuffle the cards
        dealCards();        // share the cards to each player
        gameStarted = true;
    }

    protected void dealCards() {    // share the cards to the players
        java.util.Random generator = new java.util.Random(System.currentTimeMillis());
        covered.iterator();    // iterator to be used to share cards

        // Number of cards each player is to get (random b/w 3 and 9 inclusive)
        int num_cards = Math.max(generator.nextInt(7) + 3, covered.size() / (players.size() * 2));
        Card nextCard;
        ServerCGMPRelay relay;
        for (int i = 0; i < num_cards; i++) {
            for (String player : players) {
                nextCard = covered.removeFirst();

                // Remove topmost card and give to next player in line
                // If she doesn't receive it put back it at the bottom of the pack
                relay = users.get(player);
                if (relay.sendCard(nextCard)) {
                    dealed.addFirst(nextCard);
                }
                else {
                    covered.addLast(nextCard);
                }
            }
        }
        // Place the first card that will begin the game and remove it from the reserve.
        exposed.addFirst(covered.removeFirst());
    }

}
