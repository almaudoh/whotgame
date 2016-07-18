package org.anieanie.cardgame.training;

/**
 * This class simulates games between agents and logs the game for training
 */
public class GameGenerator {

    public static void main(String[] args) throws Exception {
        GameRunner runner = new GameRunner("simple", "simple");
        int epochs = 1000;
        for (int i = 0; i < epochs; i++) {
            runner.playGame();
        }
    }

}
