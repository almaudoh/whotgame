package org.anieanie.cardgame.utils;

import org.anieanie.cardgame.cgmp.CGMPMessage;
import org.anieanie.cardgame.cgmp.LowLevelCGMPRelayListener;

/**
 * Created by almaudoh on 6/2/16.
 */
public class Debugger {
    public static LowLevelCGMPRelayListener getLowLevelListener(final String name) {
        return new LowLevelCGMPRelayListener() {
            @Override
            public void relayTerminated() {}

            @Override
            public void messageSent(CGMPMessage message) {}

            @Override
            public void messageReceived(CGMPMessage message) {}

            @Override
            public void errorSent(int errorcode) {}

            @Override
            public void errorReceived(int errorcode) {}

            @Override
            public void infoReceived(String info) {}

            @Override
            public void onBufferOut(String message) {
                System.out.println(name + ": ==> " + message + " [Thread " + Thread.currentThread().getName() + "]");
            }

            @Override
            public void onBufferIn(String message) {
                System.out.println(name + ": <== " + message + " [Thread " + Thread.currentThread().getName() + "]");
            }
        };
    }
}
