/*
 * TestClientCGMPRelay.java
 *
 * Created on April 2, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.anieanie.cardgame;

import java.net.Socket;
import org.anieanie.cardgame.cgmp.*;
import org.anieanie.cardgame.cgmp.ClientCGMPRelayListener;



/**
 *
 * @author Aniebiet
 */
public class TestClientCGMPRelay extends ClientCGMPRelay {
    
    /** Creates a new instance of TestClientCGMPRelay */
    public TestClientCGMPRelay(Socket s, ClientCGMPRelayListener sl) {
        super(s,sl);
    }
    
    public String sendMessage(String msg) throws CGMPException {
        return super.sendMessage(msg);
    }
    
}
