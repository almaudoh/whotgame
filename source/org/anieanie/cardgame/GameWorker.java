/*
 * GameWorker.java
 *
 * Created on February 20, 2005, 11:08 PM
 */

package org.anieanie.cardgame;

import java.io.IOException;

import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.utils.Debugger;

/**
 *
 * @author  ALMAUDOH
 */
public class GameWorker extends Thread implements ServerCGMPRelayListener {

    // Socket socket;
    private String username;
    private GameMonitor monitor;
    private ServerCGMPRelay relay;

    /** Creates a new instance of GameWorker */
    public GameWorker(ServerCGMPRelay relay, GameMonitor monitor) {
        super();
        this.monitor = monitor;
        this.relay = relay;
        this.relay.setListener(this);
        this.relay.addLowLevelListener(Debugger.getLowLevelListener("Port " + relay.getSocket().getPort()));
    }

    // -------------------run------------------------------
    public void run() {
        while (true) {
            try {
                // @todo If for some reason, the client disconnects, we need to kill this worker.
                relay.scan();
                Thread.sleep(500);

            }    // End of try
            catch (CGMPConnectionException e) {
                // We may not always have a response from scans.
            }
            catch (Exception e) {
                System.out.println("Error has occurred in Worker.");
                e.printStackTrace();
            }    // End of exception
        }    // End of while

    }	// End of run()

    /**
     * getter for the CGMPRelay so that they can be accessed directly
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

    /** Called when worker CGMPRelay receives request for environment from client CGMPRelay */
    public void envRequested() {
        if (monitor.isViewer(username)) {
            System.out.println("environment requested");
        }
    }

    /** Called when worker CGMPRelay receives request for card from client CGMPRelay */
    public void cardRequested() {
        if (monitor.canHaveCard(username)) {
            relay.sendCard(monitor.getCardForUser(username));
        }
    }

    /** Called when worker CGMPRelay receives request to start the game from a client CGMPRelay */
    public void gameStartRequested() {
        try {
            if (monitor.canStartGame()) {
                relay.sendAcknowledgement();
                monitor.startGame();
            } else {
                relay.sendRejection();
            }
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the client or server is terminated 
     */
    public void relayTerminated() {
        // Now, close the socket after deleting that socket from online list
        // if you terminate the relay, you must terminate the GameWorker since 
        // communication will have been breached
        try {
            //        Socket s = (Socket)tOnlineUsers.remove(username);
            //        tOfflineUsers.put(username, s);
            System.out.println("client called for termination");
//          System.out.println("relay terminated, cleanup needed");
            relay = null;
//            System.out.println("finalizing worker");
//            this.finalize();
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

}
