/*
 * ClientCGMPRelay.java
 *
 * Created on May 5, 2005, 7:29 PM
 */

package org.anieanie.cardgame.cgmp;

import org.anieanie.cardgame.environment.GameLoop;

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
        catch (CGMPConnectionException e) {
            // A connection exception means the request was not received. So abort and try again.
            return false;
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
   public Object requestEnvironment(GameLoop env) {
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
    
    public boolean sendCard(String cardspec) throws IOException, CGMPException {
        CGMPMessage response = sendMessage(new CGMPMessage(CGMPSpecification.MOVE, cardspec), true);
        if (response.getKeyword().equals(CGMPSpecification.MACK)) {
            // Server returns invalid response.
            return true;
        }
        else if (response.getKeyword().equals(CGMPSpecification.MNAK)) {
            return false;
        }
        else {
            throw new CGMPException("Invalid response from server");
        }
    }
    
    protected void handleMessage(CGMPMessage response) {
        if (this.listener == null) {
            return;
        }
        ClientCGMPRelayListener listener = (ClientCGMPRelayListener) this.listener;
        String op = response.getKeyword();
        String arg = response.getArguments();
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

        else if (op.equals(CGMPSpecification.INFO)) {
            listener.infoReceived(arg);
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
