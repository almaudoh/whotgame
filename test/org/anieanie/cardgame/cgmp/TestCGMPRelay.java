package org.anieanie.cardgame.cgmp;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by almaudoh on 5/29/16.
 */
public class TestCGMPRelay extends CGMPRelay {

    /**
     * Creates a new instance of CGMPRelay
     *
     * @param s The socket to which this CGMPRelay listens
     * @param l the CGMPRelayListener that responds to the events raised by
     */
    public TestCGMPRelay(Socket s, CGMPRelayListener l) {
        super(s, l);
    }

    public TestCGMPRelay() {
        super(new TestSocket(null, null), null);
    }

    @Override
    public synchronized CGMPResponse readMessage(int attempts) throws CGMPException {
        return super.readMessage(attempts);
    }

    @Override
    public void bufferOut(String command) {
        super.bufferOut(command);
    }

    @Override
    public String bufferIn(int timeout) throws IOException {
        return super.bufferIn(timeout);
    }

    @Override
    public synchronized CGMPResponse sendMessage(String message) throws CGMPException, IOException {
        return super.sendMessage(message);
    }
    @Override
    protected void handleResponse(CGMPResponse response) {

    }
}
