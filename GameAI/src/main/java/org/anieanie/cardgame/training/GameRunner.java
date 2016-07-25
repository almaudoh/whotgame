package org.anieanie.cardgame.training;

import org.anieanie.cardgame.agent.GameAgent;
import org.anieanie.cardgame.agent.ManualWhotGameAgent;
import org.anieanie.cardgame.agent.SimpleWhotGameAgent;
import org.anieanie.cardgame.cgmp.CGMPException;
import org.anieanie.cardgame.cgmp.ClientCGMPRelay;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.gameplay.GameServer;
import org.anieanie.cardgame.gameplay.logging.WhotGameLogger;
import org.anieanie.cardgame.gameplay.whot.WhotGameClient;
import org.anieanie.cardgame.gameplay.whot.WhotGameMonitor;
import org.anieanie.cardgame.learning.whot.IntelligentWhotGameAgent;
import org.anieanie.cardgame.ui.Display;
import org.anieanie.cardgame.ui.cli.CommandLineDisplay;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Runs a single game from start to finish with selected game agents.
 */
public class GameRunner {

    private final String agentType2;
    private final String agentType1;

    public GameRunner(String agentType1, String agentType2) {
        this.agentType1 = agentType1;
        this.agentType2 = agentType2;
    }

    public void playGame() {
        // Start a game server.
        WhotGameMonitor monitor = new WhotGameMonitor();
        GameServer server = launchServer(monitor);

        // Then start two game clients and engage them.
        GameClient client1 = launchClient(agentType1);
        GameClient client2 = launchClient(agentType2);

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

    private GameServer launchServer(WhotGameMonitor monitor) {
        GameServer server = new GameServer(monitor, 5550);
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        }).start();
        return server;
    }

    private WhotGameClient initializeClient(Display display) throws IOException {
        Socket socket = new Socket("127.0.0.1", 5550);
        ClientCGMPRelay relay = new ClientCGMPRelay(socket);
        WhotGameClient client = new WhotGameClient(relay, display);
        client.setLogger(new WhotGameLogger("resources/saved_moves.txt", true));
        return client;
    }

    private WhotGameClient launchClient(String agentType) {
        Display display = new CommandLineDisplay();
        try {
            // A short wait for server to initialize
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WhotGameClient client = initializeClient(display);

            GameAgent agent;
            switch (agentType) {
                case "simple":
                    agent = new SimpleWhotGameAgent(client);
                    break;
                case "smart":
                    agent = new IntelligentWhotGameAgent(client);
                    break;
                default:
                    agent = new ManualWhotGameAgent(client, display);
            }
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
