package org.anieanie.cardgame.agent;

import org.anieanie.card.whot.WhotCard;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.whot.WhotGameClient;
import org.anieanie.cardgame.ui.Display;
import org.anieanie.cardgame.ui.cli.InputLoop;

/**
 * Used to get input from the command line for interacting with the game.
 *
 * Created by almaudoh on 6/2/16.
 */
public class ManualWhotGameAgent implements GameAgent {

    private final Display display;
    private GameClient gameClient;

    public ManualWhotGameAgent(GameClient gameClient, Display display) {
        this.gameClient = gameClient;
        this.display = display;
    }

    // Wait for input on a separate thread
    public void run() {
        // showNotification the initial help menu and whot status.
        display.showHelpMenu();
        display.showGameStatus(gameClient);

        /*
         * Main whot control while loop.
         *
         * This loop continues until there is a winner as announced by the server or you resign.
         */
        String choice = "z";
        while (gameClient.getClientStatus() != GameClient.STATUS_GAME_WON && !choice.equals("#")) {
            choice = getActionInput();
            switch (choice) {
                case "?":
                    display.showHelpMenu();
                    break;
                case "1":
                    if (gameClient.getClientStatus() == GameClient.STATUS_WAITING_TO_START) {
                        gameClient.startGame();
                        display.showNotification("Game start requested");
                    } else {
                        display.showNotification("Game already started");
                        display.showGameStatus(gameClient);
                    }
                    gameClient.refreshClientStatus();
                    break;
                case "2":
                    // Show current whot status.
                    display.showGameStatus(gameClient);
                    getMoveAndPlay();
                    break;

                default:
            }

            // Release CPU cycles.
            // @todo: Using thread sleep delay to manage the timing between WHOT 20 playing and card CALL seems
            // brittle at this time and would need further review.
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Post move actions.
            postMoveActions();

        }   // End whot loop while

//        try {
//            gameClient.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CGMPException e) {
//            e.printStackTrace();
//        }
    }

    public String getActionInput() {
        return (new InputLoop(display)).runLoop(new InputLoop.InputLoopConstraint() {
            @Override
            public boolean isSatisfied(String input) {
                if (input.length() > 0) {
                    switch (input.charAt(0)) {
                        case '#': // Disconnect from the server.
                        case '?': // Disconnect from the server.
                        case '1': // Start a new game.
                        case '2': // Play a move
                            return true;
                        default:    // Wrong entry, ask for a correct entry.
                    }
                }
                return false;
            }

            @Override
            public String promptMessage() {
                return "Type 1 to start, 2 to play, '#' to exit whot or '?' for help ";
            }
        });
    }

    public void getMoveAndPlay() {
        // Play allowed only if it is our turn.
        String cardSpec = "";
        if (gameClient.getClientStatus() == GameClient.STATUS_WAITING_FOR_USER) {
            // Input loop for getting the move to be played.
            cardSpec = (new InputLoop(display)).runLoop(new InputLoop.InputLoopConstraint() {
                @Override
                public boolean isSatisfied(String input) {
                    if (input.charAt(0) == '#') {
                        return true;
                    }
                    else if (!input.equalsIgnoreCase("market") && WhotCard.isIllegalCardSpec(input)) {
                        display.showNotification("Illegal card specified '" + input + "'");
                        return false;
                    }
                    else {
                        return true;
                    }
                }

                @Override
                public String promptMessage() {
                    return "Type the card you wish to play (e.g. Circle 3), type MARKET to go market or # to go back";
                }
            });
            // Validate the card before playing.
            if (cardSpec.charAt(0) != '#') {
                gameClient.playMove(cardSpec);
            }
        }
        else {
            display.showNotification("Not yet your turn to play!");
        }
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
                public String promptMessage() {
                    return "Whot 20 played, call your shape ("
                            + String.join(", ", WhotCard.SHAPES).replaceFirst(", whot", "")
                            + "): ";
                }
            });
            display.showNotification("CALL for '" + shape + "' made");
            client.sendWhotCallShape(shape);
        }
    }

}
