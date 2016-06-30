package org.anieanie.cardgame.gameplay;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.CGMPException;

import java.io.IOException;

/**
 * Common features shared by all whot clients.
 */
public interface GameClient {

    int STATUS_UNDEFINED = -1;
    int STATUS_WAITING_TO_START = 0;
    int STATUS_WAITING_FOR_TURN = 1;
    int STATUS_WAITING_FOR_USER = 2;
    int STATUS_GAME_WON = 3;
    int STATUS_TERMINATE = 10;

    int getClientStatus();

    GameEnvironment getEnvironment();

    CardSet getCards();

    void startGame();


    void refreshClientStatus();

    /** Plays the specified move */
    void playMove(String cardSpec);

    void playMove(Card card);

    void connect() throws CGMPException, IOException;

    void close() throws CGMPException, IOException;
}
