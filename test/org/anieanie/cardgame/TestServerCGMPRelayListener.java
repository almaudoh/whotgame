package org.anieanie.cardgame;

import org.anieanie.cardgame.cgmp.CGMPMessage;
import org.anieanie.cardgame.cgmp.ServerCGMPRelayListener;

/**
 * Created by almaudoh on 6/2/16.
 */
public class TestServerCGMPRelayListener implements ServerCGMPRelayListener {
    @Override
    public boolean playRequested() {
        return false;
    }

    @Override
    public boolean viewRequested() {
        return false;
    }

    @Override
    public Object envRequested() {
        return null;
    }

    @Override
    public Object cardRequested() {
        return null;
    }

    @Override
    public void clientConnected(String identifier) {

    }

    @Override
    public void relayTerminated() {

    }

    @Override
    public void messageSent(CGMPMessage message) {

    }

    @Override
    public void messageReceived(CGMPMessage message) {

    }

    @Override
    public void errorSent(int errorcode) {

    }

    @Override
    public void errorReceived(int errorcode) {

    }
}
