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
    public boolean playRequested();

    /** Called when worker CGMPRelay receives request to watch from client CGMPRelay */
    public boolean viewRequested();

    /** Called when worker CGMPRelay receives request for environment from client CGMPRelay */
    public Object envRequested();

    /** Called when worker CGMPRelay receives request for card from client CGMPRelay */
    public Object cardRequested();

    /**
     * Called when worker CGMPRelay receives a connection request from client CGMPRelay
     *
     * @param identifier The name of the client relay.
     */
    public void clientConnected(String identifier);
}
