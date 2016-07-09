// The monitor class is the environment in which a card game is played
// It deals the cards and signifies to each player that it is its turn to play
// The monitor class also provides the interface that allows Watcher objects to watch
// the proceedings
package org.anieanie.cardgame.gameplay.whot;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.gameplay.AbstractGameMonitor;
import org.anieanie.cardgame.cgmp.CGMPException;

import java.io.IOException;
import java.lang.*;
import java.util.Arrays;
import java.util.Collections;

public class WhotGameMonitor extends AbstractGameMonitor {
    // The number that is used as the pick two number.
    public static final int PICK_TWO_LABEL = 7;

    // The number that is used as the general market number.
    public static final int GENERAL_MARKET_LABEL = 4;

    // The number that is used for suspension.
    public static final int SUSPENSION_LABEL = 8;

    // The number that is used as hold on.
    public static final int HOLD_ON_LABEL = 1;

    // Indicates that the monitor is waiting for a move from the client. Loop control variable.
    private volatile boolean waitingForMove = false;

    // Indicates that the monitor is waiting for a player to call a card (after Whot 20 is played).
    private volatile boolean waitingForCall = false;

    // Indicates that the monitor is waiting for a player to call a card (after Whot 20 is played).
    private int pickTwoCount = 0;

    // Flag for which player played the general market card. -1 if not played.
    private int generalMarketPlayer = -1;

    // Flag to identify that the game is in general market mode.
    private boolean isGeneralMarket = false;

    // Flag to identify that the next player is in suspension.
    private boolean isSuspensionCard = false;

    // Flag to identify that the HOLD ON card has been played.
    private boolean isHoldOnCard = false;

    // The card that is called by a player who played Whot 20.  `
    private String calledCard = "";

    // A statement that is used to inform all of the move that was made
    private String movePlayed = "";

    // constructors
    public WhotGameMonitor() {
        super();
    }

    protected void initCardDecks() {
        exposed = new WhotCardSet();
        dealed = new WhotCardSet();
        covered = new WhotCardSet();
    }

    protected void dealCards() {
        super.dealCards();
        // whot 20 should not be on top the first time.
        while (exposed.getFirst().getShape() ==  WhotCard.WHOT) {
            exposed.addFirst(covered.removeFirst());
        }
    }

    @Override
    public void updateEnvironment() {
        super.updateEnvironment();
        environment.put("CalledCard", calledCard);
        if (pickTwoCount > 0) {
            environment.put("MarketMode", "PickTwo");
        }
        else if (isGeneralMarket) {
            environment.put("MarketMode", "General");
        }
        else {
            environment.put("MarketMode", "Normal");
        }
    }

    @Override
    public boolean canPlayGame(String user) {
        // A user cannot join a whot that has already started or exceeds 4 players.
        return !gameStarted && players.size() <= 4;
    }

    @Override
    public boolean canViewGame(String user) {
        return true;
    }

    @Override
    public boolean canMakeMove(String user) {
        return user.equals(players.get(currentPlayer));
    }

    @Override
    public void nextMove() {
        // Main whot control method.
        try {
            // Send the current whot status, request move from the next user and then wait for him to play.
            broadcastEnvironment();
            users.get(players.get(currentPlayer)).requestMove();
            waitingForMove = true;

            // Waiting for the players to play a move.
            while (waitingForMove) {
                try {
                    // The actual processing of the move is done in one of the callback
                    // methods in a separate thread.
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Broadcast move that was played.
            broadcastInformation(movePlayed);

            // Check if the game has been won.
            if (playerCardCount.get(players.get(currentPlayer)) <= 0) {
                // @todo Consistency checks are needed here.
                gameWon = true;
                gameWinner = currentPlayer;
                broadcastGameWon();
                return;
            }

            // A player who played Whot 20 should make a call which card they want.
            if (exposed.getFirst().getShape() == WhotCard.WHOT && calledCard.equals("")) {
//                broadcastEnvironment();
                users.get(players.get(currentPlayer)).sendInformation("CALL");
                waitingForCall = true;
                // Only advance to next player after current player has made a call.
                while (waitingForCall) {
                    try {
                        // The actual processing of the call response is done in one of the callback
                        // methods in a separate thread.
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Broadcast called card.
                broadcastInformation(movePlayed);
            }

            // The current player will still play again if he just played a HOLD_ON card.
            if (isHoldOnCard) {
                broadcastInformation("HOLD ON");
            }
            else {
                advanceGameTurn();
            }

            // If a suspension was played, inform the current player and then move on.
            if (isSuspensionCard) {
                users.get(players.get(currentPlayer)).sendInformation("SUSPENSION miss a turn");
//                broadcastEnvironment();
                advanceGameTurn();
                isSuspensionCard = false;
            }

            // If someone played a pick-two, then tell the next player.
            if (pickTwoCount > 0) {
                users.get(players.get(currentPlayer)).sendInformation("PICK " + (pickTwoCount * 2) + " CARDS");
            } else if (isGeneralMarket) {
                users.get(players.get(currentPlayer)).sendInformation("GENERAL MARKET");
            }
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Card[] getCardForUser(String user) {
        waitingForMove = false;
        Card[] cards;
        if (covered.size() < Math.max(pickTwoCount * 2, 1)) {
            reloadCovered();
        }
        // If pick-two, then send the player two cards.
        if (pickTwoCount > 0) {
            playerCardCount.put(user, playerCardCount.get(user) + pickTwoCount * 2);
            cards = new Card[pickTwoCount * 2];
            while (pickTwoCount > 0) {
                pickTwoCount -= 1;
                cards[pickTwoCount * 2] = covered.removeFirst();
                cards[pickTwoCount * 2 + 1] = covered.removeFirst();
            }
        } else {
            playerCardCount.put(user, playerCardCount.get(user) + 1);
            cards = new Card[]{covered.removeFirst()};
        }
        // If everyone has picked general, then reset.
        if (currentPlayer == generalMarketPlayer && isGeneralMarket) {
            generalMarketPlayer = -1;
            isGeneralMarket = false;
        }
        Collections.addAll(dealed, cards);

        // If the current player has gone to market, then it's not hold on anymore.
        isHoldOnCard = false;

        // Update the message to send.
        movePlayed = String.format("%s picked %s cards", user, cards.length);

        return cards;
    }

    @Override
    public boolean receiveMoveFromUser(String strMove, String user) {
        WhotCard move = WhotCard.fromString(strMove);
        // Check that move follows rules before accepting.
        if (isValidMove(move)) {
            exposed.addFirst(move);
            dealed.remove(move);
            waitingForMove = false;
            calledCard = "";
            if (move.getLabel() == PICK_TWO_LABEL) {
                pickTwoCount += 1;
            }
            playerCardCount.put(user, playerCardCount.get(user) - 1);

            // Initialize or reset general market conditions.
            if (move.getLabel() == GENERAL_MARKET_LABEL) {
                generalMarketPlayer = currentPlayer;
                isGeneralMarket = true;
            }
            else {
                generalMarketPlayer = -1;
                isGeneralMarket = false;
            }

            // Set suspension card status.
            if (move.getLabel() == SUSPENSION_LABEL) {
                isSuspensionCard = true;
            }

            // Set hold-on card state.
            isHoldOnCard = move.getLabel() == HOLD_ON_LABEL;

            // Update the message to send.
            movePlayed = String.format("%s played %s", user, move);
            return true;
        }
        else {
            // The move is invalid. Return false.
            return false;
        }
    }

    @Override
    public void handleInfoReceived(String info, String user) {
        // Act upon a call for card by a player who played whot 20.
        String[] infos = info.split(" ", 2);
        if (infos[0].equals("CALL") && !WhotCard.isIllegalCardSpec(infos[1] + " 1")) {
            waitingForCall = false;
            calledCard = infos[1];
            // Update the message to send.
            movePlayed = String.format("%s called %s", user, calledCard);
        }
        else {
            System.out.println(info);
        }
    }

    /** Reloads the covered set by transferring everything from the exposed set except the topmost card */
    private void reloadCovered() {
        Card first = exposed.removeFirst();
        exposed.shuffle(20);
        covered.addAll(exposed);
        exposed.removeAll(exposed);
        exposed.addFirst(first);
    }

    /** Check that the move follows rules */
    private boolean isValidMove(Card move) {
        return matchesTopCard(move) && isPickTwoCounter(move)
                && (!isGeneralMarket || currentPlayer == generalMarketPlayer)
                && !exposed.isDuplicate(move);
    }

    private boolean matchesTopCard(Card move) {
        Card top = exposed.getFirst();
        return move.getLabel() == top.getLabel() || move.getShape() == top.getShape() ||
                move.getShape() == WhotCard.WHOT || WhotCard.SHAPES.get(move.getShape()).equals(calledCard);
    }

    private boolean isPickTwoCounter(Card move) {
        return pickTwoCount < 1 || move.getLabel() == PICK_TWO_LABEL;
    }

}
