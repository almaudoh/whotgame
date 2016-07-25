/*
 * WhotGameClient.java
 *
 * Created on February 24, 2005, 12:29 AM
 */

package org.anieanie.cardgame.gameplay.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.gameplay.AbstractGameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.ui.Display;

import java.io.IOException;

/**
 *
 * @author  ALMAUDOH
 */
public class WhotGameClient extends AbstractGameClient {
    private boolean awaitingWhotCallInfo = false;

    /**
     * Creates a new instance of GameLauncher
     */
    public WhotGameClient(ClientCGMPRelay relay, Display display) {
        super(relay, display);
        relay.setListener(this);
    }

    /**
     * Handles communication with server.
     *
     * Once this method exits, the client will be cleaned up and terminated
     */
    public void run() {
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

            // Start the thread that runs the game agent.
            new Thread(agent, "GameAgent:" + agent.getName()).start();
            
            // ... while current thread scans server and updates client asynchronously
            while (clientStatus != STATUS_GAME_WON && clientStatus != STATUS_TERMINATE) {
                try {
                    relay.scanWithThreadSleep();
                }
                catch (InterruptedException i) {
                    i.printStackTrace();
                }
                catch (CGMPConnectionException e) {
                    // We may not always have a response from scans.
                }
                catch (NullPointerException e) {
                    // The relay has most likely been nulled. End game.
                    clientStatus = STATUS_TERMINATE;
                }
            }

            // Interrupt agents input loop so it can recheck the client's status.
            agent.refresh();
            
        }
        catch(Exception e) {
            System.out.println("Some kind of error has occurred.");
            e.printStackTrace();
        }	// End of exception
    }

    public Card getTopCard() {
        String topCard = environment.get(WhotGameRule.VAR_TOP_CARD);
        if (topCard != null) {
            return WhotCard.fromString(topCard);
        }
        else {
            return null;
        }
    }

    public String getCalledCard() {
        return environment.get(WhotGameRule.VAR_CALLED_CARD);
    }

    @Override
    public void playMove(String moveSpec) {
        if (moveSpec.equalsIgnoreCase(WhotGameRule.GO_MARKET)) {
            requestCard();
        }
        else {
            if (!WhotCard.isIllegalCardSpec(moveSpec)) {
                // Just to ensure valid card is created.
                playCard(WhotCard.fromString(moveSpec));
            }
            else {
                throw new IllegalArgumentException("Illegal card specification");
            }
        }
    }

    @Override
    public void playMove(Card card) {
        playCard(card);
    }

    private void playCard(Card card) {
        // User cannot play a card he doesn't have.
        if (!cards.contains(card)) {
            agent.moveRejected(card);
            display.showNotification("You don't have '" + card + "'.");
            return;
        }

        // Send card to server and wait for positive acknowledgement.
        try {
            if (relay.sendCard(card.toString())) {
                // Log the move before the cardset is updated.
                logMove(card.toString());
                // Card was successfully played.
                clientStatus = STATUS_WAITING_FOR_TURN;
                cards.remove(card);
                agent.moveAccepted(card);
                display.showNotification("Played " + card);
            }
            else {
                agent.moveRejected(card);
                display.showNotification("Card '" + card + "' was rejected. Please play another card.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (CGMPException e) {
            // Do a double check to confirm that the server got the card and acted on it.
            try {
                GameEnvironment env = relay.requestEnvironment();
                if (env.get("TopCard").equals(card.toString())) {
                    // Log the move before the cardset is updated.
                    logMove(card.toString());
                    // Card was successfully played.
                    clientStatus = STATUS_WAITING_FOR_TURN;
                    cards.remove(card);
                    display.showNotification("Played " + card);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (CGMPException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void requestCard() {
        try {
            CGMPMessage response = relay.sendRequest(CGMPSpecification.CARD);
            if (response.isCard()) {
                // Log the move before the cardset is updated.
                logMove(WhotGameRule.GO_MARKET);
                // Cards were successfully received, add them to the card pack
                for (String spec: response.getArguments().split(";")) {
                    cards.add(WhotCard.fromString(spec.trim()));
                }
                clientStatus = STATUS_WAITING_FOR_TURN;
                display.showNotification("Received: " + response.getArguments().replace(";", "  "));
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
     * These are methods for reacting to requests from the game server / worker.
     */

    @Override
    public void cardReceived(String cardSpec) {
        super.cardReceived(cardSpec);
        // add cards to card pack
        for (String spec: cardSpec.split(";")) {
            cards.add(WhotCard.fromString(spec.trim()));
        }
    }

    @Override
    /** Called when client CGMPRelay receives information from worker CGMPRelay */
    public void infoReceived(String info) {
        // @todo This needs to be refactored to the cli reader.
        // Information received and it's a request to call shape.
//        if (info.equals("CALL") && this.getTopCard().getShape() == WhotCard.WHOT) {
        if (info.equals("CALL")) {
            awaitingWhotCallInfo = true;
            display.showNotification("Call requested");
        }
        else {
            display.showNotification(info);
        }
    }

    public void sendWhotCallShape(String shape) {
        try {
            relay.sendInformation("CALL " + shape);
            awaitingWhotCallInfo = false;
            display.showNotification("Shape " + shape + " called");
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logMove(String move) {
        if (logger != null) {
            logger.logMove(move, environment, cards);
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
