package org.anieanie.cardgame;

import org.anieanie.cardgame.cgmp.CGMPMessage;
import org.anieanie.cardgame.cgmp.ServerCGMPRelayListener;

/**
 * Created by almaudoh on 6/2/16.
 */
public class TestServerCGMPRelayListener implements ServerCGMPRelayListener {
    @Override
    public void playRequested() {

    }

    @Override
    public void viewRequested() {

    }

    @Override
    public void environmentRequested() {

    }

    @Override
    public void cardRequested() {

    }

    @Override
    public void moveReceived(String move) {

    }

    @Override
    public void clientConnected(String identifier) {

    }

    @Override
    public void gameStartRequested() {

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

    @Override
    public void infoReceived(String info) {

    }
}
