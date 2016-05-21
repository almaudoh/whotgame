/*
 * Player.java
 *
 * Created on May 19, 2005, 1:42 AM
 */

package org.anieanie.cardgame;

/**
 * This interface is to be implemented by all classes to take part in a game. This includes
 * user interface elements that allow games to be played and artificial intelligence
 * classes that will take part
 *
 * @author  ALMAUDOH
 */
public interface Player extends Watcher {
    public Card play(Object env);
    public boolean receiveCard(Card card);
}
