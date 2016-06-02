/*
 * TestClientCGMPRelay.java
 *
 * Created on April 2, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.anieanie.cardgame.cgmp;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Aniebiet
 */
public class TestClientCGMPRelay extends ClientCGMPRelay {

    public TestClientCGMPRelay(Socket s) {
        super(s);
    }

    /** Creates a new instance of TestClientCGMPRelay */
    public TestClientCGMPRelay(Socket s, ClientCGMPRelayListener sl) {
        super(s,sl);
    }
    
    public CGMPMessage sendMessage(CGMPMessage msg) throws CGMPException, IOException {
        return super.sendMessage(msg);
    }
    
}
