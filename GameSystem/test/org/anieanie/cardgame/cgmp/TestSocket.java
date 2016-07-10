package org.anieanie.cardgame.cgmp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Socket subclass used for testing.
 *
 * Created by almaudoh on 5/29/16.
 */
public class TestSocket extends Socket {

    private InputStream inputStream;
    private OutputStream outputStream;

    public TestSocket(InputStream istream, OutputStream ostream) {
        inputStream = istream;
        outputStream = ostream;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
