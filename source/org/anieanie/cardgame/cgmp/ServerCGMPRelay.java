/*
 * ServerCGMPRelay.java
 *
 * Created on May 5, 2005, 7:28 PM
 */

package org.anieanie.cardgame.cgmp;

import java.io.IOException;
import java.net.*;

import org.anieanie.card.Card;
import org.anieanie.cardgame.gameplay.GameEnvironment;

/**
 *
 * @author  ALMAUDOH
 */
public class ServerCGMPRelay extends CGMPRelay {

    public ServerCGMPRelay(Socket s) {
        super(s);
    }
    
    /** Creates a new instance of ServerCGMPRelay */
    public ServerCGMPRelay(Socket s, ServerCGMPRelayListener sl) {
        super(s, sl);
    }
    
    /**
     * Used by Game Worker to request a move from the client. This tells the client that it is his turn to play.
     */
    public void requestMove() throws IOException, CGMPException {
        sendMessage(CGMPMessage.request(CGMPSpecification.MOVE), false);
    }

    /**
     * Used by Game Worker to accept a move it received from the game client
     */
    public void sendMoveAcknowledgment(String move) throws IOException, CGMPException {
        sendMessage(new CGMPMessage(CGMPSpecification.MACK, move), false);
    }

    /**
     * Used by Game Worker to reject a move it received from the game client
     */
    public void sendMoveRejection(String move) throws IOException, CGMPException {
        sendMessage(new CGMPMessage(CGMPSpecification.MNAK, move), false);
    }

    /** Used by Game Worker to send a copy of the Game Environment to the Game Clients
     * @param env The environment object to be sent
     */
    public void sendEnvironment(GameEnvironment env) throws IOException, CGMPException {
        sendMessage(new CGMPMessage(CGMPSpecification.ENVR , env.toCGMPString()), false);
    }

    /** Used by Game Worker to send a card to the Game client
     * @param cards An array of Card objects to be sent
     * @return boolean true if card was successfully received, false otherwise
     */
    public boolean sendCard(Card[] cards) {
        if (cards.length > 0) {
            try {
                StringBuilder strCards = new StringBuilder();
                for (Card card : cards) {
                    strCards.append(card.toString()).append(';');
                }
                strCards.deleteCharAt(strCards.length() - 1);
                CGMPMessage response = sendMessage(new CGMPMessage(CGMPSpecification.CARD, strCards.toString()), true);
                return response.isAcknowledgement();
            } catch (CGMPConnectionException e) {
                // We may not always have a response from scans.
            } catch (CGMPException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /** 
     * Used by Game Worker to send a notification that someone has won to the Game client
     */
    public void sendGameWon(String gameWinner) {
        try {
            sendMessage(new CGMPMessage(CGMPSpecification.WON, gameWinner), false);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called by the scan() method to handle the message received during a passive scan.
     */
    protected void handleMessage(CGMPMessage response) {
        if (this.listener == null) {
            return;
        }
        ServerCGMPRelayListener listener = (ServerCGMPRelayListener) this.listener;
        String op = response.getKeyword();
        String arg = response.getArguments();
        if (op.equals(CGMPSpecification.HANDSHAKE)) {
            listener.clientConnected(arg);
        }

        else if (op.equals(CGMPSpecification.REQ)) {
            if (arg.equals(CGMPSpecification.PLAY)) {
                listener.playRequested();
            }
            else if (arg.equals(CGMPSpecification.VIEW)) {
                listener.viewRequested();
            }
            else if (arg.equals(CGMPSpecification.ENVR)) {
                listener.environmentRequested();
            }
            else if (arg.equals(CGMPSpecification.MOVE)) {
                sendError(CGMPSpecification.Error.BAD_MSG);
            }
            else if (arg.equals(CGMPSpecification.CARD)) {
                listener.cardRequested();
            }
            else if (arg.equals(CGMPSpecification.START)) {
                listener.gameStartRequested();
            }
        }

        else if (op.equals(CGMPSpecification.ENVR)) {
            // listener.environmentReceived(arg);
        }

        else if (op.equals(CGMPSpecification.CARD)) {
            // listener.cardReceived(arg);
        }

        else if (op.equals(CGMPSpecification.MOVE)) {
            // Can't receive a move without asking for it
            listener.moveReceived(arg);
        }

        else if (op.equals(CGMPSpecification.INFO)) {
            listener.infoReceived(arg);
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
