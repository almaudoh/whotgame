package org.anieanie.cardgame.gameplay.logging;

import org.anieanie.card.Card;
import org.anieanie.card.CardSet;
import org.anieanie.cardgame.gameplay.GameEnvironment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Logs the game moves into a training set of features.
 */
public class WhotGameLogger implements GameLogger {
    private PrintWriter pr;

    public WhotGameLogger(String filename, boolean append) throws FileNotFoundException {
        pr = new PrintWriter(new FileOutputStream(filename, append));
    }

    @Override
    public void logMove(String move, GameEnvironment environment, CardSet cards) {
        // Log the key environment state variables that determine which move is made.
        pr.printf("%s,%s,%s,%s,%s%n", move, environment.get("TopCard"),
                environment.get("CalledCard"), environment.get("MarketMode"), toConcatenatedString(cards));
    }

    private String toConcatenatedString(CardSet cards) {
        if (cards == null || cards.size() < 1) {
            return "";
        }
        StringBuilder string = new StringBuilder();
        for (Card card : cards) {
            string.append(card).append(',');
        }
        return string.deleteCharAt(string.length() - 1).toString();
    }

    @Override
    public void flush() {
        pr.flush();
    }

    @Override
    public void finalize() throws Throwable {
        pr.close();
        super.finalize();
    }

}
