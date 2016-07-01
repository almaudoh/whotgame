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
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
    }

    @Override
    public void showGameStatus(GameClient gameClient) {
        // showNotification the current whot status.
        switch (gameClient.getClientStatus()) {
            case (AbstractGameClient.STATUS_WAITING_FOR_USER):
//                System.out.print("My turn ... ");
                break;

            case (AbstractGameClient.STATUS_WAITING_FOR_TURN):
//                System.out.print("Awaiting turn ... ");
                break;

            case (AbstractGameClient.STATUS_WAITING_TO_START):
//                System.out.print("Waiting to start ... ");
                break;

            case (AbstractGameClient.STATUS_GAME_WON):
//                System.out.print("Game won!!");
                break;
        }
        displayCards(gameClient.getCards(), gameClient.getEnvironment());
    }

    private void displayCards(CardSet cards, GameEnvironment environment) {
        StringBuilder displayedCards = new StringBuilder();
        for (Card card : cards.getCardlist()) {
            displayedCards.append(card).append("|");
        }
        if (displayedCards.length() > 0) {
            displayedCards.delete(displayedCards.length() - 1, displayedCards.length());
        }
        // @todo: Need to abstract this top card for non-Whot games.
        System.out.printf("Top card: %s; ", environment.get("TopCard"));

        System.out.printf("Your cards: (%s)", displayedCards);

        String calledCard = environment.get("CalledCard");
        if (calledCard != null && !calledCard.equals("")) {
            System.out.printf("; Called card: %s", calledCard);
        }
        System.out.print('\n');
    }

}
