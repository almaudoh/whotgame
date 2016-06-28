/*
 * AbstractGameClient.java
 *
 * Created on March 31, 2007, 5:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.anieanie.cardgame;

import java.io.*;

import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.environment.GameEnvironment;

/**
 *
 * @author Aniebiet
 */
public abstract class AbstractGameClient implements ClientCGMPRelayListener {

    public static final int STATUS_UNDEFINED = -1;
    public static final int STATUS_WAITING_TO_START = 0;
    public static final int STATUS_WAITING_FOR_TURN = 1;
    public static final int STATUS_WAITING_FOR_USER = 2;
    public static final int STATUS_GAME_WON = 3;
    public static final int STATUS_TERMINATE = 10;

    protected GameEnvironment environment;
    protected ClientCGMPRelay relay;
    protected String name;
    protected int clientStatus = STATUS_UNDEFINED;

    /**
     * Creates a new instance of AbstractGameClient
     */
    public AbstractGameClient(ClientCGMPRelay relay, String name) {
        this.relay = relay;
        this.name = name;
        this.environment = new GameEnvironment();
    }
    
    /**
     * Creates a new instance of AbstractGameClient
     */
    public AbstractGameClient(ClientCGMPRelay relay) {
        /** @todo Generate random user name if function call returns null */
        this.relay = relay;
        this.name = null;
        this.environment = new GameEnvironment();
    }
    
    public void connect() throws CGMPException, IOException {
        if (!relay.connect(name)) {
            throw new GameClientException(String.format("Server not responding on ip address %s and port %s",
                    relay.getSocket().getInetAddress(), relay.getSocket().getPort()));
        }
    }

    public void close() throws IOException, CGMPException {
        relay.disconnect();
    }

    /* Possible values for client status
     * -1 - Undefined
     *  0 - Waiting to start game
     *  1 - Waiting for turn
     *  2 - Waiting for user input (action)
     *  3 - Game has been won
     * 10 - Terminate Game
     */
    public int getClientStatus() {
        return clientStatus;
    }

    public void refreshClientStatus() {
        try {
            environment = relay.requestEnvironment();
            updateClientStatus();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CGMPConnectionException e) {
            e.printStackTrace();
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateClientStatus() {
        // @todo: Need to look deeper into this.
        String currentPlayer = environment.get("CurrentPlayer");
        if (currentPlayer != null && currentPlayer.equals(getUsername())) {
            clientStatus = STATUS_WAITING_FOR_USER;
        }
        else {
            clientStatus = STATUS_WAITING_FOR_TURN;
        }
    }

    // Game management methods.
    public abstract CardSet getCards();

    public abstract void playCard(String card);

    public abstract void startGame();

    public abstract void requestCard();

    protected abstract void run();
    
    // Later I may implement this to generate a random name
    public abstract String getUsername();

    // Events
    @Override
    public void messageSent(CGMPMessage message) {}

    @Override
    public void messageReceived(CGMPMessage message) {}

    @Override
    public void errorSent(int errorcode) {
        // Let's log the errors to console for now.
        System.out.println("An error was sent: " + errorcode + " [" + CGMPSpecification.Error.describeError(errorcode) + "]");
    }

    @Override
    public void errorReceived(int errorcode) {
        // Let's log the errors to console for now.
        System.out.println("An error occurred: " + errorcode + " [" + CGMPSpecification.Error.describeError(errorcode) + "]");

        // @todo: Should we throw exceptions on CGMP errors here? or in the listener?
//        switch (errorcode) {
//            case CGMPSpecification.Error.BAD_PROTO:
//                // // TODO: 5/21/16
//                // Potential weakness here (stack overflow) if other end continues to send BAD_PROTO errors
//                // regardless of what comes in.
//                return sendMessage(msg);
//
//            case CGMPSpecification.Error.BAD_KWD:
//                throw new CGMPException("Message contains bad keyword");
//
//            case CGMPSpecification.Error.BAD_SYN:
//                throw new CGMPException("Message has wrong syntax");
//
//            case CGMPSpecification.Error.BAD_MSG:
//                throw new CGMPException("Inappropriate message or reply sent");
//
//            default:
//                throw new CGMPException("Unknown error in message");
//        }
    }

    /** Called when client CGMPRelay receives game environment state from worker CGMPRelay */
    public void environmentReceived(String envSpec) {
        environment = GameEnvironment.fromCGMPString(envSpec);
        updateClientStatus();
    }

    /** Called when client CGMPRelay receives request for move from worker CGMPRelay */
    public void moveRequested() {
        // Now waiting for user to make move.
        clientStatus = STATUS_WAITING_FOR_USER;
    }

    /** Called when the client CGMPRelay receives card from the worker CGMPRelay at the start of the game */
    public void cardReceived(String cardSpec) {
        try {
            relay.sendAcknowledgement();
        }
        catch (CGMPException ex) {
            ex.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // Now waiting for server to ask for move.
        clientStatus = STATUS_WAITING_FOR_TURN;
    }

    public void gameWon(String winner) {
        System.out.println("game won by " + winner);
    }

    /** Called when the client or server is terminated */
    public void relayTerminated() {
        try {
            // Now, close the socket after deleting that socket from online list
            //        Socket s = (Socket)tOnlineUsers.remove(getUsername());
            //        tOfflineUsers.put(getUsername(), s);
            System.out.println("relay terminated, cleanup needed");
            //relay = null;
            System.out.println("finalizing worker");
            //this.finalize();
        }
        catch (Exception e) {

        }
        catch (Throwable t) {

        }
    }
}
