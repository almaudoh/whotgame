package org.anieanie.cardgame;

import org.anieanie.cardgame.cgmp.CGMPMessage;
import org.anieanie.cardgame.cgmp.ClientCGMPRelayListener;

/**
 * Created by almaudoh on 6/2/16.
 */
public class TestClientCGMPRelayListener implements ClientCGMPRelayListener {
    @Override
    public void moveRequested() {

    }

    @Override
    public void envReceived(String envSpec) {

    }

    @Override
    public void cardReceived(String cardSpec) {

    }

    @Override
    public void gameWon(String winner) {

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
