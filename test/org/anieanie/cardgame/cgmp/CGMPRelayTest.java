package org.anieanie.cardgame.cgmp;

import mockit.Expectations;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Verifications;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.net.Socket;

import static org.testng.Assert.*;

/**
 * Created by almaudoh on 5/21/16.
 */
public class CGMPRelayTest {

    @Mocked private Socket socket;
    @Mocked private CGMPRelayListener listener;

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

        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        relay.bufferOut("This is a test message");

        assertEquals(baos.toString(), CGMPSpecification.MARKER + " This is a test message\n");
    }

    @Test
    public void testBufferIn() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = (CGMPSpecification.MARKER + " "  + " This is a test").getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.getOutputStream(); returns(baos);
            socket.getInputStream(); returns(bais);
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        String response = relay.bufferIn(0);

        assertEquals(response, CGMPSpecification.MARKER + " This is a test");
    }

    @Test
    public void testReadMessage() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = (CGMPSpecification.MARKER + " test response").getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);

        new Expectations() {{
            socket.getOutputStream(); returns(baos); times = 1;
            socket.getInputStream(); returns(bais); times = 1;
        }};

        CGMPRelay relay = new TestCGMPRelay(socket, listener);
        CGMPResponse response = relay.readMessage(0);

        assertNotNull(response);
        assertEquals(response.getKeyword(), "test");
        assertEquals(response.getArguments(), "response");
        assertFalse(response.isValidKeyword(), "response");
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
        relay.sendMessage("This is a test message");
    }

//    @Test
    public void testSendError() throws Exception {

    }

//    @Test
    public void testRenewConnection() throws Exception {

    }

//    @Test
    public void testHandleMessage() throws Exception {

    }

}