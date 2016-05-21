/*
 * GameWorker.java
 *
 * Created on February 20, 2005, 11:08 PM
 */

package org.anieanie.cardgame;

import java.io.*;
import java.net.*;
import java.util.*;

import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.GameEnvironment.GameMonitor;
import org.anieanie.whot.WhotCard;

/**
 *
 * @author  ALMAUDOH
 */
public class GameWorker extends Thread implements ServerCGMPRelayListener {

    // Socket socket;
    String strUserName;
    GameMonitor gMon;
    ServerCGMPRelay relay;

    /** Creates a new instance of GameWorker */
    public GameWorker(Socket s, GameMonitor gm, String userName) {
        // socket = s;
        super(userName);
        gMon = gm;
        strUserName = userName;
        relay = new ServerCGMPRelay(s, this);
    }

    // -------------------run------------------------------
    public void run() {
        try {
            while(true) {
                relay.scan();
                Thread.sleep(50);
            }	// End of while

        }	// End of try
        catch(Exception e) {
            System.out.println("Error has occurred in Worker.");
            e.printStackTrace();
        }	// End of exception

    }	// End of run()

    /**
     * getter for the CGMPRelay so that they can be accessed directly
     */
    public CGMPRelay getRelay() {
        return this.relay;
    }

    /*
     * Methods implemented by interface ServerCGMPRelayListener
     */
    /** Called when worker CGMPRelay receives request to play from client CGMPRelay */
    public boolean playRequested() {
        System.out.println("play requested");
        // Later route this through the GameMonitor
        try {
            relay.sendAcknowledgement();
            WhotCard card1 = new WhotCard(WhotCard.ANGLE, 3);
            System.out.print("Sending card " + card1 + " waiting for acknowledgement ");
            int ct = 0;
            // Loop until card is received
            relay.sendCard(card1);
//            while (!relay.sendCard(card1) && ct++ < 10000) {
//                try {
//                    System.out.print(".");
//                    Thread.sleep(50);
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//            }
            System.out.print("\n");
            relay.requestMove();
        } catch (CGMPException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /** Called when worker CGMPRelay receives request to watch from client CGMPRelay */
    public boolean viewRequested() {
        System.out.println("view requested");
        return true;
    }

    /** Called when worker CGMPRelay receives request for environment from client CGMPRelay */
    public Object envRequested() {
        System.out.println("environment requested");
        return null;
    }

    /** Called when worker CGMPRelay receives request for card from client CGMPRelay */
    public Object cardRequested() {
        System.out.println("card requested");
        relay.sendCard(new WhotCard(3,6));
        return null;
    }

    /** 
     * Called when the client or server is terminated 
     */
    public boolean relayTerminated() {
        // Now, close the socket after deleting that socket from online list
        // if you terminate the relay, you must terminate the GameWorker since 
        // communication will have been breached
        try {
            //        Socket s = (Socket)tOnlineUsers.remove(strUserName);
            //        tOfflineUsers.put(strUserName, s);
            System.out.println("client called for termination");
//          System.out.println("relay terminated, cleanup needed");
            relay = null;
//            System.out.println("finalizing worker");
//            this.finalize();
        }
        catch (Exception e) { e.printStackTrace(); }
        catch (Throwable t) { t.printStackTrace(); }
        return true;
    }

    public void errorReceived(int errorcode) {
        System.out.println("error received: " + errorcode + " " + CGMPSpecification.Error.describeError(errorcode));
    }
    
    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        if (relay != null) {
            relay.terminateRelay();
        }
        super.finalize();
    }

    public void start() {
        System.out.println("Worker for socket " + this.strUserName + " started");
        super.start();
    }
}
