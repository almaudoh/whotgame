package org.anieanie.cardgame.ui.cli;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.gameplay.AbstractGameClient;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameEnvironment;
import org.anieanie.cardgame.ui.Display;

/**
 * Abstraction to allow display of whot information.
 */
public class CommandLineDisplay implements Display {
    @Override
    public void showNotification(String message) {
        System.out.println(message + " - " + Thread.currentThread());
    }

    @Override
    public void showGameStatus(GameClient gameClient) {
        // showNotification the current whot status.
        switch (gameClient.getClientStatus()) {
            case (AbstractGameClient.STATUS_WAITING_FOR_USER):
                System.out.println("Status: your turn to play.");
                break;

            case (AbstractGameClient.STATUS_WAITING_FOR_TURN):
                System.out.println("Status: waiting for your turn to play.");
                break;

            case (AbstractGameClient.STATUS_WAITING_TO_START):
                System.out.println("Status: waiting for server to start whot.");
                break;

            case (AbstractGameClient.STATUS_GAME_WON):
                System.out.println("Status: whot has been won");
                break;

        }
        displayCards(gameClient.getCards(), gameClient.getEnvironment());
    }

    @Override
    public void showHelpMenu() {
        // Give the user a menu.
        System.out.println(
                "\nType:\n"
                        + "      (#) to disconnect from server.\n"
                        + "      (?) to show this help menu.\n\n"
                        + "      (1) To start a new game.\n"
                        + "      (2) To play a move."
        );
        System.out.println("Choose an option:");
    }

    private void displayCards(CardSet cards, GameEnvironment environment) {
        StringBuilder displayedCards = new StringBuilder(" | ");
        for (Card card : cards.getCardlist()) {
            displayedCards.append(card).append(" | ");
        }
        System.out.printf("Your cards: %s%n", displayedCards);

        // @todo: Need to abstract this top card for non-Whot games.
        System.out.printf("Top card: %s", environment.get("TopCard"));

        String calledCard = environment.get("CalledCard");
        if (calledCard != null && !calledCard.equals("")) {
            System.out.printf("; Called card: %s", calledCard);
        }
        System.out.print('\n');
    }

}
