package org.anieanie.cardgame.gameplay.whot;

import org.anieanie.cardgame.agent.GameAgent;
import org.anieanie.cardgame.agent.ManualWhotGameAgent;
import org.anieanie.cardgame.agent.SimpleWhotGameAgent;
import org.anieanie.cardgame.cgmp.CGMPSpecification;
import org.anieanie.cardgame.cgmp.ClientCGMPRelay;
import org.anieanie.cardgame.gameplay.GameClient;
import org.anieanie.cardgame.ui.Display;
import org.anieanie.cardgame.ui.cli.CommandLineDisplay;
import org.anieanie.cardgame.utils.Debugger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

/**
 * A whot game client that interacts with the command line.
 */
public class GameLauncher {

    @Option(name = "-agent", usage = "Specify the game playing agent (cli, simple or q-learning)")
    private String agent;

    @Option(name = "-port", usage = "Specify the server connection port (default: " + CGMPSpecification.Connection.DEFAULT_PORT + ")")
    private int serverPort = CGMPSpecification.Connection.DEFAULT_PORT;

    @Option(name = "-address", usage = "Specify the server connection ip-address (default: " + CGMPSpecification.Connection.DEFAULT_IP + ")")
    private String ipAddress = CGMPSpecification.Connection.DEFAULT_IP;

    public GameAgent initializeGameAgent(GameClient client, Display display) {
        switch (agent) {
            case "simple":
                return new SimpleWhotGameAgent(client, display);
            default:
                return new ManualWhotGameAgent(client, display);
        }
    }

    /**
     * Constructor
     */
    public GameLauncher() {
    }

    public void launch() {
        Display display = new CommandLineDisplay();
        try {
            Socket socket = new Socket(ipAddress, serverPort);
            ClientCGMPRelay relay = new ClientCGMPRelay(socket);
            WhotGameClient client = new WhotGameClient(relay, inputUserName(), display);
            relay.setListener(client);
//            relay.addLowLevelListener(Debugger.getLowLevelListener(client.getUsername()));

            // Connect to the client with the specified agent.
            client.connect();
            client.run(initializeGameAgent(client, display));
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

    public static void main(String [] args) {
        GameLauncher launcher = new GameLauncher();
        CmdLineParser parser = new CmdLineParser(launcher);

        try {
            parser.parseArgument(args);
            launcher.launch();
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
//            e.printStackTrace();
//            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
        }
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
