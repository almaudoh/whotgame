/*
 * GameEnvironment.java
 *
 * Any card game can be simplified as a number of card stacks, some facing up, some facing
 * down and the rest distributed to the player(s). Then the game rules simply specify how
 * the cards in each stack may or should be moved round (removed or replaced).
 * Thus, as far as the cards are considered, the game environment consists of three groups
 * of CardSets: exposed, covered and dealed. Each of these groups (which can contain zero or
 * more CardSet objects) could be represented by a Hashset or Hashtable (though internals
 * may differ) with appropriate objects (preferably Strings) used as the hash or key.
 *
 * Created on February 23, 2005, 11:31 AM
 */

package org.anieanie.cardgame.environment;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.Player;
import org.anieanie.cardgame.Watcher;

import java.io.*;
import java.util.*;

/**
 * A GameEnvironment refers to a single game and should run on a separate thread
 *
 * @author  aaudoh1
 */
public final class GameEnvironment extends Thread implements java.io.Serializable, java.lang.Cloneable {
    
    //    private CardSet fullCardSet;
    //    private static Random generator = new Random(System.currentTimeMillis());
    
    private boolean playing;	// true if a game is currently going on
    private GameMonitor gMon;   // The game monitor who has locked this Environment
    private Hashtable players;	// the list of all players in the game
    private Hashtable watchers;	// the list of all those watching the game
    private String __gameClass = "org.anieanie.org.anieanie.card.whot.Whot";

    // The card decks in this game and the additional information on them.
    protected HashMap<String, CardSet> cardDecks;

    // The deckInfo provides information on whether card decks can be view, changed
    // (added / subtracted from), shuffled, sorted, etc.
    protected HashMap<String, DeckInfo> deckInfo;
    
    /**
     * The card decks for this game.
     */
    private CardSet exposed;
    private CardSet covered;
    private CardSet dealed;
    
    private int stack_nums;
    
    /** Initializes the GameEnvironment variables */
    public GameEnvironment() {
        players = new Hashtable();     // players list is empty
        watchers = new Hashtable();    // watchers of the game
        gMon = newGameMonitor(__gameClass);
    }
    
    /** Initializes the GameEnvironment variables */
    public GameEnvironment(String gameClass) {
        players = new Hashtable();     // players list is empty
        watchers = new Hashtable();    // watchers of the game
        gMon = newGameMonitor(gameClass);
    }

    public void addDeck(String name, CardSet cards, DeckInfo info) {
        if (cardDecks.containsKey(name)) {
            throw new RuntimeException("Card deck with name '" + name + "' already exists.");
        }
        cardDecks.put(name, cards);
        deckInfo.put(name, info);
    }

    public CardSet removeDeck(String name) {
        deckInfo.remove(name);
        return cardDecks.remove(name);
    }
    
    /*
     * Ensure no duplicates in players and watchers
     */
    public void addPlayer(Player player) {
        players.put(player.getName(), player);
    }
    
    /*
     * Ensure no duplicates in players and watchers
     */
    public void addWatcher(Watcher watcher) {
        watchers.put(watcher.getName(), watcher);
    }
    
    public boolean startGame() {
        if (players.size() > 1 && !playing) {
            playing = true;
            start();
        }
        return false;
    }
    
    public Cardspace getCardspace() {
        return new Cardspace();
    }
    
    public GameMonitor getGameMonitor() {
        return gMon;
    }
    
    private GameMonitor newGameMonitor(String gameClass) {
        try {
            return (GameMonitor)Class.forName(gameClass+"GameMonitor").newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            System.out.println("Could not find the specified class: "+gameClass+"GameMonitor");
        }
        catch (IllegalAccessException iae) {
            System.out.println("Could not access the specified class: "+gameClass+"GameMonitor");
        }
        catch (InstantiationException ie) {
            System.out.println("Could not instantiate the specified class: "+gameClass+"GameMonitor");
        }
        catch (ClassCastException cce) {
            System.out.println("Specified class: "+gameClass+"GameMonitor does not extend org.anieanie.cardgame.environment.GameEnvironment.GameMonitor");
        }
        finally {
            return null;
        }
    }
    
    public void start() {
        super.start();
        gMon.initEnvironment();
    }
    
    
    public void run() {		// this is the thread body
        gMon.Go();
    }
    
    /**
     * GameEnvironment.Cardspace inner class
     */
    
    public final class Cardspace implements Serializable, Cloneable {
        Hashtable cardstack;
        private Cardspace() {
            cardstack = new Hashtable(exposed.size());
            //            for (Enumeration en = exposed.elements(); en.hasMoreElements(); ) {
            //                cardstack.put(((CardSet)en.next()).clone());
            //            }
        }
        
        //        public Cardspace fromString(String string) {
        //
        //        }
        
    }
    
    public abstract class GameMonitor {
        //        private void requestPlayers() {
        //            // the current implementation is simply to add two players
        //            for (int i=0; i<2; i++) {
        //                //      players.add(new CardPlayer());
        //            }
        //        }
        /**
         * Abstract methods to be implemented by all subclasses
         */
        public abstract CardSet getFullCardSet();
        
        private void initEnvironment() {
            exposed = new CardSet();
            dealed = new CardSet();
            covered = getFullCardSet();
            
            covered.shuffle(20);	// shuffle the cards
            dealCards();		// share the cards to each player
        }
        
        private void Go() {
            //playGame();			// play the game
        }
        
        private void dealCards() {	// share the cards to the players
            java.util.Random generator = new java.util.Random(System.currentTimeMillis());
            covered.iterator();	// iterator to be used to share cards
            
            /**
             * @todo We'll need to later check that cards will be sufficient here
             */
            int num_cards = generator.nextInt(7) + 3; 	// number of cards each player is to get (random b/w 3 and 9 inclusive)
            Card nextCard;
            for (int i=0; i<num_cards; i++) {
                for (Enumeration en = players.elements(); en.hasMoreElements();) {
                    nextCard = covered.removeFirst();
                    /*
                     * Remove topmost card and give to next player in line
                     * If s/he doesn't receive it put back it at the bottom of the pack
                     */
                    if (((Player)en.nextElement()).receiveCard(nextCard)) {
                        dealed.addFirst(nextCard);
                    } else
                        covered.addLast(nextCard);
                }
            }
            // place the first card that will begin the game and remove it from the reserve
            exposed.addFirst(covered.removeFirst());
        }
    }
}