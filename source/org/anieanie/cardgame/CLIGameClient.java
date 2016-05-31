/*
 * CLIGameClient.java
 *
 * Created on February 24, 2005, 12:29 AM
 */

package org.anieanie.cardgame;

import org.anieanie.cardgame.cgmp.*;
import org.anieanie.card.AbstractCard;
import org.anieanie.card.CardSet;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 *
 * @author  ALMAUDOH
 */
public class CLIGameClient extends AbstractGameClient {
    private CardSet cards;
    private int clientStatus = -1;
    private String gameWinner;
    
    /* Possible values for status
     * -1 - Undefined
     *  0 - Waiting to start game
     *  1 - Waiting for turn
     *  2 - Waiting for user input (action)
     *  3 - Game has been won
     * 10 - Terminate Game
     */
    
    /**
     * Creates a new instance of CLIGameClient
     */
    public CLIGameClient(ClientCGMPRelay relay, String name) {
        super(relay, name);
        cards = new CardSet();
    }
    
    /**
     * Creates a new instance of CLIGameClient
     */
    public CLIGameClient(ClientCGMPRelay relay) {
        super(relay);
        cards = new CardSet();
    }
    
    public static void main(String [] args) {
        try {
            // create a new socket
            /** @todo Update this line to search for unused ports in case DEFAULT_PORT  is used already */
            int port = CGMPSpecification.Connection.DEFAULT_PORT;
            String ip = CGMPSpecification.Connection.DEFAULT_IP;
            Socket socket = new Socket(ip, port);
            ClientCGMPRelay relay = new ClientCGMPRelay(socket);
            CLIGameClient client = new CLIGameClient(relay);
            relay.setListener(client);

            // Connect to the client.
            client.connect();
            client.run();
        }
        catch (ConnectException e) {
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
    
    /**
     * Handles communication with server.
     * Once this method exits, the client will be cleaned up and terminated
     *
     */
    protected void run() {
        try {
            // Immediately, the client asks the server for permission to play
            int count = 0;
            while (true) {
                if (relay.requestPlay()) break;
                else if (count++ > 10) {
                    System.out.println("Server did not grant request to play");
                    return;
                }
            }
            
            clientStatus = 0;
            
            // This thread to wait for user input
            Thread t = new Thread(new CommandLineReader());
            t.start();
            
            // ... while current thread scans server and updates client asynchronously
            while (clientStatus != 3 && clientStatus != 10) {
                try {
                    relay.scan();
                    Thread.sleep(50);
                } catch (InterruptedException i) {
                    i.printStackTrace();
                }
            }
            
        }	// End of try
        catch(Exception e) {
            System.out.println("Some kind of error has occurred.");
            //relay.terminateRelay();
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
    }
    
    private final void info(String msg) {
        System.out.println("Message: "+msg);
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
        switch (actioncode) {
            // (1) To view game status.\n" +
            case 1:
                System.out.println("Requesting game status");
                break;
                // (2) To view your hand.\n" +
            case 2:
                System.out.println("Requesting my hand");
                break;
                // (3) To play a card.\n" +
            case 3:
                System.out.println("Playing a card");
                break;
                // (4) To pick market.    " );
            case 4:
                System.out.println("Requesting a card");
                break;
            default:
        }
        
    }
    
    /*
     * Methods implemented by interface ClientCGMPRelayListener
     */
    
    /** Called when the client CGMPRelay receives card from the worker CGMPRelay */
    public boolean cardReceived(String cardSpec) {
        try {
            relay.sendAcknowledgement();
        }
        catch (CGMPException ex) {
            ex.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        clientStatus = 1;  // Now waiting for server to ask for move
        // add card to card pack
        cards.add(AbstractCard.fromString(cardSpec));
        System.out.println("card received: " + cardSpec);
        return true;
    }
    
    /** Called when client CGMPRelay receives environment from worker CGMPRelay */
    public void envReceived(String envSpec) {
        System.out.println("environment received: " + envSpec);
    }
    
    
    public void errorReceived(int errorcode) {
        System.out.println("error received: " + errorcode);
    }
    
    
    public void gameWon(String winner) {
        System.out.println("game won");
    }
    
    /** Called when client CGMPRelay receives confirmation of validity of move from client CGMPRelay */
    public void moveAccepted(String moveSpec) {
        System.out.println("move accepted: " + moveSpec);
    }
    
    /** Called when client CGMPRelay receives request for move from worker CGMPRelay */
    public Object moveRequested() {
        clientStatus = 2;  // Now waiting for user to make move
        System.out.println("move requested");
        return null;
    }
    
    
    /** Called when the client or server is terminated */
    public boolean relayTerminated() {
        try {
            // Now, close the socket after deleting that socket from online list
            //        Socket s = (Socket)tOnlineUsers.remove(getUserName());
            //        tOfflineUsers.put(getUserName(), s);
            System.out.println("relay terminated, cleanup needed");
            //relay = null;
            System.out.println("finalizing worker");
            //this.finalize();
        } catch (Exception e) {} catch (Throwable t) {}
        return true;
    }
    
    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        super.finalize();
    }
    
    private boolean playCard() {
        
        return false;
    }
    
    private class CommandLineReader implements Runnable {
        // Wait for input on a separate thread
        public void run() {
            // ---------------------------------
            int action = 0;	// action to be carried out
            String strInput="z";
            String quit = "#";
            String chmod = "@";
            String rsp = "";
            
            // Wait for server to signal start of game
            info("Waiting for server to deal cards");
            while (clientStatus != 1) try {
                Thread.sleep(50);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            
            /*
             * Main game control while loop
             * loop until there is a winner as announced by the server
             * or you resign.
             */
            while (clientStatus !=3 && strInput.charAt(0) != '#') {
                // Wait for your turn
                info("Waiting for your turn to play");
                while (clientStatus != 2) try {
                    Thread.sleep(50);
                }
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                
                if (clientStatus == 2) {
                    // Time to play
                    info("Your turn to play");
                    
                    // give user a menu
                    System.out.println(
                          "\n\nEnter (#) to disconnect from server.\n"
                        + "      (1) To view game status.\n"
                        + "      (2) To view your hand.\n"
                        + "      (3) To play a card.\n"
                        + "      (4) To pick market."
                    );
                    
                    // The while loop for getting input
                    // --------------------------------
                    while (strInput.charAt(0) != '#') {
                        try {
                            strInput = input.readLine();
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        
                        if (strInput.equals("")) {
                            strInput = "z";
                        }
                        
                        switch(strInput.charAt(0)) {
                            case '#':
                                break;
                            case '@':
//                                action = 1 - action;
//                                System.out.println("Mode changed, new mode is " + action);
                                break;
                            case '0':
                                //doAction(Character.digit(strInput.charAt(0), 10)); break;
                            case '1': // To view game status
                                // Yet to be implemented
                                break;
                            case '2': // To view your hand
                                System.out.println(cards + " (" + cards.size() + ")");
                                break;
                            case '3': // To play a card
                                playCard();
                                break;
                            case '4': // To pick market
                                
                                break;
                            case '\uD400':
                                /*switch (action) {
                                    case 0: // Raw action
                                        pr.println(strInput);
                                        // wait for reply
                                        // process reply
                                        int t=0;
                                        while (!br.ready() && t++ < CGMPSpecification.READ_TIMEOUT*10) { System.out.print("."); Thread.sleep(50); }
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
                                }*/
                                break;
                            default:	// send expression to the server
                        }
                        
                        try {
                            // Allow other threads to run
                            Thread.sleep(50);
                        }
                        catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }	// End of the while loop
                }
            }   // End of outer while loop
            
            relay.terminateRelay();
            
        }
        
    }
}	// End of class}
