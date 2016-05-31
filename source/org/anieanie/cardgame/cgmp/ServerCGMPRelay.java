/*
 * ServerCGMPRelay.java
 *
 * Created on May 5, 2005, 7:28 PM
 */

package org.anieanie.cardgame.cgmp;

import java.io.IOException;
import java.net.*;

import org.anieanie.card.AbstractCard;
import org.anieanie.card.Card;

/**
 *
 * @author  ALMAUDOH
 */
public class ServerCGMPRelay extends CGMPRelay {

    public ServerCGMPRelay(Socket s) {
        super(s, null);
        System.out.println("ServerCGMPRelay for socket "+s+" initiated");
    }
    
    /** Creates a new instance of ServerCGMPRelay */
    public ServerCGMPRelay(Socket s, ServerCGMPRelayListener sl) {
        super(s, sl);
        System.out.println("ServerCGMPRelay for socket "+s+" initiated");
    }
    
    /** Used by Game Worker to request a move from the client
     * @return Object A (Card) object representing the move chosen by the client or null if unsuccessful
     */
    public Card requestMove() {
        try {
            int trials = 0;
            CGMPResponse response = sendRequest(CGMPSpecification.MOVE);
            String op = response.getKeyword();

            // Loop until you get a valid message or maximum number of tries is exceeded
            while (!op.equals(CGMPSpecification.MOVE) && ++trials < CGMPSpecification.MAX_TRIES) {
                if (!op.equals("")) sendError(CGMPSpecification.Error.BAD_MSG);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                response = sendRequest(CGMPSpecification.MOVE);
                op = response.getKeyword();
            }
            if (!op.equals(CGMPSpecification.MOVE)) return null;
            return AbstractCard.fromString(response.getArguments());
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Used by Game Worker to accept/confirm move it received from the game client
     */
    public void acceptMove(Card move) {
        try {
            sendMessage(CGMPSpecification.MACK + " " + move);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Used by Game Worker to send a copy of the Game Environment to the Game Clients
     * @param env The environment object to be sent
     */
    public void sendEnvironment(Object env) {
        try {
            sendMessage(CGMPSpecification.ENVR + " " + env);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Used by Game Worker to send a card to the Game client
     * @param card The Card object to be sent
     * @return boolean true if card was successfully received, false otherwise
     */
    public boolean sendCard(Object card) {
        try {
            CGMPResponse response = sendMessage(CGMPSpecification.CARD + " " + card);
            return response.getKeyword().equals(CGMPSpecification.ACK);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 
     * Used by Game Worker to send a notification that someone has won to the Game client
     */
    public void sendGameWon() {
        try {
            sendMessage(CGMPSpecification.WON);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called by the scan() method to handle the message received during a passive scan
     */
    protected void handleResponse(CGMPResponse response) {
        ServerCGMPRelayListener listener = (ServerCGMPRelayListener) this.listener;
        String op = response.getKeyword();
        String arg = response.getArguments();
        //System.out.println("op: " + op + "; arg" + arg);
        //listener.playRequested();
        if (op.equals(CGMPSpecification.REQ)) {
            if (arg.equals(CGMPSpecification.PLAY)) {
                listener.playRequested();
            }
            else if (arg.equals(CGMPSpecification.VIEW)) {
                listener.viewRequested();
            }
            else if (arg.equals(CGMPSpecification.ENVR)) {
                listener.envRequested();
            }
            else if (arg.equals(CGMPSpecification.MOVE)) {
                sendError(CGMPSpecification.Error.BAD_MSG);
            }
            else if (arg.equals(CGMPSpecification.CARD)) {
                listener.cardRequested();
            }
        }

        else if (op.equals(CGMPSpecification.ENVR)) {
            // listener.envReceived(arg);
        }

        else if (op.equals(CGMPSpecification.CARD)) {
            // listener.cardReceived(arg);
        }

        else if (op.equals(CGMPSpecification.MOVE)) {
            // Can't receive a move without asking for it
            // listener.moveReceived(Card.fromString(arg));
        }

        else if (op.equals(CGMPSpecification.MACK)) {
            // listener.moveAccepted(arg);
        }

        else if (op.equals(CGMPSpecification.WON)) {
            // listener.gameWon();
        }

        else if (op.equals(CGMPSpecification.TERM)) {
            try {
                sendAcknowledgement();
            }
            catch (CGMPException ex) {
                //ex.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            listener.relayTerminated();
        }

        else if (op.equals(CGMPSpecification.ACK)) {

        }

        else if (op.equals(CGMPSpecification.NAK)) {

        }

        else if (op.equals(CGMPSpecification.WAIT)) {
            // scan();
        }

        else if (op.equals(CGMPSpecification.ERR)) {
            this.listener.errorReceived(Integer.parseInt(arg));
        }

    }

}