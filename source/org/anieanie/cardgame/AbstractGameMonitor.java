package org.anieanie.cardgame;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.CGMPException;
import org.anieanie.cardgame.cgmp.ServerCGMPRelay;

import java.io.IOException;
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

    // The list of all players in the game
    protected ArrayList<String> players;

    // The list of all those watching the game
    protected ArrayList<String> viewers;

    // Flag to mark that the game has already started.
    protected boolean gameStarted = false;

    // Flag to mark that the game should be started at the next scan.
    protected boolean gameStartRequested = false;

    /**
     * The card decks for this game.
     */
    protected CardSet exposed;
    protected CardSet covered;
    protected CardSet dealed;

    protected boolean gameWon;

    protected int currentPlayer = 0;


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
    public void addPlayer(String user) {
        players.add(user);
    }

    @Override
    public void addViewer(String user) {
        viewers.add(user);
    }

    @Override
    public boolean isPlayer(String user) {
        return players.contains(user);
    }

    @Override
    public boolean isViewer(String user) {
        return viewers.contains(user);
    }

    @Override
    public boolean canHaveCard(String user) {
        return players.get(currentPlayer).equals(user);
    }

    @Override
    public boolean requestStartGame() {
        if (users.size() > 1) {
            gameStartRequested = true;
            return true;
        }
        else {
            gameStartRequested = false;
            return false;
        }
    }

    @Override
    public void startGame() {
        // Only start if someone has requested game to start.
        if (gameStartRequested && !gameStarted) {
            covered.initialize();
            covered.shuffle(20);    // shuffle the cards
            dealCards();        // share the cards to each player
            gameStarted = true;
            gameStartRequested = false;
        }
    }

    @Override
    public boolean isGameStarted() {
        return gameStarted;
    }

    @Override
    public boolean isGameWon() {
        return gameWon;
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
                if (relay.sendCard(new Card[] {nextCard})) {
                    dealed.addFirst(nextCard);
                }
                else {
                    covered.addLast(nextCard);
                }
            }
        }
        // Place the first card that will begin the game and remove it from the reserve.
        exposed.addFirst(covered.removeFirst());

        // Send top card to all players and watchers.
        broadcastEnvironment();
    }

    /** Broadcasts the current environment to all users in the game */
    protected void broadcastEnvironment() {
        try {
            for (ServerCGMPRelay user : users.values()) {
                user.sendEnvironment(getEnvironment());
            }
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void advanceGameTurn() {
        // Increment the player position and broadcast position.
        currentPlayer = ++currentPlayer % players.size();
    }

    public String getEnvironment() {
        return String.format("CurrentPlayer: %s; TopCard: %s;", exposed.getFirst(), players.get(currentPlayer));
    }
}
