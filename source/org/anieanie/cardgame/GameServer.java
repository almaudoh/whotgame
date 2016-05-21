/*
 * GameServer.java
 *
 * Created on February 20, 2005, 1:07 AM
 */

package org.anieanie.cardgame;

import java.net.*;
import java.io.*;
import java.util.Hashtable;
import org.anieanie.cardgame.GameEnvironment.GameMonitor;

/**
 *
 * @author  ALMAUDOH
 */
public class GameServer {
    static GameMonitor gameMon = null;
    static Hashtable<String, GameWorker> workers;
    static Hashtable<String, Socket> users;
    static ServerSocket ss;
    public static void main(String []args) {
        try {
            // Create a socket on server
            ss = new ServerSocket(5550);
            
            // Create new GameMonitor object
            gameMon = null; //new GameEnvironment().getGameMonitor();
            
            // hashtable to manage list of sockets and workers
            workers = new Hashtable(10);
            users = new Hashtable(10);
            
            System.out.println("Server running on port " + ss.getLocalPort());
            // ---------------------------------------------------------------
            // Now start accepting connections from clients in a while loop
            // The server should run in an infinite loop
            while(true) {
                Socket socket = ss.accept();	// accept connection from client
                System.out.println("A new client is connected.");
                
                // to get data to and from server
                InputStream in = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                OutputStream out = socket.getOutputStream();
                PrintWriter pr = new PrintWriter(out, true);
                
                // read user name from the client and store in table
                // in the format username + socket
                String strUserName = br.readLine();
                System.out.println("Username: " + strUserName + "\n");
                users.put(strUserName, socket);
                
                // create a thread to allow simultaneous connections
                // There is need to check workers on a continual basis to ensure that their clients are still there
                // disconnected workers should be discarded
                GameWorker w = new GameWorker(socket, gameMon, strUserName);
                workers.put(strUserName, w);
                w.start();
            }	// End of while
            
        }	// End of try
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("Some kind of error has occurred.");
        }	// End of exception
        
    }	// End of main()
    
    // Override finalize() to close server socket
    protected void finalize() {
        if (workers != null) {
            for (GameWorker w : workers.values()) {
                try {
                    w.finalize();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            workers = null;
        }
        if (users != null) {
            for (Socket s : users.values()) {
                try {
                    s.close();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
            users = null;
        }
        if (ss != null) {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ss = null;
        }
    }
}
