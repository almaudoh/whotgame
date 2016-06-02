package org.anieanie.cardgame.cgmp;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;
import java.net.Socket;
import static org.testng.Assert.*;

/**
 * Tests for the CGMPRelay abstract class.
 *
 * Created by almaudoh on 5/21/16.
 */
public class CGMPRelayTest {

    @Mocked private Socket socket;
    @Mocked private LowLevelCGMPRelayListener listener;

    @BeforeClass
    public void setUp() {
//        socket = new TestSocket(inputStream, outputStream);
    }

//    @Test
    public void testTerminateRelay() throws Exception {
        CGMPRelay relay = new TestCGMPRelay(socket, null);
        relay.terminateRelay();
    }

//    @Test
    public void testScan() throws Exception {

    }

//    @Test
    public void testGetSocket() throws Exception {

    }

//    @Test
    public void testGetListener() throws Exception {

    }

//    @Test
    public void testSendAcknowledgement() throws Exception {

    }

    @Test
    public void testBufferOut() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[30]);

        final String expected_output = CGMPSpecification.MARKER + " This is a test message";
        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
            listener.onBufferOut(expected_output); times = 1;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        relay.bufferOut(new CGMPMessage("This is a test message"));

        // A newline will come with the output pushed into the output stream.
        assertEquals(baos.toString(), expected_output + "\n");
    }

    @Test
    public void testBufferIn() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String input = CGMPSpecification.MARKER + " This is a test";
        byte[] b = input.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
            listener.onBufferIn(input); times = 1;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        String response = relay.bufferIn(0);

        assertEquals(response, CGMPSpecification.MARKER + " This is a test");
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
        final String expected_output = CGMPSpecification.MARKER + " " + CGMPSpecification.ERR + " 10\n";
        final String expected_reply = CGMPSpecification.MARKER + " " + CGMPSpecification.ACK;

        // A fake LowLevelCGMPRelayListener to allow reply to be mocked after CGMPRelay.bufferOut().
        LowLevelCGMPRelayListener fake_listener = new MockUp<LowLevelCGMPRelayListener>() {
            @Mock
            void onBufferOut(String message) throws IOException {
                assertNotEquals(message, expected_output);
                // write() needs to have the newline appended at the end.
                pos.write((expected_reply + '\n').getBytes());
                pos.flush();
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
        }};

        // Exercise the method under test.
        CGMPRelay relay = new TestCGMPRelay(socket, fake_listener);
        relay.sendError(10);

        // Assertions and verifications.
        assertEquals(baos.toString(), expected_output);
        assertEquals(reply.toString(), expected_reply);
    }

    @Test
    public void testReadMessage() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = (CGMPSpecification.MARKER + " " + CGMPSpecification.ENVR + " response").getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.isClosed(); returns(true);
            socket.getOutputStream(); returns(baos); times = 2;
            socket.getInputStream(); returns(bais); times = 2;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        CGMPMessage response = relay.readMessage(0);

        assertNotNull(response);
        assertEquals(response.getKeyword(), CGMPSpecification.ENVR);
        assertEquals(response.getArguments(), "response");
    }

    @Test (expectedExceptions = {CGMPConnectionException.class})
    public void testReadMessageWithException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = (CGMPSpecification.MARKER + " test response").getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        CGMPMessage response = relay.readMessage(0);
    }

//    @Test
    public void testSendMessage() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[30]);

        new Expectations() {{
//            socket.getOutputStream(); returns(baos); times = 3;
//            socket.getInputStream(); returns(bais); times = 3;
            socket.isClosed(); returns(true);
            socket.getOutputStream(); returns(baos); times = 3;
            socket.getInputStream(); returns(bais); times = 3;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, null);
        relay.sendMessage(CGMPMessage.request(CGMPSpecification.CARD));
    }

//    @Test
    public void testRenewConnection() throws Exception {

    }

//    @Test
    public void testHandleMessage() throws Exception {

    }

}