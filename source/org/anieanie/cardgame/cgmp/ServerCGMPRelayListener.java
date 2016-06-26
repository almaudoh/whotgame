/*
 * ServerCGMPRelayListener.java
 *
 * Created on May 5, 2005, 8:13 PM
 */

package org.anieanie.cardgame.cgmp;

/**
 *
 * @author  ALMAUDOH
 */
public interface ServerCGMPRelayListener extends CGMPRelayListener {
    
    /** Called when worker CGMPRelay receives request to play from client CGMPRelay */
    void playRequested();

    /** Called when worker CGMPRelay receives request to watch from client CGMPRelay */
    void viewRequested();

    /** Called when worker CGMPRelay receives request for environment from client CGMPRelay */
    void envRequested();

    /** Called when worker CGMPRelay receives request for card from client CGMPRelay */
    void cardRequested();

    /** Called when the worker CGMPRelay receives a move from the client CGMPRelay */
    void moveReceived(String move);

    /**
     * Called when worker CGMPRelay receives a connection request from client CGMPRelay
     *
     * @param identifier The name of the client relay.
     */
    void clientConnected(String identifier);

    void gameStartRequested();
}
