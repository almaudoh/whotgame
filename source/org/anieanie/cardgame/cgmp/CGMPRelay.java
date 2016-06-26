/*
 * CGMPRelay.java
 *
 * Created on February 20, 2005, 11:20 PM
 */

package org.anieanie.cardgame.cgmp;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * The CGMPRelay class defines the active component in the system of communication between the
 * client (player or watcher) and the worker (the Server object dedicated to that client) in a
 * Card Game. It is the CGMPRelay class that handles the details of communication using the Card
 * Game Messaging Protocol (CGMP). The CGMPRelay object actively listens for messages from the
 * other end and notifies its listener (a CGMPRelayListener object) of such messages when they
 * arrive. The CGMPRelay also transmits messages to the other end.
 *
 * There are two modes in which a CGMPRelay object operates. In the passive or listening mode, the
 * relay scans the socket (by calling the <code>scan()</code> method) for any incoming messages.
 * If any message is received, the corresponding method of the associated CGMPRelayListener object
 * is invoked (e.g. <code>playRequested()</code> or <code>relayTerminated()</code> event method).
 * This is implemented by the developer. The <code>scan()</code> method should be called in a
 * recurring loop in a separate thread.
 * In the active mode, the CGMPRelay object's corresponding method is called to send the desired
 * message (e.g. <code>relay.requestPlay()</code>). In this active mode, the relay will wait until
 * either a response is received or the <code>CGMPSpecification.READ_TIMEOUT</code> lapses. If the
 * response received is bad, the relay will send an error message and continue to wait for another
 * response. This cycle is continued until a valid response is received or the <code>CGMPSpecification.MAX_TRIES</code>
 * is exceeded, at which point the relay assumes nothing was sent and exits the active mode.
 * @author ALMAUDOH
 */
public abstract class CGMPRelay {
    public static boolean debug = false;
    
    protected Socket socket;
    protected CGMPRelayListener listener;
    protected LowLevelCGMPRelayListener[] lowLevelListeners;

    /*
     * Must be synchronized since multiple threads may access them and simultaneous
     * reading may corrupt message
     */
    protected PrintWriter pr;
    protected BufferedReader br;

    public CGMPRelay(Socket socket) {
        this(socket, null);
    }

    /**
     * Creates a new instance of CGMPRelay
     * @param socket The socket to which this CGMPRelay listens
     * @param listener the CGMPRelayListener that responds to the events raised by
     * this CGMPRelay when a message comes in.
     */
    public CGMPRelay(Socket socket, CGMPRelayListener listener) {
        this.socket = socket;
        this.listener = listener;
        this.lowLevelListeners = new LowLevelCGMPRelayListener[] {};
        try {
            pr = new PrintWriter(this.socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            if (pr != null) pr = null;
            if (br != null) br = null;
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
    
    public CGMPRelayListener getListener() {
        return this.listener;
    }

    // @todo Improve to add more than one listener.
    public void setListener(CGMPRelayListener listener) {
        this.listener = listener;
    }

    public void addLowLevelListener(LowLevelCGMPRelayListener listener) {
        lowLevelListeners = Arrays.copyOf(lowLevelListeners, lowLevelListeners.length + 1);
        lowLevelListeners[lowLevelListeners.length - 1] = listener;
    }

    /**
     * Connects the relay to the server and returns success status.
     *
     * @param identifier The name used to identify this relay.
     *
     * @return boolean
     *   TRUE if the connection was successful and acknowledged.
     *
     * @throws CGMPException
     * @throws IOException
     */
    public boolean connect(String identifier) throws CGMPException, IOException {
        // Connect to the socket with the specified identifier. Response should be ACK with the identifier returned.
        CGMPMessage response = sendMessage(CGMPMessage.handShake(identifier), true);
        return response.isAcknowledgement() && response.getArguments().equals(identifier);
    }

    public void disconnect() throws IOException, CGMPException {
        // @todo: What to do before disconnecting?
        if (socket != null) {
            terminateRelay();
            socket.close();
            socket = null;
        }
    }

    /**
     * Scans the socket for any outstanding or impending messages from the other end.
     *
     * This method must be called frequently (every 0.2 - 0.5 seconds recommended) from
     * a separate thread to ensure that messages don't get locked in for too long and that
     * the socket input buffer doesn't overflow.
     * The action of this method is to call the relevant function of its <CODE>listener
     * (CGMPRelayListener)</CODE> according to the message received.
     */
    public CGMPMessage scan() throws IOException, CGMPException {
        CGMPMessage message = readMessage(0);
        handleMessage(message);
        return message;
    }

    /**
     * Used by Game Client or Worker to terminate the connection before closing
     */
    public void terminateRelay() throws IOException, CGMPException {
        sendMessage(CGMPMessage.terminate(), false);
    }

    public void sendAcknowledgement() throws CGMPException, IOException {
        sendMessage(CGMPMessage.acknowledgement(), false);
    }

    public void sendAcknowledgement(String argument) throws CGMPException, IOException {
        sendMessage(CGMPMessage.acknowledgement(argument), false);
    }

    public void sendRejection() throws IOException, CGMPException {
        sendMessage(new CGMPMessage(CGMPSpecification.NAK), false);
    }

    public CGMPMessage sendRequest(String request) throws CGMPException, IOException {
        return sendMessage(CGMPMessage.request(request), true);
    }

    public void sendNonBlockingRequest(String request) throws CGMPException, IOException {
        sendMessage(CGMPMessage.request(request), false);
    }

    public void sendInformation(String information) throws CGMPException, IOException {
        sendMessage(new CGMPMessage(CGMPSpecification.INFO, information), false);
    }

    /**
     * Handles message transmission, verification and error correction/recovery
     *
     * This method receives the reply and does error correction/recovery on the reply.
     * A CGMPException is thrown if <code>msg</code> is invalid or - more precisely -,
     * if the CGMPRelay on the other end of the connection returns an error message.
     *
     * @param msg Message to be sent.
     *
     * @param readBack true if the relay should read back a response.
     *
     */
    protected synchronized CGMPMessage sendMessage(CGMPMessage msg, boolean readBack) throws CGMPException, IOException {
        // Only one thread can do sendMessage or readMessage at a time.
        if (socket.isClosed()) {
            renewConnection();
        } else if (pr.checkError()) {
            throw new CGMPException("Error on remote end of socket. Cannot send message");
        }
        CGMPMessage response;
        // Clear read and write buffers.
        pr.flush();
        while (br.ready() && br.read() != 0) {}
        bufferOut(msg);
        onSendMessage(msg);

        // Only read back if needed.
        if (readBack) {
            response = readMessage(0);
            /* Error handling code */
//            if (response.isError()) {
                // @todo Should we be pushing out ACK's for errors too???
//                bufferOut(CGMPMessage.acknowledgement());
//                onReceiveError(response.getError());
//            }
            return response;
        }
        return null;
    }
    
    protected synchronized CGMPMessage readMessage(int attempts) throws CGMPException, IOException {
        // Only one thread can do sendMessage or readMessage at a time.
        if (socket.isClosed()) {
            renewConnection();
        }
        if (attempts++ > CGMPSpecification.MAX_TRIES) {
            throw new CGMPConnectionException("Failed to read valid message from remote CGMP peer: maximum tries exceeded");
        }
        String response = bufferIn(CGMPSpecification.READ_TIMEOUT);
        if (response == null) {
            if (attempts > 0) {
                throw new CGMPConnectionException("Failed to read valid message from remote CGMP peer: maximum tries exceeded");
            } else {
                throw new CGMPConnectionException("No message received from remote CGMP peer: maximum tries exceeded");
            }
        }

        CGMPMessage cgmpResponse = CGMPMessage.fromString(response);

        /* Check for correct protocol specification */
        if (!cgmpResponse.isValidProtocol()) {
            sendError(CGMPSpecification.Error.BAD_PROTO);
            // Further read attempts.
            return readMessage(attempts);
        }

        /* Check for correct keyword */
        if (!cgmpResponse.isValidKeyword()) {
            sendError(CGMPSpecification.Error.BAD_KWD);
            // Further read attempts.
            return readMessage(attempts);
        }

        /* Check for correct syntax */
        if (!cgmpResponse.isValidResponse()) {
            sendError(CGMPSpecification.Error.BAD_SYN);
            // Further read attempts.
            return readMessage(attempts);
        }
        onReceiveMessage(cgmpResponse);
        return cgmpResponse;
    }
    
    public synchronized void sendError(int errorcode) {
        String resp = "";
        try {
            // Using this method to clear buffer.
            while (br.ready() && br.read() != 0) {
            }
            bufferOut(CGMPMessage.error(errorcode));
            int i = 1;
            do {
                resp = bufferIn(CGMPSpecification.READ_TIMEOUT);
                if (resp.equals(CGMPMessage.acknowledgement().toString())) break;

                bufferOut(CGMPMessage.error(errorcode));
            }
            while (i++ < CGMPSpecification.MAX_TRIES);
            onSendError(errorcode);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (Exception e) {
        }
    }

    protected void bufferOut(CGMPMessage message) {
        pr.println(message);
        onBufferOut(message.toString());
    }

    /**
     *
     * @param timeout The duration in milliseconds to keep trying to read before timing out.
     * @return The input read as a string or null if timed out.
     * @throws IOException
     */
    protected String bufferIn(int timeout) throws IOException {
        int max_cycles = 100;

        // Make at most 100 attempts within the specified timeout.
        for (int cycles = 0; !br.ready() && cycles < max_cycles; cycles++) {
            try {
                Thread.sleep(timeout / max_cycles);
            } catch (InterruptedException e) {
                // TODO: 5/21/16
                e.printStackTrace();
            }
        }
        if (br.ready()) {
            String message = br.readLine();
            onBufferIn(message);
            return message;
        }
        else {
            return null;
        }
    }
    
    private boolean renewConnection() {
        try {
            socket.connect(socket.getRemoteSocketAddress());
            pr = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        } catch (IOException ex) {
            if (pr != null) pr = null;
            if (br != null) br = null;
            ex.printStackTrace();
        }
        return false;
    }
    
    protected abstract void handleMessage(CGMPMessage response);

    // Events
    protected void onSendMessage(CGMPMessage message) {
        if (listener != null) listener.messageSent(message);
    }

    protected void onReceiveMessage(CGMPMessage message) {
        if (listener != null) listener.messageReceived(message);
    }

    protected void onSendError(int errorcode) {
        if (listener != null) listener.errorSent(errorcode);
    }

    protected void onReceiveError(int errorcode) {
        if (listener != null) listener.errorReceived(errorcode);
    }

    // Low-level events
    protected void onBufferOut(String message) {
        for (LowLevelCGMPRelayListener listener : lowLevelListeners) {
            listener.onBufferOut(message);
        }
    }

    protected void onBufferIn(String message) {
        for (LowLevelCGMPRelayListener listener : lowLevelListeners) {
            listener.onBufferIn(message);
        }
    }

    // Override finalize() to close socket
    public void finalize() {
        System.out.println("Finalizing relay: ...");
        try {
            disconnect();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
    }

}