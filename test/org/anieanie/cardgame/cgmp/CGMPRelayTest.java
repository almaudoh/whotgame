package org.anieanie.cardgame.cgmp;

import mockit.*;

import org.testng.annotations.Test;

import java.io.*;
import java.net.Socket;

import static org.anieanie.cardgame.cgmp.CGMPSpecification.*;
import static org.testng.Assert.*;

/**
 * Tests for the CGMPRelay abstract class.
 *
 * Created by almaudoh on 5/21/16.
 */
public class CGMPRelayTest {

    @Mocked private Socket socket;
    @Mocked private LowLevelCGMPRelayListener listener;

//    @Test
    public void testTerminateRelay() throws Exception {
        CGMPRelay relay = new TestCGMPRelay(socket, null);
        relay.terminateRelay();
    }

//    @Test
    public void testScan() throws Exception {
        // @TODO add assertion / expectation that handleResponse() is invoked.

    }

    @Test
    public void testBufferOut() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[30]);

        final String expected_output = MARKER + " This is a test message";
        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
            listener.onBufferOut(expected_output); times = 1;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        relay.addLowLevelListener(listener);
        relay.bufferOut(new CGMPMessage("This is a test message"));

        // A newline will come with the output pushed into the output stream.
        assertEquals(baos.toString(), expected_output + "\n");
    }

    @Test
    public void testBufferIn() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String input = MARKER + " This is a test";
        byte[] b = input.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
            listener.onBufferIn(input); times = 1;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        relay.addLowLevelListener(listener);
        String response = relay.bufferIn(0);

        assertEquals(response, MARKER + " This is a test");
    }

    @Test
    public void testSendError() throws Exception {
        // Initialize output streams.
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);

        // Temporary stream to store the reply.
        final StringBuffer reply = new StringBuffer();

        // Expected results.
        final String expected_output = MARKER + " " + ERR + " 10";
        final String expected_reply = MARKER + " " + ACK;

        // A fake LowLevelCGMPRelayListener to allow reply to be mocked after CGMPRelay.bufferOut().
        final LowLevelCGMPRelayListener fake_listener = new MockUp<LowLevelCGMPRelayListener>() {
            @Mock
            void onBufferOut(String message) throws IOException {
                assertEquals(message, expected_output);
                // write() needs to have the newline appended at the end.
                pos.write((expected_reply + '\n').getBytes());
            }

            @Mock
            void onBufferIn(String message) throws IOException {
                reply.append(message);
            }
        }.getMockInstance();

        // Expectations.
        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(pis);
//            fake_listener.errorSent(11); times = 3;
            // @todo How to verify that the onBufferOut / onBufferIn events were called.
//            fake_listener.onBufferOut(anyString); times = 1;
//            fake_listener.onBufferIn(expected_reply); times = 1;
        }};

        // Exercise the method under test.
        CGMPRelay relay = new TestCGMPRelay(socket, fake_listener);
        relay.addLowLevelListener(fake_listener);
        relay.sendError(10);

        // Assertions and verifications.
        assertEquals(baos.toString(), expected_output + '\n');
        assertEquals(reply.toString(), expected_reply);
    }

    @Test
    public void testReadMessage() throws Exception {
        // Initialize output streams.
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = (MARKER + " " + ENVR + " response").getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        // Expectations.
        new Expectations() {{
            socket.isClosed(); returns(true);
            socket.getOutputStream(); returns(baos); times = 2;
            socket.getInputStream(); returns(bais); times = 2;
        }};

        // Exercise method under test.
        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        final CGMPMessage response = relay.readMessage(0);

        // Assert responses.
        assertNotNull(response);
        assertEquals(response.getKeyword(), ENVR);
        assertEquals(response.getArguments(), "response");

        // Verify that the relevant event was called.
        new Verifications() {{
            listener.messageReceived(response); times = 1;
        }};
    }

    @Test (expectedExceptions = {CGMPConnectionException.class})
    public void testReadMessageWithException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = (MARKER + " test response").getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        CGMPMessage response = relay.readMessage(0);
    }

    @Test
    public void testSendMessage() throws Exception {
        // Initialize output streams.
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);

        // Expected results.
        final String expected_output = MARKER + " " + REQ + " " + CARD;
        final String expected_reply = MARKER + " " + ACK;

        // A fake CGMPRelayListener to allow reply to be mocked after CGMPRelay.messageSent().
        final CGMPRelayListener fake_listener = new MockUp<CGMPRelayListener>() {
            @Mock
            void messageSent(CGMPMessage message) throws IOException {
                assertEquals(message.toString(), expected_output);
                // write() needs to have the newline appended at the end.
                pos.write((expected_reply + '\n').getBytes());
            }
        }.getMockInstance();

        // Expectations.
        new Expectations() {{
            socket.isClosed(); returns(true);
            socket.getOutputStream(); returns(baos); times = 3;
            socket.getInputStream(); returns(pis); times = 3;
        }};

        // Exercise method under test.
        CGMPRelay relay = new TestCGMPRelay(socket, fake_listener);
        final CGMPMessage reply = relay.sendMessage(CGMPMessage.request(CARD), true);

        // Assert responses.
        assertEquals(baos.toString(), expected_output + '\n');
        assertEquals(reply.toString(), expected_reply);

        // Verify that the relevant event was called.
        new Verifications() {{
            // TODO: 6/2/16
//            fake_listener.messageSent(CGMPMessage.fromString(expected_output)); times = 1;
//            fake_listener.messageReceived(reply); times = 1;
        }};
    }

    @Test
    public void sendingMessageWithFilledInputStream() throws Exception {
        // Load the relay's input stream with random bytes.
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.random() * 100; i++) {
            builder.append(CGMPMessage.request(PLAY).toString());
            builder.append('\n');
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(builder.toString().getBytes());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Expectations.
        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
        }};

        // Exercise method under test. Send acknowledgement and confirm that read buffer is cleared.
        CGMPRelay relay = new TestCGMPRelay(socket, null);
        relay.sendAcknowledgement();

        assertEquals(bais.available(), 0);
    }

    @Test
    public void testConnect() throws Exception {

    }

//    @Test
    public void testRenewConnection() throws Exception {

    }

}