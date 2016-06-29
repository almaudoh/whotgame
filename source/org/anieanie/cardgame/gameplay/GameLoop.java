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

package org.anieanie.cardgame.gameplay;

/**
 * A GameLoop refers to a single game and should run on a separate thread
 *
 * @author  aaudoh1
 */
public final class GameLoop extends Thread {

    private GameMonitor monitor;

    /** The game playing loop */
    public GameLoop(GameMonitor monitor) {
        super("GameLoop");
        this.monitor = monitor;
    }

    @Override
    public void run() {

        while (!monitor.isGameStarted()) {
            monitor.startGame();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (!monitor.isGameWon()) {
            monitor.nextMove();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
