package org.anieanie.cardgame.agent;

import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.gameplay.whot.WhotGameClient;
import org.anieanie.cardgame.ui.Display;
import org.anieanie.cardgame.ui.cli.InputLoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Used to get input from the command line for interacting with the game.
 *
 * Created by almaudoh on 6/2/16.
 */
public class ManualWhotGameAgent implements GameAgent {

    private final Display display;
    private GameClient gameClient;
    private String name;

    public ManualWhotGameAgent(GameClient gameClient, Display display) {
        this.gameClient = gameClient;
        this.display = display;
    }

    // Wait for input on a separate thread
    public void run() {
        // showNotification the initial help menu and whot status.
        display.showGameStatus(gameClient);

        boolean startPromptDone = false;
        boolean gamePromptDone = false;
        /*
         * Main whot control while loop.
         *
         * This loop continues until there is a winner as announced by the server or you resign.
         */
        String choice = "";
        while (gameClient.getClientStatus() != GameClient.STATUS_GAME_WON && !choice.equals("#")) {
            if (gameClient.getClientStatus() == GameClient.STATUS_WAITING_TO_START && !startPromptDone) {
                display.showNotification("Type 'START' to start the game or '#' to exit");
                startPromptDone = true;
            }
            else if (!gamePromptDone) {
                display.showNotification("Type the card you wish to play (e.g. 'Circle 3'), type 'MARKET' to go market, 'STATUS' for game status or '#' to exit");
                gamePromptDone = true;
            }

            choice = getActionInput();
            switch (choice) {
                case "start":
                    if (gameClient.getClientStatus() == GameClient.STATUS_WAITING_TO_START) {
                        gameClient.startGame();
                    } else {
                        display.showNotification("Game already started");
                        display.showGameStatus(gameClient);
                    }
                    gameClient.refreshClientStatus();
                    break;
                case "status":
                    // Show current whot status.
                    display.showGameStatus(gameClient);
                    break;

                case "market":
                default:
                    if (gameClient.getClientStatus() == GameClient.STATUS_WAITING_FOR_USER) {
                        gameClient.playMove(choice);
                    }
                    else {
                        display.showNotification("Not yet your turn to play!");
                    }
            }

            // Release CPU cycles.
            // @todo: Using thread sleep delay to manage the timing between WHOT 20 playing and card CALL seems
            // brittle at this time and would need further review.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Post move actions.
            postMoveActions();

        }   // End game loop while

//        try {
//            gameClient.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CGMPException e) {
//            e.printStackTrace();
//        }
    }

    public String getActionInput() {
        // Input loop for getting the move to be played.
        return  (new InputLoop(display)).runLoop(new InputLoop.InputLoopConstraint() {
            @Override
            public boolean isSatisfied(String input) {
                if (input.charAt(0) == '#') {
                    return true;
                }
                else if (input.equalsIgnoreCase("market") || input.equalsIgnoreCase("start") || input.equalsIgnoreCase("status") ) {
                    return true;
                }
                else if (WhotCard.isIllegalCardSpec(input)) {
                    display.showNotification("Illegal card specified '" + input + "'");
                    return false;
                }
                else {
                    return true;
                }
            }

            @Override
            public String helpMessage() {
                return null;
            }

            @Override
            public String promptMessage() {
                return null;
            }
        });
    }

    private void postMoveActions() {
        WhotGameClient client = (WhotGameClient)gameClient;
        if (client.isAwaitingWhotCallInfo()) {
            // Get input loop for calling the card after whot 20 is played.
            String shape = (new InputLoop(display)).runLoop(new InputLoop.InputLoopConstraint() {

                @Override
                public boolean isSatisfied(String input) {
                    // 1's exist in all shapes except WHOT so this is a great shortcut to evaluate if the shape
                    // is legal.
                    return !WhotCard.isIllegalCardSpec(input + " 1");
                }

                @Override
                public String helpMessage() {
                    return "Whot 20 played, call your shape ("
                            + String.join(", ", WhotCard.SHAPES).replaceFirst(", whot", "")
                            + "): ";
                }

                @Override
                public String promptMessage() {
                    return null;
                }
            });
            display.showNotification("CALL for '" + shape + "' made");
            client.sendWhotCallShape(shape);
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            name = inputUserName();
        }
        return name;
    }

    @Override
    public void refresh() {
        if (gameClient.getClientStatus() == GameClient.STATUS_GAME_WON) {
            // @todo Print message and then force the readLine() input loop to stop.
        }
    }

    @Override
    public void moveRejected(Card card) {
        if (gameClient.getCards().contains(card)) {
            display.showNotification("Card '" + card + "' was rejected. Please play another card.");
        }
        else {
            display.showNotification("You don't have '" + card + "'.");
        }
    }

    @Override
    public void moveAccepted(Card card) {
        display.showNotification("Played " + card);
    }

    private static String inputUserName() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
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

}
