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

    public static class ServerMultiCGMPRelayListener extends MultiCGMPRelayListener implements ServerCGMPRelayListener {

        // Constructors
        public ServerMultiCGMPRelayListener() {
            this.listeners = new ServerCGMPRelayListener[] {};
        }

        public ServerMultiCGMPRelayListener(ServerCGMPRelayListener[] listeners) {
            this.listeners = listeners;
        }

        @Override
        public boolean playRequested() {
            boolean returnval = true;
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                returnval = returnval && listener.playRequested();
            }
            return returnval;
        }

        @Override
        public boolean viewRequested() {
            boolean returnval = true;
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                returnval = returnval && listener.viewRequested();
            }
            return returnval;
        }

        @Override
        public Object envRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.envRequested();
            }
            return null;
        }

        @Override
        public Object cardRequested() {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.cardRequested();
            }
            return null;
        }

        @Override
        public void clientConnected(String identifier) {
            for (ServerCGMPRelayListener listener : (ServerCGMPRelayListener[]) listeners) {
                listener.clientConnected(identifier);
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
        public Object moveRequested() {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.moveRequested();
            }
            return null;
        }

        @Override
        public void moveAccepted(String moveSpec) {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.moveAccepted(moveSpec);
            }
        }

        @Override
        public void envReceived(String envSpec) {
            for (ClientCGMPRelayListener listener : (ClientCGMPRelayListener[]) listeners) {
                listener.envReceived(envSpec);
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
