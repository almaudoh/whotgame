package org.anieanie.cardgame.agent;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.whot.WhotGameClient;
import org.anieanie.cardgame.gameplay.whot.WhotGameMonitor;
import org.anieanie.cardgame.ui.Display;

/**
 * This Game agent follows simple WhotGame playing rules without using any strategy.
 */
public class SimpleWhotGameAgent implements GameAgent {

    private final GameClient gameClient;
    private final Display display;

    public SimpleWhotGameAgent(GameClient gameClient, Display display) {
        this.gameClient = gameClient;
        this.display = display;
    }

    // Wait for input on a separate thread
    public void run() {
        // [optional] Keep trying to start a game.
        while (gameClient.getClientStatus() == GameClient.STATUS_WAITING_TO_START) {
            gameClient.startGame();
            display.showNotification("Game start requested");
            threadSleep(100);
        }

        // Now we have a game started, so proceed.
        // This loop continues until there is a winner as announced by the server or you resign.
        String cardSpec = "";
        while (gameClient.getClientStatus() != GameClient.STATUS_GAME_WON) {
            // Play allowed only if it is our turn.
            if (gameClient.getClientStatus() == GameClient.STATUS_WAITING_FOR_USER) {
                // Input loop for getting the move to be played.
                display.showGameStatus(gameClient);
                playMove();
            }
            threadSleep(100);
        }
    }

    protected void playMove() {
        GameEnvironment environment = gameClient.getEnvironment();
        // First check if awaiting whot 20 shape to call.
        WhotGameClient client = (WhotGameClient)gameClient;
        if (client.isAwaitingWhotCallInfo()) {
            client.sendWhotCallShape(getBestWhotCallShape());
        }
        else {
            gameClient.playMove(getMoveFromEnvironment(environment));
        }
    }

    /** Using simple rules, choose which card to play. */
    protected String getMoveFromEnvironment(GameEnvironment environment) {
        CardSet cards;
        WhotCard topCard = WhotCard.fromString(environment.get("TopCard"));
        if (topCard != null) {
            // Different playing compulsions.
            if (environment.get("MarketMode").equals("PickTwo")) {
                if (gameClient.getCards().containsLabel(WhotGameMonitor.PICK_TWO_LABEL)) {
                    return gameClient.getCards().containingLabel(WhotGameMonitor.PICK_TWO_LABEL).getFirst().toString();
                }
            }
            else if (environment.get("MarketMode").equals("General")) {
                return "MARKET";
            }
            else if (topCard.getShape() == WhotCard.WHOT) {
                // If whot 20 is played, then look at the called card.
                cards = gameClient.getCards().containingShape(WhotCard.getShapeInt(environment.get("CalledCard")));
                cards.shuffle();
                if (cards.size() > 0) {
                    return cards.getFirst().toString();
                }
            }
            else {
                // Otherwise, choose a random card that matches the rule.
                cards = gameClient.getCards().containingShape(topCard.getShape());
                cards.addAll(gameClient.getCards().containingLabel(topCard.getLabel()));
                if (cards.size() > 0) {
                    cards.shuffle();
                    return cards.getFirst().toString();
                }
            }
        }
        // If we reach here, then we don't have a card to play.
        return "MARKET";
    }

    protected String getBestWhotCallShape() {
        // Get the best shape to call after whot 20 is played.
        // Simple strategy is to find the shape with most cards.
        int[] weights = new int[WhotCard.N_SHAPES];
        int weight, maxWeight = 0, maxShape = 0;
        for (Card card : gameClient.getCards()) {
            weight = weights[card.getShape()] + 1;
            if (weight > maxWeight) {
                maxWeight = weight;
                maxShape = card.getShape();
            }
            weights[card.getShape()] = weight;
        }
        return WhotCard.SHAPES.get(maxShape);
    }

    // Convenience function for thread sleep delays.
    private void threadSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
