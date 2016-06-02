/*
 * ClientCGMPRelay.java
 *
 * Created on May 5, 2005, 7:29 PM
 */

package org.anieanie.cardgame.cgmp;

import org.anieanie.card.AbstractCard;
import org.anieanie.card.Card;
import org.anieanie.cardgame.environment.GameEnvironment;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author  ALMAUDOH
 */
public class ClientCGMPRelay extends CGMPRelay {

    public ClientCGMPRelay(Socket s) {
        super(s);
    }
    
    /** Creates a new instance of ServerCGMPRelay */
    public ClientCGMPRelay(Socket s, ClientCGMPRelayListener sl) {
        super(s, sl);
    }
    
    /**
     * Used by Game Client to request permission to play in a game.
     *
     * @return boolean true if the request is granted, false otherwise
     */
    public boolean requestPlay() {
        try {
            return sendRequest(CGMPSpecification.PLAY).isAcknowledgement();
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
     * Used by Game Client to request permission to watch a game.
     *
     * @return boolean true if request is granted, false otherwise
     */
    public boolean requestView() {
        try {
            return sendRequest(CGMPSpecification.VIEW).getKeyword().equals(CGMPSpecification.ACK);
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
     * Used by Game Client to request the current state of the Game Environment
     *
     * @return Object An object representing the current Game Environment
     *    or null if unsuccessful
     */
   public Object requestEnvironment(GameEnvironment env) {
        try {
            int trials = 0;
            CGMPMessage msg = sendRequest(CGMPSpecification.ENVR);
            String op = msg.getKeyword();
    
            // Loop until you get a valid message or maximum number of tries is exceeded
            while (!op.equals(CGMPSpecification.ENVR) && ++trials < CGMPSpecification.MAX_TRIES) {
                sendError(CGMPSpecification.Error.BAD_MSG);
                msg = sendRequest(CGMPSpecification.ENVR);
                op = msg.getKeyword();
            }
            if (!op.equals(CGMPSpecification.ENVR)) return null;
            String arg = msg.getArguments();
            //return env.fromString(arg);
            return null;
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }
    
    /**
     * Used by Game Client to request a card to be given to them.
     *
     * @return Object An object (Card) representing the card given or null if unsuccessful
     */
    public Card requestCard() throws CGMPException, IOException {
        int trials = 0;
        CGMPMessage msg = sendRequest(CGMPSpecification.CARD);
        String op = msg.getKeyword();

        // Loop until you get a valid message or maximum number of tries is exceeded
        while (!op.equals(CGMPSpecification.CARD) && ++trials < CGMPSpecification.MAX_TRIES) {
            sendError(CGMPSpecification.Error.BAD_MSG);
            msg = sendRequest(CGMPSpecification.CARD);
            op = msg.getKeyword();
        }
        if (!op.equals(CGMPSpecification.CARD)) {
            throw new CGMPException("Invalid card specifications given");
        }
        return AbstractCard.fromString(msg.getArguments());
    }
    
    protected void handleResponse(CGMPMessage response) {
        ClientCGMPRelayListener listener = (ClientCGMPRelayListener) this.listener;
        String op = response.getKeyword();
        String arg = response.getArguments();
        //System.out.println("op: " + op + "; arg" + arg);
        //listener.playRequested();
        if (op.equals(CGMPSpecification.REQ)) {
            // Clients should not handle play, view, environment or card requests.
            if (arg.equals(CGMPSpecification.PLAY)
                || arg.equals(CGMPSpecification.VIEW)
                || arg.equals(CGMPSpecification.ENVR)
                || arg.equals(CGMPSpecification.CARD)) {
                sendError(CGMPSpecification.Error.BAD_MSG);
            }
            // Move requests from the server.
            else if (arg.equals(CGMPSpecification.MOVE)) {
                listener.moveRequested();
            }
        }
        
        else if (op.equals(CGMPSpecification.ENVR)) {
            listener.envReceived(arg);
        }
        
        else if (op.equals(CGMPSpecification.CARD)) {
            listener.cardReceived(arg);
        }
        
        else if (op.equals(CGMPSpecification.MOVE)) {
            // Can't receive a move without asking for it
            // listener.moveReceived(Card.fromString(arg));
        }
        
        else if (op.equals(CGMPSpecification.MACK)) {
            listener.moveAccepted(arg);
        }
        
        else if (op.equals(CGMPSpecification.WON)) {
            listener.gameWon(arg);
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
            listener.errorReceived(Integer.parseInt(arg));
        }
    }
    
}
