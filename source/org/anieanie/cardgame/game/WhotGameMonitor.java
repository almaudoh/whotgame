// The monitor class is the environment in which a card game is played
// It deals the cards and signifies to each player that it is its turn to play
// The monitor class also provides the interface that allows Watcher objects to watch
// the proceedings
package org.anieanie.cardgame.game;
import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.AbstractGameMonitor;

import java.util.*;
import java.lang.*;

public class WhotGameMonitor extends AbstractGameMonitor {
  // Each new WhotGameMonitor starts a new game
  // and should normally be on a new thread
  // static initializer for random number generator
  private static Random generator = new Random(System.currentTimeMillis());

  // The Game Environment is an inner class that keeps information about
  // the game environment
  //public class Environment {


  // constructors
  public WhotGameMonitor() {
     super();
  }

  protected void initCardDecks() {
    exposed = new WhotCardSet();
    dealed = new CardSet();
    covered = getFullCardSet();
  }

  public CardSet getFullCardSet() {
    return new WhotCardSet();
  }

    @Override
    public boolean canPlayGame(String identifier) {
        // A user cannot join a game that has already started or exceeds 4 players.
        return !gameStarted && players.size() <= 4;
    }

    @Override
    public boolean canViewGame(String identifier) {
        return true;
    }

    @Override
    public Card getCardForUser(String identifier) {
        return covered.removeFirst();
    }

    private void pick2(CardPlayer player) {
//    Iterator reserveIter = reserve.iterator();
//    ((CardPlayer)iter.next()).recieveCard((WhotCard)reserveIter.next());
//    reserveIter.remove();
//    ((CardPlayer)iter.next()).recieveCard((WhotCard)reserveIter.next());
//    reserveIter.remove();
  }

}
