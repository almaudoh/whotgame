package org.anieanie.cardgame.utils;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.AbstractGameClient;
import org.anieanie.cardgame.CLIGameClient;
import org.anieanie.cardgame.cgmp.CGMPException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Used to get input from the command line for interacting with the game.
 *
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
        // ---------------------------------
        String choice = "z";
        String chmod = "@";
        String card = "";

        // Display the initial help menu and game status.
        showHelpMenu();
        showGameStatus();

        /*
         * Main game control while loop.
         *
         * This loop continues until there is a winner as announced by the server or you resign.
         */
        while (gameClient.getClientStatus() != AbstractGameClient.STATUS_GAME_WON && choice.charAt(0) != '#') {
            // Show a prompt
            System.out.print(">> ");
            choice = getValidInput();

            if (choice.equals("?")) {
                showHelpMenu();
            }
            else if (choice.equals("1")) {
                gameClient.startGame();
            }
            else if (choice.equals(("2"))) {
                showGameStatus();
            }
            else if (choice.equals("3")) {
                displayCards(gameClient.getCards());
            }
            else if (choice.equals("4")) {
                // Play allowed only if it is our turn.
                if (gameClient.getClientStatus() == AbstractGameClient.STATUS_WAITING_FOR_USER) {
                    try {
                        card = input.readLine();
                        gameClient.playCard(card);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println("Not your turn to play yet!");
                }
            }
            else if (choice.equals("5")) {
                // Play allowed only if it is our turn.
                if (gameClient.getClientStatus() == AbstractGameClient.STATUS_WAITING_FOR_USER) {
                    gameClient.requestCard();
                }
                else {
                    System.out.println("Not your turn to play yet!");
                }
            }
        }

        try {
            gameClient.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (CGMPException e) {
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
                "\n\nEnter:\n"
                        + "      (#) to disconnect from server.\n"
                        + "      (?) to show this help menu.\n\n"
                        + "      (1) To start a new game.\n"
                        + "      (2) To view game status.\n"
                        + "      (3) To view your hand.\n"
                        + "      (4) To play a card.\n"
                        + "      (5) To pick market."
        );
        System.out.println("Choose an option:");
    }

    private void displayCards(CardSet cards) {
        StringBuilder displayedCards = new StringBuilder(" | ");
        for (Card card : cards.getCardlist()) {
            displayedCards.append(card).append(" | ");
        }
        System.out.println("Your cards: " + displayedCards);
        // @todo: Need to abstract this top card for non-Whot games.
        System.out.println("Top card: " + ((CLIGameClient)gameClient).getTopCard());
    }

    private String getValidInput() {
        String strInput = "";
        while (true) {
            try {
                while (strInput.length() <= 0) {
                    strInput = input.readLine();
                    switch (strInput.charAt(0)) {
                        case '#': // Disconnect from the server.
                        case '1': // Start a new game.
                        case '2': // View game status (all card stacks).
                        case '3': // View your card stack.
                        case '4': // Play a card.
                        case '5': // Pick market.
                            return strInput;
                        default:    // Wrong entry, ask for a correct entry.
                            System.out.println("Type [1 - 5] or #, type ? for help:");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

}
