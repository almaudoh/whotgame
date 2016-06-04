/*
 * CLIGameClient.java
 *
 * Created on February 24, 2005, 12:29 AM
 */

package org.anieanie.cardgame;

import org.anieanie.card.AbstractCard;
import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.utils.CommandLineReader;
import org.anieanie.cardgame.utils.Debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

/**
 *
 * @author  ALMAUDOH
 */
public class CLIGameClient extends AbstractGameClient {

    private CardSet cards;
    protected static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    private Card topCard;

    @Override
    public String getUsername() {
        return this.name;
    }

    /**
     * Creates a new instance of CLIGameClient
     */
    public CLIGameClient(ClientCGMPRelay relay, String name) {
        super(relay, name);
        cards = new WhotCardSet();
    }

    public static void main(String [] args) {
        try {
            // create a new socket
            /** @todo Update this line to search for unused ports in case DEFAULT_PORT  is used already */
            int port = CGMPSpecification.Connection.DEFAULT_PORT;
            String ip = CGMPSpecification.Connection.DEFAULT_IP;
            Socket socket = new Socket(ip, port);

            ClientCGMPRelay relay = new ClientCGMPRelay(socket);
            CLIGameClient client = new CLIGameClient(relay, inputUserName());
            relay.setListener(client);
            relay.addLowLevelListener(Debugger.getLowLevelListener(client.getUsername()));

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
     *
     * Once this method exits, the client will be cleaned up and terminated
     */
    protected void run() {
        try {
            // Immediately, the client asks the server for permission to play
            int count = 0;
            while (true) {
                if (relay.requestPlay()) break;
                else if (count++ > 20) {
                    System.out.println("Server did not grant request to play");
                    return;
                }
            }
            
            clientStatus = 0;
            
            // This thread to wait for user input and play the game.
            Thread t = new Thread(new CommandLineReader(this), "CLI-Thread");
            t.start();
            
            // ... while current thread scans server and updates client asynchronously
            while (clientStatus != STATUS_GAME_WON && clientStatus != STATUS_TERMINATE) {
                try {
                    relay.scan();
                    Thread.sleep(500);
                }
                catch (InterruptedException i) {
                    i.printStackTrace();
                }
                catch(CGMPConnectionException e) {
                    // We may not always have a response from scans.
                }
            }
            
        }
        catch(Exception e) {
            System.out.println("Some kind of error has occurred.");
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
    }

    private static String inputUserName() {
        String strUserName = "";	// User name of client
        try {
            do  {
                System.out.print("Enter User Name: ");
                strUserName = input.readLine();
            } while (strUserName.equals(""));
            return strUserName;
        }
        catch (IOException ioe) {
            // do something positive here
            return null;
        }
    }

    @Override
    public CardSet getCards() {
        return cards;
    }


    public Card getTopCard() {
        return topCard;
    }

    @Override
    public void playCard(String cardspec) {
        Card card = AbstractCard.fromString(cardspec);
//        relay.sendCard(card);
    }

    @Override
    public void startGame() {
        try {
            CGMPMessage response = relay.sendRequest(CGMPSpecification.START);
            clientStatus = STATUS_WAITING_FOR_TURN;  // Now waiting for server to ask for move
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestCard() {
        try {
            CGMPMessage response = relay.sendRequest(CGMPSpecification.CARD);
            if (response.isCard()) {
                cards.add(WhotCard.fromString(response.getArguments()));
            }
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Methods implemented by interface ClientCGMPRelayListener
     */
    
    /** Called when the client CGMPRelay receives card from the worker CGMPRelay */
    public void cardReceived(String cardSpec) {
        try {
            relay.sendAcknowledgement();
        }
        catch (CGMPException ex) {
            ex.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        clientStatus = STATUS_WAITING_FOR_TURN;  // Now waiting for server to ask for move.
        // add card to card pack
        cards.add(WhotCard.fromString(cardSpec));
    }
    
    /** Called when client CGMPRelay receives environment from worker CGMPRelay */
    public void envReceived(String envSpec) {
        System.out.println("environment received: " + envSpec);
    }
    
    public void gameWon(String winner) {
        System.out.println("game won");
    }
    
    /** Called when client CGMPRelay receives confirmation of validity of move from client CGMPRelay */
    public void moveAccepted(String moveSpec) {
        System.out.println("move accepted: " + moveSpec);
    }
    
    /** Called when client CGMPRelay receives request for move from worker CGMPRelay */
    public void moveRequested() {
        clientStatus = STATUS_WAITING_FOR_USER;  // Now waiting for user to make move
    }
    
    
    /** Called when the client or server is terminated */
    public void relayTerminated() {
        try {
            // Now, close the socket after deleting that socket from online list
            //        Socket s = (Socket)tOnlineUsers.remove(getUsername());
            //        tOfflineUsers.put(getUsername(), s);
            System.out.println("relay terminated, cleanup needed");
            //relay = null;
            System.out.println("finalizing worker");
            //this.finalize();
        }
        catch (Exception e) {

        }
        catch (Throwable t) {

        }
    }

    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        super.finalize();
    }
    
    private boolean playCard() {
        
        return false;
    }

}
