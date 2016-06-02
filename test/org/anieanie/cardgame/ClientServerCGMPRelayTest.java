package org.anieanie.cardgame;

import mockit.*;

import org.anieanie.card.AbstractCard;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.utils.Debugger;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;

import static org.testng.Assert.*;

/**
 * Tests the client-server CGMP relay integration.
 * 
 * Created by almaudoh on 6/2/16.
 */
public class ClientServerCGMPRelayTest {
    
    TestClientCGMPRelayListener clientListener = new TestClientCGMPRelayListener();
    TestServerCGMPRelayListener serverListener = new TestServerCGMPRelayListener();
    
    @Mocked private Socket clientSocket;
    @Mocked private Socket serverSocket;

    private ClientCGMPRelay clientRelay;
    private ServerCGMPRelay serverRelay;

    // Initialize output streams.
    private PipedOutputStream clientOut = new PipedOutputStream();
    private PipedOutputStream serverOut = new PipedOutputStream();
    private PipedInputStream clientIn = new PipedInputStream(serverOut);
    private PipedInputStream serverIn = new PipedInputStream(clientOut);

    public ClientServerCGMPRelayTest() throws IOException { }

    @Test
    public void testClientServerHandshake() throws Exception {
        initializeRelays();

        new MockUp<TestClientCGMPRelayListener>(clientListener) {
            @Mock
            public void messageSent(CGMPMessage message) throws IOException {
                if (message.isHandshake()) {
                    assertEquals(message.getArguments(), "game_client");
                    writeOut(new CGMPMessage(CGMPSpecification.ACK, message.getArguments()).toString());
                }
                else if (message.isRequest() && message.getArguments().equals(CGMPSpecification.PLAY)) {
                    writeOut(new CGMPMessage(CGMPSpecification.ACK, CGMPSpecification.PLAY).toString());
                }
                else if (message.isRequest() && message.getArguments().equals(CGMPSpecification.CARD)) {
                    writeOut(new CGMPMessage(CGMPSpecification.CARD, "Circle 10").toString());
                }
            }

            public void writeOut(String message) throws IOException {
                serverOut.write((message + '\n').getBytes());
            }
        };

        assertTrue(clientRelay.connect("game_client"));
        assertTrue(clientRelay.requestPlay());
        assertEquals(clientRelay.requestCard(), AbstractCard.fromString("Circle 10"));

        new Verifications() {{
           clientListener.messageReceived(CGMPMessage.handShake("game_client"));
        }};
    }

    public void initializeRelays() throws IOException, CGMPException {
        // Expectations.
        new Expectations() {{
            clientSocket.getOutputStream(); returns(clientOut);
            clientSocket.getInputStream(); returns(clientIn);
            serverSocket.getOutputStream(); returns(serverOut);
            serverSocket.getInputStream(); returns(serverIn);
        }};

        clientRelay = new ClientCGMPRelay(clientSocket, clientListener);
        serverRelay = new ServerCGMPRelay(serverSocket, serverListener);

        // Add a low-level logger for the two relays.
        LowLevelCGMPRelayListener clientLLL = Debugger.getLowLevelListener("clientRelay");
        LowLevelCGMPRelayListener serverLLL = Debugger.getLowLevelListener("serverRelay");

        clientRelay.addLowLevelListener(clientLLL);
        serverRelay.addLowLevelListener(serverLLL);
    }
}
