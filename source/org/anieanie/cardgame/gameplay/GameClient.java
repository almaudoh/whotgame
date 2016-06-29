package org.anieanie.cardgame.gameplay;

import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.CGMPException;

import java.io.IOException;

/**
 * Common features shared by all whot clients.
 */
public interface GameClient {

    int getClientStatus();

    GameEnvironment getEnvironment();

    CardSet getCards();

    void startGame();


    void refreshClientStatus();

    /** Plays the specified move */
    void playMove(String cardSpec);

    void connect() throws CGMPException, IOException;

    void close() throws CGMPException, IOException;
}
