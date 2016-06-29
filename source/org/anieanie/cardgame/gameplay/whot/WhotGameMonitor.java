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

public class WhotGameMonitor extends AbstractGameMonitor {
    // The number that is used as the pick two number.
    private static final int PICK_TWO_LABEL = 7;
    private static final int GENERAL_MARKET_LABEL = 4;
    private static final int SUSPENSION_LABEL = 8;

    // Each new WhotGameMonitor starts a new whot
    // and should normally be on a new thread

    // Indicates that the monitor is waiting for a move from the client. Loop control variable.
    private volatile boolean waitingForMove = false;

    // Indicates that the monitor is waiting for a player to call a card (after Whot 20 is played).
    private volatile boolean waitingForCall = false;

    // Indicates that the monitor is waiting for a player to call a card (after Whot 20 is played).
    private int pickTwoCount = 0;

    private int generalMarketPlayer = -1;

    // The card that is called by a player who played Whot 20.  `
    private String calledCard = "";

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
            System.out.println("Waiting for move from: " + players.get(currentPlayer));
            while (waitingForMove) {
                try {
                    // The actual processing of the move is done in one of the callback
                    // methods in a separate thread.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Check if the whot has been won.
            if (playerCardCount.get(players.get(currentPlayer)) <= 0) {
                gameWon = true;
                gameWinner = currentPlayer;
                broadcastGameWon();
                return;
            }

            // Things to do before advancing the turn to the next user.
            if (isGeneralMarket()) {
                // Mark the player who played general market, so he can play again.
                generalMarketPlayer = currentPlayer;
            }
            else {
                generalMarketPlayer = -1;
            }

            // A player who played Whot 20 should make a call which card they want.
            if (exposed.getFirst().getShape() == WhotCard.WHOT && calledCard.equals("")) {
                broadcastEnvironment();
                users.get(players.get(currentPlayer)).sendInformation("CALL");
                waitingForCall = true;
                while (waitingForCall) {
                    try {
                        // The actual processing of the call response is done in one of the callback
                        // methods in a separate thread.
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Only advance to next player if current player has made a call.
            advanceGameTurn();

            // If a suspension was played, inform the current player and then move on.
            if (isSuspensionCard()) {
                users.get(players.get(currentPlayer)).sendInformation("SUSPENSION miss a turn");
                broadcastEnvironment();
                advanceGameTurn();
            }

            // If someone played a pick-two, then tell the next player.
            if (pickTwoCount > 0) {
                users.get(players.get(currentPlayer)).sendInformation("PICK " + (pickTwoCount * 2) + " CARDS");
            } else if (isGeneralMarket()) {
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
            cards = new Card[pickTwoCount * 2];
            while (pickTwoCount > 0) {
                pickTwoCount -= 1;
                cards[pickTwoCount * 2] = covered.removeFirst();
                cards[pickTwoCount * 2 + 1] = covered.removeFirst();
            }
            playerCardCount.put(user, playerCardCount.get(user) + pickTwoCount * 2);
        } else {
            cards = new Card[]{covered.removeFirst()};
            playerCardCount.put(user, playerCardCount.get(user) + 1);
        }
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
        }
        else {
            System.out.println(info);
        }
    }


    /** Reloads the covered set by transferring everything from the exposed set except the topmost card */
    private void reloadCovered() {
        System.out.printf("Before reload: exposed: %s; covered %s;%n", exposed.toString(), covered.toString());
        Card first = exposed.removeFirst();
        exposed.shuffle(20);
        covered.addAll(exposed);
        exposed.removeAll(exposed);
        exposed.addFirst(first);
        System.out.printf("After reload: exposed: %s; covered %s;%n", exposed.toString(), covered.toString());
    }

    /** Check that the move follows rules */
    private boolean isValidMove(Card move) {
        return matchesTopCard(move) && isPickTwoCounter(move)
                && (!isGeneralMarket() || currentPlayer == generalMarketPlayer)
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

    private boolean isGeneralMarket() {
        return exposed.getFirst().getLabel() == GENERAL_MARKET_LABEL;
    }

    private boolean isSuspensionCard() {
        return exposed.getFirst().getLabel() == SUSPENSION_LABEL;
    }

}
