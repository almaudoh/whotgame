package org.anieanie.cardgame.cgmp;

import java.util.Arrays;

/**
 * Allows multiple instances of listeners of the same type to be treated as a single item.
 *
 * Created by almaudoh on 6/2/16.
 */
public class MultiCGMPRelayListener implements CGMPRelayListener {

    private static final String CLIENT = "CLIENT";
    private static final String SERVER = "SERVER";

    protected CGMPRelayListener[] listeners;

    // Static factory.
    public static MultiCGMPRelayListener create(String type) {
        if (type.equalsIgnoreCase(CLIENT)) {
            return new ClientMultiCGMPRelayListener();
        }
        else if (type.equalsIgnoreCase(SERVER)) {
            return new ServerMultiCGMPRelayListener();
        }
        else {
            return new MultiCGMPRelayListener();
        }
    }

    // Static factory with initialization.
    public static MultiCGMPRelayListener create(String type, CGMPRelayListener[] listeners) {
        if (type.equalsIgnoreCase(CLIENT)) {
            return new ClientMultiCGMPRelayListener((ClientCGMPRelayListener[]) listeners);
        }
        else if (type.equalsIgnoreCase(SERVER)) {
            return new ServerMultiCGMPRelayListener((ServerCGMPRelayListener[]) listeners);
        }
        else {
            return new MultiCGMPRelayListener(listeners);
        }
    }

    // Constructors
    public MultiCGMPRelayListener() {
        this.listeners = new CGMPRelayListener[] {};
    }

    public MultiCGMPRelayListener(CGMPRelayListener[] listeners) {
        this.listeners = listeners;
    }

    public void addListener(CGMPRelayListener listener) {
        listeners = Arrays.copyOf(listeners, listeners.length + 1);
        listeners[listeners.length - 1] = listener;
    }

    public CGMPRelayListener[] getListeners() {
        return this.listeners;
    }

    public void setListeners(CGMPRelayListener[] listeners) {
        this.listeners = listeners;
    }

    @Override
    public void relayTerminated() {
        for (CGMPRelayListener listener : listeners) {
            listener.relayTerminated();
        }
    }

    @Override
    public void messageSent(CGMPMessage message) {
        for (CGMPRelayListener listener : listeners) {
            listener.messageSent(message);
        }
    }

    @Override
    public void messageReceived(CGMPMessage message) {
        for (CGMPRelayListener listener : listeners) {
            listener.messageReceived(message);
        }
    }

    @Override
    public void errorSent(int errorcode) {
        for (CGMPRelayListener listener : listeners) {
            listener.errorSent(errorcode);
        }
    }

    @Override
    public void errorReceived(int errorcode) {
        for (CGMPRelayListener listener : listeners) {
            listener.errorReceived(errorcode);
        }
    }

    @Override
    public void infoReceived(String info) {
        for (CGMPRelayListener listener : listeners) {
            listener.infoReceived(info);
        }
    }

    public static class ServerMultiCGMPRelayListener extends MultiCGMPRelayListener implements ServerCGMPRelayListener {

        // Constructors
        public ServerMultiCGMPRelayListener() {
            this.listeners = new ServerCGMPRelayListener[] {};
        }

        public ServerMultiCGMPRelayListener(ServerCGMPRelayListener[] listeners) {
            this.listeners = listeners;
        }

        @Override
        public void playRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.playRequested();
            }
        }

        @Override
        public void viewRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.viewRequested();
            }
        }

        @Override
        public void environmentRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.environmentRequested();
            }
        }

        @Override
        public void cardRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.cardRequested();
            }
        }

        @Override
        public void moveReceived(String move) {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.moveReceived(move);
            }
        }

        @Override
        public void clientConnected(String identifier) {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.clientConnected(identifier);
            }
        }

        @Override
        public void gameStartRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.gameStartRequested();
            }
        }
    }

    public static class ClientMultiCGMPRelayListener extends MultiCGMPRelayListener implements ClientCGMPRelayListener {

        // Constructors
        public ClientMultiCGMPRelayListener() {
            this.listeners = new ClientCGMPRelayListener[] {};
        }

        public ClientMultiCGMPRelayListener(ClientCGMPRelayListener[] listeners) {
            this.listeners = listeners;
        }

        @Override
        public void moveRequested() {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.moveRequested();
            }
        }

        @Override
        public void environmentReceived(String envSpec) {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.environmentReceived(envSpec);
            }
        }

        @Override
        public void cardReceived(String cardSpec) {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.cardReceived(cardSpec);
            }
        }

        @Override
        public void gameWon(String winner) {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.gameWon(winner);
            }
        }
    }
}
