/*
 * AbstractGameClient.java
 *
 * Created on March 31, 2007, 5:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.anieanie.cardgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import org.anieanie.cardgame.cgmp.ClientCGMPRelay;
import org.anieanie.cardgame.cgmp.ClientCGMPRelayListener;
import org.anieanie.cardgame.cgmp.CGMPSpecification;

/**
 *
 * @author Aniebiet
 */
public abstract class AbstractGameClient implements ClientCGMPRelayListener {
    
    protected ClientCGMPRelay relay;
    protected String name;
    protected static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    
    /** Creates a new instance of AbstractGameClient */
    public AbstractGameClient(String name) {
        this.name = name;
    }
    
    /** Creates a new instance of AbstractGameClient */
    public AbstractGameClient() {
        /** @todo Generate random user name if function call returns null */
        this.name = getUserName();
    }
    
    public void connect(String ip, int port) {
        // Declarations to get input from keyboard
        // ---------------------------------------
        /** @todo Update this line to search for unused ports in case DEFAULT_PORT  is used already */
        if (port<=1024) port = CGMPSpecification.Connection.DEFAULT_PORT;       // server port
        if (ip=="" || ip==null) ip = CGMPSpecification.Connection.DEFAULT_IP;   // IP of server
        
        try {
            // create a new socket
            Socket socket = new Socket(ip, port);
            relay = new ClientCGMPRelay(socket, this);
            
            // Connection successful at this point, so inform user about this
            System.out.print("\n\n\t\tConnection successful.\n\t\t----------------------");
            System.out.print("\n\t\tClient connected on port "+port+" to server on ip "+ip+"\n");
            
            // to get data to and from server
            InputStream in = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            OutputStream out = socket.getOutputStream();
            PrintWriter pr = new PrintWriter(out, true);
            
            // send user name to the server
            pr.println(this.name);
            
            // Carry out interaction between client and server
            run(br, pr);
            
            // At this point client wants to disconnect from the server,
            // so close the connection
            //socket.close();
            
            // At this point, the relay should also clean up
            
        } catch (ConnectException e) {
            System.out.println("I could not connect to the server.");
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
        catch (Exception e) {
            System.out.println("Some kind of error has occurred.");
            e.printStackTrace();
            System.exit(0);
        }	// End of exception	
    }
    
    protected abstract void run(BufferedReader br, PrintWriter pr);
    
    // Later I may implement this to generate a random name
    protected abstract String getUserName(); 
    
    /**
     * public GameClient(Socket sock, String name) {
     * relay = new ClientCGMPRelay(sock, this);
     * this.name = name;
     * }
     *
     * public GameClient(String ip, int port, String name) {
     * try {
     * // instructions
     * //            System.out.println("Instructions to connect to the server.\n\n" +
     * //            "-> If the server is running on the same computer," +
     * //            "just press enter key or enter \"127.0.0.1\".\n\n" +
     * //            "-> Do not enter anything when it asks for port unless" +
     * //            "you don't edit the code in Server.java and edit it." +
     * //            "Just leave it blank by pressing the enter key.\n\n" +
     * //            "-> Enter the UserName of your choice. It can be you own name.\n");
     *
     * // get IP from the user
     * //            System.out.print("\n\nEnter IP of the server: ");
     * //            ip = input.readLine();
     * //            if (ip.equals(""))
     *
     * // get port from user
     * //            System.out.print("Port Number: ");
     * //            strPort = input.readLine();
     * //            if (strPort.equals(""))
     * //            else
     * //                port = Integer.parseInt(strPort);
     *
     * // --------------------------------------------------------------
     * // IP, port and username is complete at this point
     * // Now, create a socket to connect to server.
     * // After that manage the connection in a while loop
     * // until user wants to exit on his/her will
     * // --------------------------------------------------------------
     *
     * // create a new socket
     * Socket socket = new Socket(ip, port);
     * relay = new ClientCGMPRelay(socket, this);
     * this.name = name;
     * }
     * catch (IOException ioe) {
     *
     * }
     *
     * }*/
    
}

