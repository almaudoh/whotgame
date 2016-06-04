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

import org.anieanie.card.CardSet;

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
    
    /** Initializes the GameEnvironment variables */
    public GameEnvironment() {
    }
    
    /** Initializes the GameEnvironment variables */
    public GameEnvironment(String gameClass) {
    }

}
