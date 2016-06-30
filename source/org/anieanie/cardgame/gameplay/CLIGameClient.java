/*
 * CLIGameClient.java
 *
 * Created on February 24, 2005, 12:29 AM
 */

package org.anieanie.cardgame.gameplay;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.ui.cli.CommandLineDisplay;
import org.anieanie.cardgame.ui.cli.CommandLineReader;
import org.anieanie.cardgame.ui.Display;
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
    private boolean awaitingWhotCallInfo = false;

    @Override
    public String getUsername() {
        return this.name;
    }

    /**
     * Creates a new instance of CLIGameClient
     */
    public CLIGameClient(ClientCGMPRelay relay, String name, Display display) {
        super(relay, name, display);
        cards = new WhotCardSet();
    }

    public static void main(String [] args) {
        Display display = new CommandLineDisplay();
        try {
            // create a new socket
            /** @todo Update this line to search for unused ports in case DEFAULT_PORT  is used already */
            int port = CGMPSpecification.Connection.DEFAULT_PORT;
            String ip = CGMPSpecification.Connection.DEFAULT_IP;
            Socket socket = new Socket(ip, port);

            ClientCGMPRelay relay = new ClientCGMPRelay(socket);
            CLIGameClient client = new CLIGameClient(relay, inputUserName(), display);
            relay.setListener(client);
            relay.addLowLevelListener(Debugger.getLowLevelListener(client.getUsername()));

            // Connect to the client.
            client.connect();
            client.run();
        }
        catch (ConnectException e) {
            display.showNotification("I could not connect to the server.");
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
        catch (Exception e) {
            display.showNotification("Some kind of error has occurred.");
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
                    display.showNotification("Server did not grant request to play");
                    return;
                }
                // Free CPU cycles.
                Thread.sleep(100);
            }
            
            clientStatus = 0;
            
            // This thread to wait for user input and play the whot.
            Thread t = new Thread(new CommandLineReader(this, display), "CLI-Thread");
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
        String topCard = environment.get("TopCard");
        if (topCard != null) {
            return WhotCard.fromString(topCard);
        }
        else {
            return null;
        }
    }

    public String getCalledCard() {
        return environment.get("CalledCard");
    }

    /**
     *********************************************************************************************
     *********************************************************************************************
     *                                                                                          **
     *    Game play sequence.                                                                   **
     *                                                                                          **
     *    1. startGame() - requests whot server to start the whot. Game server will not         **
     *       start unless there are at least two players in the whot area.                      **
     *    2. When whot is started, the whot server                                              **
     *       - shuffles and distributes cards to all players - callback cardReceived() invoked  **
     *       - sends the current whot gameplay state      - callback environmentReceived() invoked   **
     *       - informs each player by turn to play           - callback moveRequested() invoked **
     *    3. playCard() - the player whose turn it is to play sends a card to the whot server   **
     *       and waits for next turn if the card is accepted. If the card is rejected, another  **
     *       card should be played or a new card requested.                                     **
     *    4. requestCard() - the player whose turn it is to play requests a card from the       **
     *       spare stack (market) if he doesn't have the right card to play.                    **
     *                                                                                          **
     *                                                                                          **
     *********************************************************************************************
     *********************************************************************************************
     */

    /*
     *  These are methods for initiating moves by the Game Client.
     */

    @Override
    public void startGame() {
        try {
            relay.sendRequest(CGMPSpecification.START);
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
    public void playMove(String moveSpec) {
        if (moveSpec.equalsIgnoreCase("MARKET")) {
            requestCard();
        }
        else {
            playCard(moveSpec);
        }
    }

    private void playCard(String cardspec) {
        Card card;
        if (!WhotCard.isIllegalCardSpec(cardspec)) {
            // Just to ensure valid card is created.
            card = WhotCard.fromString(cardspec);
        }
        else {
            throw new IllegalArgumentException("Illegal card specification");
        }

        // User cannot play a card he doesn't have.
        if (!cards.contains(card)) {
            display.showNotification("You don't have '" + cardspec + "'.");
            return;
        }

        // Send card to server and wait for positive acknowledgement.
        try {
            if (relay.sendCard(cardspec)) {
                // Card was successfully played.
                clientStatus = STATUS_WAITING_FOR_TURN;
                cards.remove(card);
            }
            else {
                display.showNotification("Card '" + cardspec + "' was rejected. Please play another card.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    private void requestCard() {
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
     * Methods implemented by interface ClientCGMPRelayListener.
     *
     * These are methods for reacting to requests from the whot server / worker.
     */

    @Override
    public void cardReceived(String cardSpec) {
        super.cardReceived(cardSpec);
        // add card to card pack
        for (String spec: cardSpec.split(";")) {
            cards.add(WhotCard.fromString(spec.trim()));
        }
    }

    @Override
    /** Called when client CGMPRelay receives information from worker CGMPRelay */
    public void infoReceived(String info) {
        // @todo This needs to be refactored to the cli reader.
        // Information received and it's a request to call shape.
        if (info.equals("CALL") && this.getTopCard().getShape() == WhotCard.WHOT) {
            awaitingWhotCallInfo = true;
        }
        else {
            display.showNotification(info);
        }
    }


    public void sendWhotCallShape(String shape) {
        try {
            relay.sendInformation("CALL " + shape);
            awaitingWhotCallInfo = false;
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void finalize() throws Throwable {
        System.out.println("Finalize called!");
        super.finalize();
    }

    public boolean isAwaitingWhotCallInfo() {
        return awaitingWhotCallInfo;
    }
}
