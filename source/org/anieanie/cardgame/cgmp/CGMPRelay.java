/*
 * CGMPRelay.java
 *
 * Created on February 20, 2005, 11:20 PM
 */

package org.anieanie.cardgame.cgmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
    
    protected Socket sock;
    protected CGMPRelayListener listener;
    
    /*
     * Must be synchronized since multiple threads may access them and simultaneous
     * reading may corrupt message
     */
    protected PrintWriter pr;
    protected BufferedReader br;

    public CGMPRelay(Socket s) {
        this(s, null);
    }

    /**
     * Creates a new instance of CGMPRelay
     * @param s The socket to which this CGMPRelay listens
     * @param l the CGMPRelayListener that responds to the events raised by
     * this CGMPRelay when a message comes in.
     */
    public CGMPRelay(Socket s, CGMPRelayListener l) {
        sock = s;
        listener = l;
        try {
            pr = new PrintWriter(sock.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            if (pr != null) pr = null;
            if (br != null) br = null;
        }
    }
    
    // Override finalize() to close socket
    public void finalize() {
        System.out.println("Finalizing relay: ...");
        if (sock != null) {
            terminateRelay();
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sock = null;
        }
    }
    
    /**
     * Used by Game Client or Worker to terminate the connection before closing
     */
    public void terminateRelay() {
        try {
            sendMessage(CGMPSpecification.TERM);
        }
        catch (CGMPException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** Scans the socket for any outstanding or impending messages from the other end.
     *
     * This method must be called frequently (every 0.2 - 0.5 seconds recommended) from
     * a separate thread to ensure that messages don't get locked in for too long and that
     * the socket input buffer doesn't overflow.
     * The action of this method is to call the relevant function of its <CODE>listener
     * (CGMPRelayListener)</CODE> according to the message received.
     */
    public void scan() {
        if (debug) System.out.println("in scan()");

        CGMPResponse response = null;
        try {
            response = readMessage(0);
            handleResponse(response);
        } catch (CGMPException e) {
            e.printStackTrace();
        }
    }
    
    public Socket getSocket() {
        return this.sock;
    }
    
    public CGMPRelayListener getListener() {
        return this.listener;
    }

    // @todo Improve to add more than one listener.
    public void setListener(CGMPRelayListener listener) {
        this.listener = listener;
    }
    
    public CGMPResponse sendAcknowledgement() throws CGMPException, IOException {
        return sendMessage(CGMPSpecification.ACK);
    }

    public CGMPResponse sendRequest(String request) throws CGMPException, IOException {
        return sendMessage(CGMPSpecification.REQ + " " + request);
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
     */
    protected CGMPResponse sendMessage(String msg) throws CGMPException, IOException {
        if (sock.isClosed()) {
            renewConnection();
        }
        else if (pr.checkError()) {
            throw new CGMPException("Error on remote end of socket. Cannot send message");
        }
        CGMPResponse response;
        msg = msg.trim();
        // Clear buffers.
        pr.flush();
        if (br.ready()) br.skip(1000);
        synchronized (pr) {
            bufferOut(msg);
            response = readMessage(0);
        }

        /* Error handling code */
        if (response.isError()) {
            synchronized(pr) {
                bufferOut(CGMPSpecification.ACK);
            }
            switch (response.getError()) {
                case CGMPSpecification.Error.BAD_PROTO:
                    // // TODO: 5/21/16
                    // Potential weakness here (stack overflow) if other end continues to send BAD_PROTO errors
                    // regardless of what comes in.
                    return sendMessage(msg);

                case CGMPSpecification.Error.BAD_KWD:
                    throw new CGMPException("Message contains bad keyword");

                case CGMPSpecification.Error.BAD_SYN:
                    throw new CGMPException("Message has wrong syntax");

                case CGMPSpecification.Error.BAD_MSG:
                    throw new CGMPException("Inappropriate message or reply sent");

                default:
                    break;
            }
        }
        else {
            if (debug) System.out.println("message sent: " + msg + "; response received: "+response);
            if (debug) System.out.println("<-- sendMessage(" + msg + ")");
            return response;
        }
        return null;
        
    }
    
    protected synchronized CGMPResponse readMessage(int attempts) throws CGMPException {
        if (debug) System.out.println("--> readMessage(" + attempts + ")");
        if (sock.isClosed()) {
            renewConnection();
        }
        String response;
        try {
            if (debug) System.out.println("socket closed?: "+sock.isClosed());
            synchronized (br) {
                if (attempts++ > CGMPSpecification.MAX_TRIES) return null;
                response = bufferIn(CGMPSpecification.READ_TIMEOUT*10);
                if (response == null) {
                    if (debug) System.out.println("<-- readMessage(" + (attempts-1) + ")");
                    return null;
                }
            }
            if (debug) System.out.println("Message received: "+response);
            
            CGMPResponse cgmpResponse = CGMPResponse.fromString(response);

            /* Check for correct protocol specification */
            if (!cgmpResponse.isValidProtocol()) {
                sendError(CGMPSpecification.Error.BAD_PROTO);
                //if (debug) System.out.println("--> readMessage(" + attempts + ")");
                // Further read attempts.
                return readMessage(attempts);
            }
            
            /* Check for correct keyword */
            if (!cgmpResponse.isValidKeyword()) {
                sendError(CGMPSpecification.Error.BAD_KWD);
                //if (debug) System.out.println("--> readMessage(" + attempts + ")");
                // Further read attempts.
                return readMessage(attempts);
            }
            
            /* Check for correct syntax */
            if (!cgmpResponse.isValidResponse()) {
                sendError(CGMPSpecification.Error.BAD_SYN);
                //if (debug) System.out.println("--> readMessage(" + attempts + ")");
                // Further read attempts.
                return readMessage(attempts);
            }

            if (debug) System.out.println("readMessage(): message (" + cgmpResponse.toString() + ") read successfully");
            if (debug) System.out.println("<-- readMessage(" + (attempts-1) + ")");
            return cgmpResponse;

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // If we are here, we did not succeed in reading.
        throw new CGMPException("Failed to read valid message from remote.");
    }
    
    protected void sendError(int errorcode) {
        String resp = "";
        if (debug) System.out.println("--> sendError(" + errorcode + ")");
        try {
            synchronized (pr) {
                synchronized (br) {
                    if (br.ready()) br.skip(1000); // Using this method to clear buffer
                    bufferOut(CGMPSpecification.ERR + " " + errorcode);
                    int i=1;
                    do {
                        resp = bufferIn(CGMPSpecification.READ_TIMEOUT*10);
                        if (debug) System.out.println(resp);
                        if (resp.equals(CGMPSpecification.MARKER+" "+CGMPSpecification.ACK)) break;
                        
                        bufferOut(CGMPSpecification.ERR + " " + errorcode);
                    } while (i++ < CGMPSpecification.MAX_TRIES);
                }
            }
        } catch (IOException ioe) { ioe.printStackTrace(); } catch (Exception e) {   }
        if (debug) System.out.println("<-- sendError(" + errorcode + ")");
    }

    protected void bufferOut(String command) {
        pr.println(CGMPSpecification.MARKER + " " + command);
        pr.flush();;
    }

    protected String bufferIn(int timeout) throws IOException {
        int time = 0;
        while (!br.ready() && time++ < timeout) {
            /*if (debug) System.out.print(".");*/
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: 5/21/16
                e.printStackTrace();
            }
        }
        // if (debug) System.out.println("finished waiting: " + t + " rounds");
        if (br.ready()) {
            return br.readLine();
        } else {
            return null;
        }
    }
    
    protected final boolean renewConnection() {
        try {
            sock.connect(sock.getRemoteSocketAddress());
            pr = new PrintWriter(sock.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            return true;
        } catch (IOException ex) {
            if (pr != null) pr = null;
            if (br != null) br = null;
            ex.printStackTrace();
        }
        return false;
    }
    
    protected abstract void handleResponse(CGMPResponse response);
}