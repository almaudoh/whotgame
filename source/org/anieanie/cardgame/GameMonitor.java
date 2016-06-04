package org.anieanie.cardgame;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.ServerCGMPRelay;

/**
 * An interface implemented by all game monitors which act as referees to enforce the rules of a game.
 *
 * Created by almaudoh on 6/4/16.
 */
public interface GameMonitor {

    /**
     * Gets a full card set for this game type.
     */
    CardSet getFullCardSet();

    /**
     * Adds a user to the game environment.
     *
     * Users must be added before they can communicate or participate.
     */
    void addUser(String name, ServerCGMPRelay relay);

    /**
     * Checks that the identified user can join to play the game.
     */
    boolean canPlayGame(String identifier);

    /**
     * Checks that the identified user can join to view the game.
     */
    boolean canViewGame(String identifier);

    /**
     * Checks that the identified has been listed as a player.
     */
    boolean isPlayer(String identifier);

    /**
     * Checks that the identified has been listed as a viewer.
     */
    boolean isViewer(String identifier);

    /**
     * Adds the identified user as a player in the game.
     */
    void addPlayer(String identifier);

    /**
     * Adds the identified user as a viewer in the game.
     */
    void addViewer(String identifier);

    /**
     * Checks that the identified can receive a card from the covered stack.
     */
    boolean canHaveCard(String identifier);

    /**
     * Gives the identified user a card from the covered stack.
     */
    Card getCardForUser(String identifier);

    /**
     * Verifies that conditions are okay (e.g. number of players) to start a game.
     *
     * @return
     */
    boolean canStartGame();

    /**
     * Attempts to start the game and returns true if successful.
     */
    void startGame();
}
