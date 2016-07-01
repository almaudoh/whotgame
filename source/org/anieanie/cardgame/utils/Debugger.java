package org.anieanie.cardgame.utils;

import org.anieanie.cardgame.cgmp.CGMPMessage;
import org.anieanie.cardgame.cgmp.LowLevelCGMPRelayListener;

import java.io.*;

/**
 * Debugger class.
 */
public class Debugger {
    public static LowLevelCGMPRelayListener getLowLevelListener(final String name) throws FileNotFoundException {
        return new LowLevelCGMPRelayListener() {
            PrintStream logStream = new PrintStream(new FileOutputStream(new File("debugger.log")));

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
//                System.err.println(name + ": ==> " + message + " [Thread " + Thread.currentThread().getName() + "]");
                logStream.println(name + ": ==> " + message + " [Thread " + Thread.currentThread().getName() + "]");
            }

            @Override
            public void onBufferIn(String message) {
//                System.err.println(name + ": <== " + message + " [Thread " + Thread.currentThread().getName() + "]");
                logStream.println(name + ": <== " + message + " [Thread " + Thread.currentThread().getName() + "]");
            }
        };
    }
}
