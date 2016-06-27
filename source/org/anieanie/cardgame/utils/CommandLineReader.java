package org.anieanie.cardgame.utils;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.AbstractGameClient;
import org.anieanie.cardgame.CLIGameClient;
import org.anieanie.cardgame.cgmp.CGMPConnectionException;
import org.anieanie.cardgame.cgmp.CGMPException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Used to get input from the command line for interacting with the game.
 * <p>
 * Created by almaudoh on 6/2/16.
 */
public class CommandLineReader implements Runnable {

    private AbstractGameClient gameClient;
    protected BufferedReader input;

    public CommandLineReader(AbstractGameClient gameClient) {
        this.gameClient = gameClient;
        this.input = new BufferedReader(new InputStreamReader(System.in));
    }

    // Wait for input on a separate thread
    public void run() {
        char choice = 'z';
        String cardSpec = "";

        // Display the initial help menu and game status.
        showHelpMenu();
        showGameStatus();

        /*
         * Main game control while loop.
         *
         * This loop continues until there is a winner as announced by the server or you resign.
         */
        while (gameClient.getClientStatus() != AbstractGameClient.STATUS_GAME_WON && choice != '#') {
            choice = getValidInput();

            try {
                switch (choice) {
                    case '?':
                        showHelpMenu();
                        break;
                    case '1':
                        if (gameClient.getClientStatus() == AbstractGameClient.STATUS_WAITING_TO_START) {
                            gameClient.startGame();
                            System.out.println("Game start requested");
                        } else {
                            System.out.println("Game already started");
                            showGameStatus();
                        }
                        gameClient.refreshClientStatus();
                        break;
                    case '2':
                        // Show current game status.
                        showGameStatus();
                        // Play allowed only if it is our turn.
                        if (gameClient.getClientStatus() == AbstractGameClient.STATUS_WAITING_FOR_USER) {
                            while (true) {
                                System.out.println("Type the card you wish to play (e.g. Circle 3), type MARKET to go market or # to go back");
                                System.out.print(">>");
                                cardSpec = input.readLine();
                                if (cardSpec.charAt(0) == '#') {
                                    break;
                                } else if (cardSpec.equalsIgnoreCase("MARKET")) {
                                    gameClient.requestCard();
                                    break;
                                } else {
                                    // Validate the card before playing.
                                    if (WhotCard.isIllegalCardSpec(cardSpec)) {
                                        System.out.println("Illegal card specified '" + cardSpec + "'");
                                    } else {
                                        gameClient.playCard(cardSpec);
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            System.out.println("Not your turn to play yet!");
                        }
                        break;

                    default:
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            // Release CPU cycles.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }   // End game loop while

        try {
            gameClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CGMPException e) {
            e.printStackTrace();
        }
    }

    private void showGameStatus() {
        // Display the current game status.
        switch (gameClient.getClientStatus()) {
            case (AbstractGameClient.STATUS_WAITING_FOR_USER):
                System.out.println("Status: your turn to play.");
                break;

            case (AbstractGameClient.STATUS_WAITING_FOR_TURN):
                System.out.println("Status: waiting for your turn to play.");
                break;

            case (AbstractGameClient.STATUS_WAITING_TO_START):
                System.out.println("Status: waiting for server to start game.");
                break;

            case (AbstractGameClient.STATUS_GAME_WON):
                System.out.println("Status: game has been won");
                break;

        }
        displayCards(gameClient.getCards());
    }

    private void showHelpMenu() {
        // Give the user a menu .
        System.out.println(
                "\nType:\n"
                        + "      (#) to disconnect from server.\n"
                        + "      (?) to show this help menu.\n\n"
                        + "      (1) To start a new game.\n"
                        + "      (2) To play a move."
        );
        System.out.println("Choose an option:");
    }

    private void displayCards(CardSet cards) {
        StringBuilder displayedCards = new StringBuilder(" | ");
        for (Card card : cards.getCardlist()) {
            displayedCards.append(card).append(" | ");
        }
        System.out.printf("Your cards: %s%n", displayedCards);

        // @todo: Need to abstract this top card for non-Whot games.
        System.out.printf("Top card: %s", ((CLIGameClient) gameClient).getTopCard());

        String calledCard = ((CLIGameClient) gameClient).getCalledCard();
        if (calledCard != null && !calledCard.equals("")) {
            System.out.printf("; Called card: %s", calledCard);
        }
        System.out.print('\n');
    }

    private char getValidInput() {
        String line;
        while (true) {
            try {
                // Show a prompt
                System.out.print(">> ");
                line = input.readLine();
                if (line.length() > 0) {
                    switch (line.charAt(0)) {
                        case '#': // Disconnect from the server.
                        case '?': // Disconnect from the server.
                        case '1': // Start a new game.
                        case '2': // Play a move
                            return line.charAt(0);
                        default:    // Wrong entry, ask for a correct entry.
                            System.out.println("Type 1 to start, 2 to play, '#' to exit game or '?' for help " + Thread.currentThread());
                    }
                }
                Thread.sleep(50);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
