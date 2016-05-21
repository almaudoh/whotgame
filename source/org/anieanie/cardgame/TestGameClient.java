/*
 * TestGameClient.java
 *
 * Created on February 24, 2005, 12:29 AM
 */

package org.anieanie.cardgame;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import org.anieanie.cardgame.cgmp.*;

/**
 *
 * @author  ALMAUDOH
 */
public class TestGameClient extends AbstractGameClient {
    protected TestClientCGMPRelay relay;
    
    /**
     * Creates a new instance of TestGameClient
     */
    public TestGameClient(String name) {
        super(name);
    }
    
    /**
     * Creates a new instance of TestGameClient
     */
    public TestGameClient() {
        super();
    }
    
    public static void main(String [] args) {
        // Get ip, port & user name from the client
        String strPort="";	// server port
        TestGameClient client = new TestGameClient();
        client.connect("",0);
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
            relay = new TestClientCGMPRelay(socket, this);
            
            // Connection successful at this point, so inform user about this
            System.out.print("\n\n\t\tConnection successful.\n\t\t----------------------");
            System.out.print("\n\t\tClient connected on port "+port+" to server on ip "+ip);
            
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
            socket.close();
            
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
    
    protected void run(BufferedReader br, PrintWriter pr) {
        try {
            // Declarations to manage connection
            // ---------------------------------
            int mode = 0;	// Communication mode, 0 = direct, 1 = Thru ClientCGMPRelayListener
            String strInput="z";
            String quit = "#";
            String chmod = "@";
            String rsp = "";
            // The while loop
            // --------------
            while (strInput.charAt(0) != '#') {
                // giver user a menu
                System.out.println("\n\nEnter (#) to disconnect from server.\n" +
                        "      (@) to change mode.\n" +
                        "      or the message you wish to transmit.  ");
                strInput = input.readLine();
                if (strInput.equals(""))
                    strInput = "z";
                switch(strInput.charAt(0)) {
                    case '#':
                        //relay.terminateRelay();
                        break;
                    case '@':
                        mode = 1 - mode;
                        System.out.println("Mode changed, new mode is " + mode);
                        break;
                    default:	// send expression to the server
                        switch (mode) {
                            case 0: // Raw mode
                                pr.println(strInput);
                                // wait for reply
                                // while (!br.ready()) Thread.sleep(100);
                                // process reply
                                int t=0;
                                while (!br.ready() && t++ < CGMPSpecification.READ_TIMEOUT*10) { System.out.print("."); Thread.sleep(100); }
                                System.out.println("finished waiting: " + t + " rounds");
                                if (br.ready()) { rsp = br.readLine();
                                System.out.println(rsp); }
                                break;
                            default: // Use ClientCGMPRelayListener object
                                try {
                                    relay.sendMessage(strInput);
                                } catch (CGMPException ce) {
                                    System.out.println(ce.getMessage());
                                    //ce.printStackTrace();
                                }
                                //doAction(Integer.parseInt(strInput));
                                break;
                        }
                        break;
                }	// End of switch
            }	// End of the while loop
            
            relay.terminateRelay();
            
        }	// End of try
        catch(Exception e) {
            System.out.println("Some kind of error has occurred.");
            relay.terminateRelay();
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
    }
    
    protected String getUserName() {
        String strUserName = "";	// User name of client
        try {
            do  {
                System.out.print("Enter User Name: ");
                strUserName = input.readLine();
            } while (strUserName.equals(""));
            return strUserName;
        } catch (IOException ioe) {
            // do something positive here
            return null;
        }
    }
    
    public static void doAction(int actioncode) {
        
    }
    
    /*
     * Methods implemented by interface ClientCGMPRelayListener
     */
    
    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        super.finalize();
    }
    
    public Object moveRequested() {
        return null;
    }
    
    public void moveAccepted(String moveSpec) {
    }
    
    public void envReceived(String envSpec) {
    }
    
    public boolean cardReceived(String cardSpec) {
        return false;
    }
    
    public void gameWon() {
    }
    
    public boolean relayTerminated() {
        return false;
    }
    
    public void errorReceived(int errorcode) {
    }
    
    
}	// End of class}

