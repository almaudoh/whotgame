/*
 * ClientCGMPRelay.java
 *
 * Created on May 5, 2005, 7:29 PM
 */

package org.anieanie.cardgame.cgmp;

import java.net.*;
import org.anieanie.cardgame.*;
import org.anieanie.cardgame.cgmp.*;

/**
 *
 * @author  ALMAUDOH
 */
public class ClientCGMPRelay extends CGMPRelay {
    
    /** Creates a new instance of ServerCGMPRelay */
    public ClientCGMPRelay(Socket s, ClientCGMPRelayListener sl) {
        super(s, sl);
        System.out.println("ClientCGMPRelay for socket "+s+" initiated");
    }
    
    /** Used by Game Client to request permission to play in a game
     * @return boolean true if the request is granted, false otherwise
     */
    public boolean requestPlay() {
        try {
            String resp = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.PLAY).trim();
            
            if (resp.indexOf(' ') != -1) resp = resp.substring(0, resp.indexOf(' ')).trim();
            
            if (resp.equals(CGMPSpecification.ACK)) return true;
            else return false;
        } catch (CGMPException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /** Used by Game Client to request permission to watch a game
     * @return boolean true if request is granted, false otherwise
     */
    public boolean requestView() {
        try {
            String resp = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.VIEW);
            if (resp.substring(0, resp.indexOf(' ')).trim().equals(CGMPSpecification.ACK)) return true;
            else return false;
        } catch (CGMPException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /** Used by Game Client to request the current state of the Game Environment
     * @return Object An object representing the current Game Environment
     *    or null if unsuccessful
     */
   public Object requestEnvironment(GameEnvironment env) {
        try {
            int trials = 0;
            String msg = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.ENVR).trim();
            String op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' '))) : msg;
    
            // Loop until you get a valid message or maximum number of tries is exceeded
            while (!op.equals(CGMPSpecification.ENVR) && ++trials < CGMPSpecification.MAX_TRIES) {
                sendError(CGMPSpecification.Error.BAD_MSG);
                msg = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.ENVR);
                op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' '))) : msg;
            }
            if (!op.equals(CGMPSpecification.ENVR)) return null;
            String arg = (msg.indexOf(' ') > -1) ? (msg.substring(msg.indexOf(' '))) : "";
            //return env.fromString(arg);
            return null;
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
    * Used by Game Client to request a card to be given to them
    * @return Object An object (Card) representing the card given or null if unsuccessful
    * @param card
    */
    public Card requestCard() {
        try {
            int trials = 0;
            String msg = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.CARD).trim();
            String op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' '))) : msg;
            
            // Loop until you get a valid message or maximum number of tries is exceeded
            while (!op.equals(CGMPSpecification.CARD) && ++trials < CGMPSpecification.MAX_TRIES) {
                sendError(CGMPSpecification.Error.BAD_MSG);
                msg = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.CARD).trim();
                op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' '))) : msg;
            }
            if (!op.equals(CGMPSpecification.CARD)) return null;
            String arg = (msg.indexOf(' ') > -1) ? (msg.substring(msg.indexOf(' '))) : "";
            return AbstractCard.fromString(arg);
        } catch (CGMPException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected void handleMessage(String msg) {
        ClientCGMPRelayListener list = (ClientCGMPRelayListener) listener;
        String op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' ')).trim()) : msg;
        String arg = (msg.indexOf(' ') > -1) ? (msg.substring(msg.indexOf(' ')).trim()) : "";
        //System.out.println("op: " + op + "; arg" + arg);
        //list.playRequested();
        if (op.equals(CGMPSpecification.REQ)) {
            //            System.out.println("request made; op: " + op + "; arg" + arg);
            if (arg.equals(CGMPSpecification.PLAY)) { sendError(CGMPSpecification.Error.BAD_MSG); } else if (arg.equals(CGMPSpecification.VIEW)) { sendError(CGMPSpecification.Error.BAD_MSG); } else if (arg.equals(CGMPSpecification.ENVR)) { sendError(CGMPSpecification.Error.BAD_MSG); } else if (arg.equals(CGMPSpecification.MOVE)) list.moveRequested();
            else if (arg.equals(CGMPSpecification.CARD)) { sendError(CGMPSpecification.Error.BAD_MSG); }
        }
        
        else if (op.equals(CGMPSpecification.ENVR)) {
            list.envReceived(arg);
        }
        
        else if (op.equals(CGMPSpecification.CARD)) {
            list.cardReceived(arg);
        }
        
        else if (op.equals(CGMPSpecification.MOVE)) {
            // Can't receive a move without asking for it
            // list.moveReceived(Card.fromString(arg));
        }
        
        else if (op.equals(CGMPSpecification.MACK)) {
            list.moveAccepted(arg);
        }
        
        else if (op.equals(CGMPSpecification.WON)) {
            list.gameWon();
        }
        
        else if (op.equals(CGMPSpecification.TERM)) {
            try {
                sendMessage(CGMPSpecification.ACK);
            } catch (CGMPException ex) {
                //ex.printStackTrace();
            }
            list.relayTerminated();
        }
        
        else if (op.equals(CGMPSpecification.ACK)) {
            
        }
        
        else if (op.equals(CGMPSpecification.NAK)) {
            
        }
        
        else if (op.equals(CGMPSpecification.WAIT)) {
            // scan();
        }
        
        else if (op.equals(CGMPSpecification.ERR)) {
            list.errorReceived(Integer.parseInt(arg));
        }
        
    }
    
}
