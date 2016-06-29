/*
 * TestGameClient.java
 *
 * Created on February 24, 2005, 12:29 AM
 */

package org.anieanie.cardgame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.anieanie.card.CardSet;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.gameplay.AbstractGameClient;
import org.anieanie.cardgame.gameplay.GameClientException;
import org.anieanie.cardgame.ui.Display;

/**
 *
 * @author  ALMAUDOH
 */
public class TestGameClient extends AbstractGameClient {

    protected BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Creates a new instance of TestGameClient
     */
    public TestGameClient(ClientCGMPRelay relay, String name, Display display) {
        super(relay, name, display);
    }
    
    /**
     * Creates a new instance of TestGameClient
     */
    public TestGameClient(ClientCGMPRelay relay) {
        super(relay);
    }

    @Override
    public CardSet getCards() {
        return null;
    }

    @Override
    public void startGame() {

    }

    @Override
    public void playMove(String cardSpec) {

    }

    @Override
    public int getClientStatus() {
        return 0;
    }

    public static void main(String [] args) {
        try {
            int port = CGMPSpecification.Connection.DEFAULT_PORT;
            String ip = CGMPSpecification.Connection.DEFAULT_IP;
            Socket socket = null;
            socket = new Socket(ip, port);
            TestClientCGMPRelay relay = new TestClientCGMPRelay(socket);
            TestGameClient client = new TestGameClient(relay);
            relay.setListener(client);
            client.connect();
            client.run();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (GameClientException e) {
            e.printStackTrace();
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void run() {
        try {
            // Declarations to manage connection
            // ---------------------------------
            int mode = 0;	// Communication mode, 0 = direct, 1 = Thru ClientCGMPRelayListener
            String strInput="z";
            String quit = "#";
            String chmod = "@";
            String rsp = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(relay.getSocket().getInputStream()));
            PrintWriter pr = new PrintWriter(relay.getSocket().getOutputStream());
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
                                while (!br.ready() && t++ < CGMPSpecification.READ_TIMEOUT * 10) {
                                    System.out.print(".");
                                    Thread.sleep(100);
                                }
                                System.out.println("finished waiting: " + t + " rounds");
                                if (br.ready()) {
                                    rsp = br.readLine();
                                    System.out.println(rsp);
                                }
                                break;
                            default: // Use ClientCGMPRelayListener object
                                try {
                                    ((TestClientCGMPRelay)relay).sendMessage(CGMPMessage.fromString(strInput), false);
                                }
                                catch (CGMPException ce) {
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
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
    }

    @Override
    public String getUsername() {
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
    @Override
    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        super.finalize();
    }

    @Override
    public void moveRequested() {
    }

    @Override
    public void environmentReceived(String envSpec) {
    }

    @Override
    public void cardReceived(String cardSpec) {

    }

    @Override
    public void gameWon(String winner) {
    }

    @Override
    public void relayTerminated() {

    }

    @Override
    public void errorReceived(int errorcode) {
    }

    @Override
    public void infoReceived(String info) {
    }

}
