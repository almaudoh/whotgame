/*
 * CGMPRelayListener.java
 *
 * Created on February 21, 2005, 2:33 PM
 */

package org.anieanie.cardgame.cgmp;

/**
 * The interface for all objects that respond to CGMPRelay messages
 *
 * The CGMPRelayListener interface defines some methods (event handlers if you will) that
 * are invoked when a CGMPRelay object receives certain signals or messages from its associated
 * socket. This interface allows objects that implement it to respond to such messages. All
 * methods in this interface have corresponding methods in the CGMPRelay class, the relationship
 * being that when a CGMPRelay object method is called from one end of the socket (e.g. the client)
 * the corresponding method in the CGMPRelayListener class is called at the other end of the socket
 * (e.g. the server) and the return value (which are the same in all cases) is transferred across
 *
 * @author  aaudoh1
 */
public interface CGMPRelayListener {

    /** Called when the client or worker is terminated */
    void relayTerminated();
    
    void messageSent(CGMPMessage message);

    void messageReceived(CGMPMessage message);

    void errorSent(int errorcode);

    void errorReceived(int errorcode);

    void infoReceived(String info);
}
