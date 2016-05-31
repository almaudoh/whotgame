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
    
    /**
     * Creates a new instance of AbstractGameClient
     */
    public AbstractGameClient(ClientCGMPRelay relay, String name) {
        this.relay = relay;
        this.name = name;
    }
    
    /**
     * Creates a new instance of AbstractGameClient
     */
    public AbstractGameClient(ClientCGMPRelay relay) {
        /** @todo Generate random user name if function call returns null */
        this.relay = relay;
        this.name = getUserName();
    }
    
    public void connect() throws GameClientException, IOException {
        // Initial handshake with the Game Server.
        InputStream in = relay.getSocket().getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        OutputStream out = relay.getSocket().getOutputStream();
        PrintWriter pr = new PrintWriter(out, true);

        // Handshake: Send user name to the server and get acknowledgement.
        pr.println(this.name);

        // Carry out interaction between client and server
        String response = br.readLine();

        if (!response.equals(CGMPSpecification.ACK)) {
            throw new GameClientException(String.format("Server not responding on ip address %s and port %s",
                    relay.getSocket().getInetAddress(), relay.getSocket().getPort()));
        }
    }

    public void close() {

    }
    
    protected abstract void run();
    
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
