/*
 * ServerCGMPRelay.java
 *
 * Created on May 5, 2005, 7:28 PM
 */

package org.anieanie.cardgame.cgmp;

import java.net.*;
import org.anieanie.cardgame.*;
import org.anieanie.cardgame.cgmp.*;

/**
 *
 * @author  ALMAUDOH
 */
public class ServerCGMPRelay extends CGMPRelay {
    
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
            String msg = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.MOVE).trim();
            String op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' '))) : msg;

            // Loop until you get a valid message or maximum number of tries is exceeded
            while (!op.equals(CGMPSpecification.MOVE) && ++trials < CGMPSpecification.MAX_TRIES) {
                if (!op.equals("")) sendError(CGMPSpecification.Error.BAD_MSG);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                msg = sendMessage(CGMPSpecification.REQ + " " + CGMPSpecification.MOVE).trim();
                op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' '))) : msg;
            }
            if (!op.equals(CGMPSpecification.MOVE)) return null;
            String arg = (msg.indexOf(' ') > -1) ? (msg.substring(msg.indexOf(' '))) : "";
            return AbstractCard.fromString(arg);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Used by Game Worker to accept/confirm move it received from the game client
     */
    public void acceptMove(Card move) {
        try {
            String resp = sendMessage(CGMPSpecification.MACK + " " + move);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    /** Used by Game Worker to send a copy of the Game Environment to the Game Clients
     * @param env The environment object to be sent
     */
    public void sendEnvironment(Object env) {
        try {
            String resp = sendMessage(CGMPSpecification.ENVR + " " + env);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    /** Used by Game Worker to send a card to the Game client
     * @param card The Card object to be sent
     * @return boolean true if card was successfully received, false otherwise
     */
    public boolean sendCard(Object card) {
        try {
            String resp = sendMessage(CGMPSpecification.CARD + " " + card);
            resp = (resp.indexOf(' ') > -1) ? (resp.substring(0, resp.indexOf(' '))) : resp;
            return (resp.equals(CGMPSpecification.ACK)) ? true : false;
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 
     * Used by Game Worker to send a notification that someone has won to the Game client
     */
    public void sendGameWon() {
        try {
            String resp = sendMessage(CGMPSpecification.WON);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called by the scan() method to handle the message received during a passive scan
     */
    protected void handleMessage(String msg) {
        ServerCGMPRelayListener list = (ServerCGMPRelayListener) listener;
        String op = (msg.indexOf(' ') > -1) ? (msg.substring(0, msg.indexOf(' ')).trim()) : msg;
        String arg = (msg.indexOf(' ') > -1) ? (msg.substring(msg.indexOf(' ')).trim()) : "";
        //System.out.println("op: " + op + "; arg" + arg);
        //list.playRequested();
        if (op.equals(CGMPSpecification.REQ)) {
            //            System.out.println("request made; op: " + op + "; arg" + arg);
            if (arg.equals(CGMPSpecification.PLAY)) list.playRequested();
            else if (arg.equals(CGMPSpecification.VIEW)) list.viewRequested();
            else if (arg.equals(CGMPSpecification.ENVR)) list.envRequested();
            else if (arg.equals(CGMPSpecification.MOVE)) { sendError(CGMPSpecification.Error.BAD_MSG); }
            else if (arg.equals(CGMPSpecification.CARD)) list.cardRequested();
        }

        else if (op.equals(CGMPSpecification.ENVR)) {
            // list.envReceived(arg);
        }

        else if (op.equals(CGMPSpecification.CARD)) {
            // list.cardReceived(arg);
        }

        else if (op.equals(CGMPSpecification.MOVE)) {
            // Can't receive a move without asking for it
            // list.moveReceived(Card.fromString(arg));
        }

        else if (op.equals(CGMPSpecification.MACK)) {
            // list.moveAccepted(arg);
        }

        else if (op.equals(CGMPSpecification.WON)) {
            // list.gameWon();
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
            listener.errorReceived(Integer.parseInt(arg));
        }

    }

}
