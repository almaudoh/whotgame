/*
 * ClientCGMPRelayListener.java
 *
 * Created on May 5, 2005, 8:14 PM
 */

package org.anieanie.cardgame.cgmp;

/**
 *
 * @author  ALMAUDOH
 */
public interface ClientCGMPRelayListener extends CGMPRelayListener {
    
    /** Called when client CGMPRelay receives request for move from worker CGMPRelay */
    public Object moveRequested();

    /** Called when client CGMPRelay receives confirmation of validity of move from worker CGMPRelay */
    public void moveAccepted(String moveSpec);

    /** Called when client CGMPRelay receives environment from worker CGMPRelay */
    public void envReceived(String envSpec);

    /** Called when the client CGMPRelay receives card from the worker CGMPRelay */
    public boolean cardReceived(String cardSpec);

    /** Called when the client CGMPRelay receives notice that someone has won from the worker CGMPRelay
     * @param winner*/
    public void gameWon(String winner);

}
