package org.anieanie.cardgame.learning.whot;

import org.anieanie.cardgame.agent.GameAgent;
import org.anieanie.cardgame.agent.SimpleWhotGameAgent;
import org.anieanie.cardgame.cgmp.CGMPException;
import org.anieanie.cardgame.cgmp.ClientCGMPRelay;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameServer;
import org.anieanie.cardgame.gameplay.logging.WhotGameLogger;
import org.anieanie.cardgame.gameplay.whot.WhotGameClient;
import org.anieanie.cardgame.gameplay.whot.WhotGameMonitor;
import org.anieanie.cardgame.ui.Display;
import org.anieanie.cardgame.ui.cli.CommandLineDisplay;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * This class simulates games between agents and logs the game for training
 */
public class GameGenerator {

    public static void main(String[] args) throws Exception {
        int epochs = 5; //000;
        for (int i = 0; i < epochs; i++) {
            playGame();
        }
    }

    private static void playGame() {
        // Start a game server.
        WhotGameMonitor monitor = new WhotGameMonitor();
        GameServer server = launchServer(monitor);

        // Then start two game clients and engage them.
        GameClient client1 = launchClient();
        GameClient client2 = launchClient();

        // Wait until game is over. Then return from the method.
        while (!monitor.isGameWon()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            client1.close();
            client2.close();
            server.stopRunning();
        } catch (CGMPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Wait for server to finish before returning, in case another game should be started.
        while (server.isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static GameServer launchServer(WhotGameMonitor monitor) {
        GameServer server = new GameServer(monitor, 5550);
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        }).start();
        return server;
    }

    private static WhotGameClient launchClient() {
        Display display = new CommandLineDisplay();
        try {
            // A short wait for server to initialize
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Socket socket = new Socket("127.0.0.1", 5550);
            ClientCGMPRelay relay = new ClientCGMPRelay(socket);
            WhotGameClient client = new WhotGameClient(relay, display);
            GameAgent agent = new SimpleWhotGameAgent(client);
            client.setLogger(new WhotGameLogger("GameAI/resources/saved_moves.txt", true));

            // Connect the client to the server with the specified game agent.
            client.connect(agent);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.run();
                }
            }).start();
            return client;
        }
        catch (ConnectException e) {
            display.showNotification("I could not connect to the server.");
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
        catch (Exception e) {
            display.showNotification("Some kind of error has occurred.");
            e.printStackTrace();
            System.exit(0);
        }	// End of exception
        return null;
    }
}
