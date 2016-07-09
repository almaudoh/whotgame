package org.anieanie.cardgame.gameplay;

import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.ui.Display;
import org.anieanie.cardgame.ui.cli.CommandLineDisplay;
import org.anieanie.cardgame.utils.Debugger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * A game watcher views the game and reports progress.
 */
public class GameWatcher implements ClientCGMPRelayListener {

    private final Display display;
    private final ClientCGMPRelay relay;
    private GameEnvironment environment;
    private GameEnvironment lastEnvironment;
    private ArrayList<String> cardStack;

    public GameWatcher(ClientCGMPRelay relay, Display display) {
        this.relay = relay;
        this.display = display;
        this.environment = new GameEnvironment();
        this.cardStack = new ArrayList<>();
        this.relay.setListener(this);
    }

    public static void main(String [] args) {
        Display display = new CommandLineDisplay();
        try {
            Socket socket = new Socket(CGMPSpecification.Connection.DEFAULT_IP, CGMPSpecification.Connection.DEFAULT_PORT);
            ClientCGMPRelay relay = new ClientCGMPRelay(socket);
            GameWatcher watcher = new GameWatcher(relay, display);
            watcher.watch();
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
    }

    public void watch() throws IOException, CGMPException {
        // Add a low level listener for debugging purposes.
        relay.addLowLevelListener(Debugger.getLowLevelListener("watcher-1", "watcher-1"));

        // Connect the watcher relay to the game server.
        if (!relay.connect("watcher-1")) {
            throw new GameClientException(String.format("Server not responding on ip address %s and port %s",
                    relay.getSocket().getInetAddress(), relay.getSocket().getPort()));
        }
        display.showNotification("Requesting view");
        relay.requestView();

        while (true) {
            try {
                relay.scanWithThreadSleep();
            }
            catch (InterruptedException i) {
                i.printStackTrace();
            }
            catch(CGMPConnectionException e) {
                // We may not always have a response from scans.
            } catch (CGMPException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void moveRequested() {
        // Watcher would not request to move.
    }

    @Override
    public void environmentReceived(String envSpec) {
        lastEnvironment = (GameEnvironment) environment.clone();
        environment = GameEnvironment.fromCGMPString(envSpec);
        updateGameState();
        updateGameCommentary();
//        lastPlayer = environment.get("CurrentPlayer");
    }

    private void updateGameState() {
        cardStack.add(environment.get("TopCard") + "\n");
    }

    private void updateGameCommentary() {
        // Nothing new if the player didn't change and the card didn't change.
//        String message;
//        System.out.println("Last env: " + lastEnvironment);
//        System.out.println("Current env: " + environment);
//        if (lastEnvironment == environment) {
            // Updates a commentary on the game using the game environment.
//            message = String.format("Game start: TopCard: %s; Cardstack: %s%nEnvironment: %s%n",
//                    environment.get("TopCard"), cardStack.toString(), environment.toString());
//        }
//        else {
            // Updates a commentary on the game using the game environment.
//            message = String.format("%s played %s; Cardstack: %s%nEnvironment: %s%n",
//                    lastEnvironment.get("CurrentPlayer"), environment.get("TopCard"), cardStack.toString(), environment.toString());
//        }
//        display.showNotification(message);
        display.showNotification(environment.toString());
    }

    @Override
    public void cardReceived(String cardSpec) {
        // Watcher would not receive cards.
    }

    @Override
    public void gameWon(String winner) {
        display.showNotification("Game won by " + winner);
    }

    @Override
    public void relayTerminated() {
        display.showNotification("Relay terminated");
    }

    @Override
    public void messageSent(CGMPMessage message) {
        // Not implemented.
    }

    @Override
    public void messageReceived(CGMPMessage message) {
        // Not implemented.
    }

    @Override
    public void errorSent(int errorcode) {
        // Not implemented.
    }

    @Override
    public void errorReceived(int errorcode) {
        // Not implemented.
    }

    @Override
    public void infoReceived(String info) {
        display.showNotification(info);
    }

}
