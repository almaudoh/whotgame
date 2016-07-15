/*
 * GameWorker.java
 *
 * Created on February 20, 2005, 11:08 PM
 */

package org.anieanie.cardgame.gameplay;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.utils.Debugger;

/**
 *
 * @author  ALMAUDOH
 */
public class GameWorker extends Thread implements ServerCGMPRelayListener {

    private String username;
    private GameMonitor monitor;
    private ServerCGMPRelay relay;
    private boolean relayTerminated;

    public GameWorker(ServerCGMPRelay relay, GameMonitor monitor) {
        this(relay, monitor, false);
    }

    /** Creates a new instance of GameWorker */
    public GameWorker(ServerCGMPRelay relay, GameMonitor monitor, boolean debug) {
        super();
        this.monitor = monitor;
        this.relay = relay;
        this.relay.setListener(this);
        this.relayTerminated = false;
        if (debug) {
            try {
                this.relay.addLowLevelListener(Debugger.getLowLevelListener("Port " + relay.getSocket().getPort(), "monitor_" + monitor.hashCode()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------run------------------------------
    public void run() {
        while (!relayTerminated) {
            try {
                // @todo If for some reason, the client disconnects, we need to kill this worker.
                relay.scanWithThreadSleep();

            }    // End of try
            catch (CGMPConnectionException e) {
                // We may not always have a response from scans.
            }
            catch (NullPointerException e) {
                // Most likely, the relay has been nulled, so break the loop.
                relayTerminated = true;
            }
            catch (Exception e) {
                System.out.println("Error has occurred in Worker.");
                e.printStackTrace();
                relayTerminated = true;
            }    // End of exception
        }    // End of while
    }	// End of run()

    /**
     * Getter for the CGMPRelay so that they can be accessed directly
     */
    public CGMPRelay getRelay() {
        return this.relay;
    }

    // Methods implemented by interface ServerCGMPRelayListener
    @Override
    public void clientConnected(String identifier) {
        try {
            // The initial hello message from the user. So acknowledge.
            username = identifier;
            relay.sendAcknowledgement(username);
            monitor.addUser(username, relay);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Called when worker CGMPRelay receives request to play from client CGMPRelay */
    public void playRequested() {
        if (monitor.canPlayGame(username)) {
            try {
                relay.sendAcknowledgement();
                monitor.addPlayer(username);
            } catch (CGMPConnectionException e) {
                // What to do?
            } catch (CGMPException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Called when worker CGMPRelay receives request to watch from client CGMPRelay */
    public void viewRequested() {
        if (monitor.canViewGame(username)) {
            try {
                relay.sendAcknowledgement();
                monitor.addViewer(username);
            } catch (CGMPConnectionException e) {
                // What to do?
            } catch (CGMPException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Called when worker CGMPRelay receives request to start the game from a client CGMPRelay */
    public void gameStartRequested() {
        try {
            if (monitor.requestStartGame()) {
                relay.sendAcknowledgement();
            } else {
                relay.sendRejection();
            }
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Called when worker CGMPRelay receives request for environment from client CGMPRelay */
    public void environmentRequested() {
        try {
            if (monitor.isPlayer(username) || monitor.isViewer(username)) {
                relay.sendEnvironment(monitor.getEnvironment());
            } else {
                relay.sendRejection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    /** Called when worker CGMPRelay receives request for card from client CGMPRelay */
    public void cardRequested() {
        // Only if it's the player's turn will a card be sent.
        if (monitor.canHaveCard(username)) {
            relay.sendCard(monitor.getCardForUser(username));
        }
        else {
            try {
                relay.sendRejection();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (CGMPException e) {
                e.printStackTrace();
            }
        }
    }

    public void moveReceived(String move) {
        try {
            // Check if it is the user's turn.
            if (monitor.canMakeMove(username)) {
                if (monitor.receiveMoveFromUser(move, username)) {
                    relay.sendMoveAcknowledgment(move);
                }
                else {
                    relay.sendMoveRejection(move);
                }
            }
            else {
                // @todo A better way to send more specific error messages to the client.
                relay.sendError(CGMPSpecification.Error.BAD_SYN);
            }
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void infoReceived(String info) {
        monitor.handleInfoReceived(info, username);
    }

    /**
     * Called when the client or server is terminated 
     */
    public void relayTerminated() {
        // Now, close the socket after deleting that socket from online list
        // if you terminate the relay, you must terminate the GameWorker since 
        // communication will have been breached
        try {
            System.out.println("client called for termination");
            relayTerminated = true;
        }
        catch (Exception e) { e.printStackTrace(); }
        catch (Throwable t) { t.printStackTrace(); }
    }

    @Override
    public void messageSent(CGMPMessage message) {}

    @Override
    public void messageReceived(CGMPMessage message) {}

    @Override
    public void errorSent(int errorcode) {}

    @Override
    public void errorReceived(int errorcode) {}

    @Override
    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        if (relay != null) {
            relay.disconnect();
        }
        super.finalize();
    }

    public boolean relayWasTerminated() {
        return relayTerminated;
    }
}
