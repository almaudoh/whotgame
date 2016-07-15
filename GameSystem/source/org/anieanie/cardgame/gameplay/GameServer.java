/*
 * GameServer.java
 *
 * Created on February 20, 2005, 1:07 AM
 */

package org.anieanie.cardgame.gameplay;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import org.anieanie.cardgame.cgmp.CGMPException;
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
    private boolean keepRunning;
    private static final Object ssLock = new Object();
    private boolean running = false;

    public GameServer(GameMonitor monitor, int port_number) {
        gameMonitor = monitor;
        workers = new ArrayList<GameWorker>();
        serverPort = port_number;
        keepRunning = true;
    }

    public void start() {
        synchronized (ssLock) {
            try {
                // Create a socket on server
                ss = new ServerSocket(serverPort);

                System.out.println("Server running on port " + ss.getLocalPort());

                // Create a new GameLoop Thread where the game loop happens.
                GameLoop gameloop = new GameLoop(gameMonitor);
                gameloop.start();

                launchServerSocket();

                running = true;
                while (keepRunning) {
                    // Wait for game to be finished.
                    Thread.sleep(100);
                }

                // Disconnect all relays.
                for (GameWorker w : workers) {
                    try {
                        w.getRelay().disconnect();
                    } catch (CGMPException e) {
                        e.printStackTrace();
                    }
                }
                // Close the server socket.
                ss.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Some kind of error has occurred.");
            }
            // Mark that game server is done.
            running = false;
        }

    }

    // Launches a server socket on a separate thread.
    private void launchServerSocket() {
        // ---------------------------------------------------------------
        // Now start accepting connections from clients in a while loop
        // The server should run in an infinite loop in a separate thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Socket socket;    // accept connection from client
                    try {
                        socket = ss.accept();
                        System.out.printf("A new client is connected - ipaddr %s, port %s.%n", socket.getInetAddress(), socket.getPort());

                        // Create a thread to allow simultaneous connections
                        // There is need to check workers on a continual basis to ensure that their clients are still there
                        // disconnected workers should be discarded
                        GameWorker worker = new GameWorker(new ServerCGMPRelay(socket), gameMonitor);
                        workers.add(worker);
                        worker.start();
                    } catch (SocketException e) {
                        // Most likely, the socket has been closed. Game ended.
                        break;
                    } catch (IOException e) {
                        // Most likely, the socket has been closed. Game ended.
                        e.printStackTrace();
                        break;
                    }

                }
            }
        }).start();
    }

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

    public boolean isRunning() {
        return running;
    }

    public void stopRunning() {
        keepRunning = false;
    }
}
