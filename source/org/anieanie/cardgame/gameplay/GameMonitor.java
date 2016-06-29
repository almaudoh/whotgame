package org.anieanie.cardgame.gameplay;

import org.anieanie.card.Card;
import org.anieanie.cardgame.cgmp.ServerCGMPRelay;

/**
 * An interface implemented by all game monitors which act as referees to enforce the rules of a game.
 *
 * Created by almaudoh on 6/4/16.
 */
public interface GameMonitor {

    /**
     * Adds a user to the game gameplay.
     *
     * Users must be added before they can communicate or participate.
     */
    void addUser(String name, ServerCGMPRelay relay);

    /**
     * Checks that the identified user can join to play the game.
     */
    boolean canPlayGame(String user);

    /**
     * Checks that the identified user can join to view the game.
     */
    boolean canViewGame(String user);

    /**
     * Checks that the identified has been listed as a player.
     */
    boolean isPlayer(String user);

    /**
     * Checks that the identified has been listed as a viewer.
     */
    boolean isViewer(String user);

    /**
     * Adds the identified user as a player in the game.
     */
    void addPlayer(String user);

    /**
     * Adds the identified user as a viewer in the game.
     */
    void addViewer(String user);

    /**
     * Checks that the identified can receive a card from the covered stack.
     */
    boolean canHaveCard(String user);

    /**
     * Gives the identified user a card from the covered stack.
     */
    Card[] getCardForUser(String user);

    /**
     * Checks that the identified user can make a move (it is his turn).
     */
    boolean canMakeMove(String user);

    /**
     * Verifies that conditions are okay (e.g. number of players) to start a game.
     */
    boolean requestStartGame();

    /**
     * Requests that the game monitor should start the game at the next scan.
     *
     * @return true if the game can be started at the next scan or if the game is already ongoing.
     */

    /**
     * Attempts to start the game and returns true if successful.
     */
    void startGame();

    /**
     * Checks if the game has been started already.
     *
     * @return true if the game has been started.
     */
    boolean isGameStarted();

    /**
     * @return True if the game has been won by someone.
     */
    boolean isGameWon();

    /**
     * Orchestrate the next move in the game by demanding the players to play according to the rules.
     */
    void nextMove();

    /**
     * Receives a move from the identified user and acknowledges it.
     *
     * @return True if the move is accepted (valid) and false if the move is invalid.
     */
    boolean receiveMoveFromUser(String move, String user);

    /**
     * Handles information received from client via the INFO CGMP command
     */
    void handleInfoReceived(String info, String user);

    /**
     * Gets the current game gameplay.
     *
     * @return GameEnvironment
     */
    GameEnvironment getEnvironment();

}
