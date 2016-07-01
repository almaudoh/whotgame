package org.anieanie.cardgame.ui;

import org.anieanie.cardgame.gameplay.GameClient;

/**
 * Holds information and methods for the command line display service.
 */
public interface Display {

    void showNotification(String message);

    void showGameStatus(GameClient client);

}
