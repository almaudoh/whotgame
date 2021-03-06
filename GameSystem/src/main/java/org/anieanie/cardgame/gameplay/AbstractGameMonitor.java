package org.anieanie.cardgame.gameplay;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.CGMPException;
import org.anieanie.cardgame.cgmp.ServerCGMPRelay;
import org.anieanie.cardgame.gameplay.whot.WhotGameRule;

import java.io.IOException;
import java.util.*;

/**
 * Title:        A Complete Whot Playing Environment
 * Description:  A complete Whot playing gameplay consisting players, spectators and the umpire (or whot monitor).~nThe user can play Whot in this program
 * Copyright:    Copyright (c) 1998
 * Company:      KaySoft Intelligent Solutions
 *
 * @author Aniebiet Udoh
 */

public abstract class AbstractGameMonitor implements GameMonitor {
    // Contains information on the whot gameplay.
    protected final GameEnvironment environment;

    /**
     * The users participating in this card whot.
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
     * The card decks for this whot.
     */
    protected CardSet exposed;
    protected CardSet covered;
    protected CardSet dealed;

    protected boolean gameWon;
    protected int gameWinner;
    protected int currentPlayer = 0;

    // The number of cards held by each player.
    protected HashMap<String, Integer> playerCardCount;


    public AbstractGameMonitor() {
        users = new Hashtable<String, ServerCGMPRelay>();
        players = new ArrayList<String>();
        viewers = new ArrayList<String>();
        environment = new GameEnvironment();
        playerCardCount = new HashMap<>();
        initCardDecks();
    }

    protected void initCardDecks() {
        exposed = new CardSet();
        dealed = new CardSet();
        covered = new CardSet();
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
        if (players.size() > 1 && !gameStarted) {
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
        // Only start if someone has requested whot to start.
        if (gameStartRequested && !gameStarted) {
            covered.initialize();
            // shuffle the cards
            covered.shuffle(20);
            // share the cards to each player
            dealCards();
            gameStarted = true;
            gameStartRequested = false;
            // Send whot gameplay to all users.
            broadcastInformation(String.format("Cards dealt to %s players, game started", players.size()));
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
        java.util.Random random = new java.util.Random(System.currentTimeMillis());

        // Number of cards each player is to get (random b/w 3 and 9 inclusive)
        int num_cards = Math.min(random.nextInt(7) + 3, covered.size() / (players.size() * 2));
        ServerCGMPRelay relay;
        Card[] cards;
        for (String player: players) {
            playerCardCount.put(player, num_cards);
            cards = new Card[num_cards];

            // Remove random cards and give to this user.
            for (int i = 0; i < num_cards; i++) {
                cards[i] = covered.remove(i * players.size());
            }
            relay = users.get(player);
            // @todo: What to do if the cards are missed.
            if (relay.sendCard(cards)) {
                dealed.addAll(Arrays.asList(cards));
            } else {
                // @todo: Potential bug here.
                covered.addAll(Arrays.asList(cards));
            }
        }
        // Place the first card that will begin the game and remove it from the reserve.
        exposed.addFirst(covered.removeFirst());
    }

    /** Broadcasts the current gameplay to all users in the game */
    protected void broadcastEnvironment() {
        try {
            updateEnvironment();
            for (ServerCGMPRelay user : users.values()) {
                user.sendEnvironment(environment);
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
        // Increment the player position.
        currentPlayer = ++currentPlayer % players.size();
    }

    protected void updateEnvironment() {
        if (players.size() > 0 && currentPlayer > -1) {
            environment.put(GameEnvironment.VAR_CURRENT_PLAYER, players.get(currentPlayer));
        }
        environment.put(GameEnvironment.VAR_PLAYERS, players.toString());
        environment.put(GameEnvironment.VAR_VIEWERS, viewers.toString());
        environment.put(GameEnvironment.VAR_PLAYER_COUNT, String.valueOf(players.size()));
    }

    @Override
    public GameEnvironment getEnvironment() {
        updateEnvironment();
        return environment;
    }

    /** Broadcasts to all users in the game that the current has been won */
    protected void broadcastGameWon() {
        for (ServerCGMPRelay user : users.values()) {
            user.sendGameWon(players.get(gameWinner));
        }
    }

    protected void broadcastInformation(String info) {
        for (ServerCGMPRelay user : users.values()) {
            try {
                user.sendInformation(info);
            } catch (CGMPException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
