/*
 * GameServer.java
 *
 * Created on February 20, 2005, 1:07 AM
 */

package org.anieanie.cardgame.gameplay;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.anieanie.cardgame.cgmp.ServerCGMPRelay;

/**
 *
 * @author  ALMAUDOH
 */
public class GameServer {
    private GameMonitor gameMonitor;
    private ArrayList<GameWorker> workers;
    private ServerSocket ss;
    private int serverPort;

    public GameServer(GameMonitor monitor, int port_number) {
        gameMonitor = monitor;
        workers = new ArrayList<GameWorker>();
        serverPort = port_number;
    }

    public void start() {
        try {
            // Create a socket on server
            ss = new ServerSocket(serverPort);

            System.out.println("Server running on port " + ss.getLocalPort());

            // Create a new GameLoop Thread where the game loop happens.
            GameLoop gameloop = new GameLoop(gameMonitor);
            gameloop.start();

            // ---------------------------------------------------------------
            // Now start accepting connections from clients in a while loop
            // The server should run in an infinite loop
            while(true) {
                Socket socket = ss.accept();	// accept connection from client
                System.out.println("A new client is connected.");

                // Create a thread to allow simultaneous connections
                // There is need to check workers on a continual basis to ensure that their clients are still there
                // disconnected workers should be discarded
                GameWorker worker = new GameWorker(new ServerCGMPRelay(socket), gameMonitor);
                workers.add(worker);
                worker.start();

                // Free CPU cycles.
                Thread.sleep(100);
            }	// End of while

        }	// End of try
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("Some kind of error has occurred.");
        }	// End of exception

    }	// End of main()

    // Override finalize() to close server socket
    protected void finalize() throws Throwable {
        super.finalize();
        if (workers != null) {
            for (GameWorker w : workers) {
                try {
                    // @todo Stop this worker properly.
                    w.finalize();
                }
                catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            workers = null;
        }
        if (ss != null) {
            try {
                ss.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            ss = null;
        }
    }
}
