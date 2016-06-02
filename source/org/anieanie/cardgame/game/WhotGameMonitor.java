// The monitor class is the environment in which a card game is played
// It deals the cards and signifies to each player that it is its turn to play
// The monitor class also provides the interface that allows Watcher objects to watch
// the proceedings
package org.anieanie.cardgame.game;
import org.anieanie.card.whot.WhotCardSet;

import java.util.*;
import java.lang.*;

public class WhotGameMonitor extends Thread {
  // Each new WhotGameMonitor starts a new game
  // and should normally be on a new thread
  // static initializer for random number generator
  private static Random generator = new Random(System.currentTimeMillis());

  // The Game Environment is an inner class that keeps information about
  // the game environment
  //public class Environment {

  	// private instance fields
    private WhotCardSet vGamepack;// the virtual gamepack: used to ensure cards are not manufactured or changed
    private WhotCardSet reserve;	// the 'market' from which players replenish their supply
    private WhotCardSet used;			// the cards that have been played
    private boolean gameRunning;	// true if a game is currently going on
    private LinkedList players;		// the list of all players in the game
  //}

  // constructors
  public WhotGameMonitor() {
    vGamepack = new WhotCardSet();	// Obtain a brand new card pack (virtual)
    reserve = (WhotCardSet)vGamepack.clone();		// the reserve is the same as the virtual pack at first
    used = new WhotCardSet();			// empty
    players = new LinkedList();		// players list is empty
    start();					// The thread starts on creation
  }

  public void run() {		// this is the thread body
    requestPlayers();		// request for interested players
    reserve.shuffle(20);	// shuffle the cards
    shareCards();		// share the cards to each player
//    playGame();			// play the game
  }

  private void requestPlayers() {
    // the current implementation is simply to add two players
    for (int i=0; i<2; i++) {
//      players.add(new CardPlayer());
    }
  }

  private void shareCards() {	// share the cards to the players
    Iterator reserveIter = reserve.iterator();	// reserve iterator to be used to share cards
    int num_cards = generator.nextInt(7) + 3; 	// number of cards each player is to get (random b/w 3 and 9 inclusive)
    for (int i=0; i<num_cards; i++) {
      for (Iterator iter = players.iterator(); iter.hasNext();) {
//        if (((CardPlayer)iter.next()).recieveCard((WhotCard)reserveIter.next())) {
//          reserveIter.remove();
          // send the card above to the player object then remove it from
          // reserve if successful
//        }
      }
    }

    // place the first card that will begin the game and remove it from the reserve
    used.add(reserveIter.next());
    reserveIter.remove();
  }

  private void pick2(CardPlayer player) {
//    Iterator reserveIter = reserve.iterator();
//    ((CardPlayer)iter.next()).recieveCard((WhotCard)reserveIter.next());
//    reserveIter.remove();
//    ((CardPlayer)iter.next()).recieveCard((WhotCard)reserveIter.next());
//    reserveIter.remove();
  }
}

