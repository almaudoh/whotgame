/*
 * CGMPSpecification.java
 *
 * Created on February 21, 2005, 2 {51 PM
 */

package org.anieanie.cardgame.cgmp;

import java.lang.reflect.Field;

/**
 * The CGMPSpecification class holds information on the Card Game Messaging Protocol (CGMP)
 *
 * This protocol is developed as a language of communication between servers and clients
 * playing the card game. This class defines the constants used in the syntax of CGMP
 *
 * <div align="center">Card Game Messaging Protocol version 0.5 -- CGMP 0.5</div>
 * The card game messaging protocol is defined in two sections { 1. Protocol definition (actions)
 * and 2. Protocol Syntax
 *
 * Protocol Definition
 * CGMP follows the procedures or steps outlined below in communicating statements between a game
 * server and client. There are three types of game statements { Requests, Actions and Replies
 *
 * Requests
 * Request description &nbsp; &nbsp; &nbsp; Direction &nbsp; &nbsp; &nbsp; &nbsp; Response
 * <ol>
 * <li>Request to play  &nbsp; &nbsp; &nbsp; &nbsp; client to server &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>Request to watch &nbsp; &nbsp; &nbsp; &nbsp; client to server &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>Request for environment &nbsp; &nbsp; &nbsp; client to server &nbsp; &nbsp; &nbsp; &nbsp; ENVR, WAIT, NAK
 * <li>Request for move &nbsp; &nbsp; &nbsp; &nbsp; server to client &nbsp; &nbsp; &nbsp; &nbsp; MOVE, WAIT, NAK
 * <li>Request for card &nbsp; &nbsp; &nbsp; &nbsp; client to server &nbsp; &nbsp; &nbsp; &nbsp; CARD, WAIT, NAK
 * </ol>
 *
 * Actions
 * Action description &nbsp; &nbsp; &nbsp; Direction &nbsp; &nbsp; &nbsp; &nbsp; Response
 * <ol>
 * <li>Environment (game position) &nbsp; &nbsp; server to client &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>Confirmation of move &nbsp; &nbsp; &nbsp; server to client &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>Send Move &nbsp; &nbsp; &nbsp;  &nbsp; client to server &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>Send Card &nbsp; &nbsp; &nbsp;  &nbsp; server to client &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>State Game winner &nbsp; &nbsp; &nbsp; server to client &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * <li>Terminate Connection     &nbsp; &nbsp; both ways &nbsp; &nbsp; &nbsp; &nbsp; ACK, NAK
 * </ol>
 *
 * Replies
 * <ul>
 * <li>Acknowledgement</li>
 * <li>Negative Acknowledgement</li>
 * <li>Wait</li>
 * <li>Error</li>
 * </ul>
 *
 * These  statements are used in the protocol procedure defined below.
 * <h3><u>Protocol Definition/Procedure</u></h3>
 * <ol>
 * <li>Client notifies Server of its availability to watch or play the game</li>
 * <li>Server adds client to list of players/watchers if possible and responds with ACK</li>
 * <li>If not possible, server responds with NAK</li>
 * <li>Game starts, server deals cards as follows {
 *    <ul>
 *      <li>Server loops through all registered players and</li>
 *      <li>for each of them sends a card</li>
 *      <li>Server does a certain number of loops to ensure all players get the required number of cards</li>
 *    </ul>
 * <li>Game starts, server sends current game position (Environment) to client & requests for a move</li>
 * <li>Client calculates move and sends move to server</li>
 * <li>Server confirms that move is valid and confirms move, sends ACK to client else sends NAK if move is invalid</li>
 * <li>If client receives ACK, plays move else client repeats from 5. above</li>
 * <li>Server receives move and starts step 4. again for the next client.</li>
 * <li>In between all these, all other clients can make other requests. Requests to play are countered with NAK, requests for environment are appropriately replied.</li>
 * <li>When the game is won, Server notifies all that game is won stating the winner.</li>
 * </ol>
 *
 * 2. Syntax
 * Requests
 * Request to play  &nbsp; &nbsp; &nbsp; REQ PLAY
 * Request to watch &nbsp; &nbsp; &nbsp; REQ VIEW
 * Request for environment &nbsp; &nbsp; REQ ENVR
 * Request for move &nbsp; &nbsp; &nbsp; REQ MOVE
 * Request for card &nbsp; &nbsp; &nbsp; REQ CARD
 *
 * Actions
 * Send current environment &nbsp; &nbsp; ENVR [environment specs]
 * Send move  &nbsp; &nbsp; &nbsp; &nbsp; MOVE [move specs]
 * Send card  &nbsp; &nbsp; &nbsp; &nbsp; CARD [card specs]
 * State Game winner &nbsp; &nbsp; &nbsp; WON [winner specs]
 * Terminate Connection     &nbsp; &nbsp; TERM
 *
 * Replies
 * Acknowledgement   &nbsp; &nbsp; &nbsp; &nbsp; ACK
 * Negative Acknowledgement &nbsp; &nbsp; &nbsp; NAK
 * Wait while processing  &nbsp; &nbsp; &nbsp;   WAIT
 * Error in syntax  &nbsp; &nbsp; &nbsp; &nbsp;  ERR
 *
 * @author  aaudoh1
 */
public final class CGMPSpecification {
    
    public static final String VERSION = "0.5";

    /**
     * Constant holding MAGIC
     */
    public static final String MARKER = "CGMP 0.5:";

    /**
     * Constant holding value of request keyword
     */
    public static final String REQ = "REQ";

    /**
     * Constant holding value of play keyword
     */
    public static final String PLAY = "PLAY";

    /**
     * Constant holding value of watch keyword
     */
    public static final String VIEW = "VIEW";

    /**
     * Constant holding value of environment keyword
     */
    public static final String ENVR = "ENVR";

    /**
     * Constant holding value of move keyword
     */
    public static final String MOVE = "MOVE";

    /**
     * Constant holding value of move accept keyword
     */
    public static final String MACK = "MACK";

    /**
     * Constant holding value of card keyword
     */
    public static final String CARD = "CARD";

    /**
     * Constant holding value of acknowledgement keyword
     */
    public static final String ACK = "ACK";

    /**
     * Constant holding value of negative acknowledgement keyword
     */
    public static final String NAK = "NAK";

    /**
     * Constant holding value of wait for processing keyword
     */
    public static final String WAIT = "WAIT";

    /**
     * Constant holding value of error keyword
     */
    public static final String ERR = "ERR";

    /**
     * Constant holding value of game winner keyword
     */
    public static final String WON = "WON";

    /**
     * Constant holding value of game winner keyword
     */
    public static final String TERM = "TERM";

    /**
     * Constant holding the value for the client-server handshake.
     */
    public static final java.lang.String HANDSHAKE = "HELLO";

    /**
     * The maximum number of times a CGMPRelay will retry to read from a socket that
     * gives bad data before continuing as if no data was read
     */
    public static int MAX_TRIES = 5;

    /**
     * The number of seconds a CGMPRelay will block waiting to read from a socket's
     * inputstream before continuing as if no data was read
     */
    public static int READ_TIMEOUT = 10;

    /**
     * Compare the supplied argument with the valid keywords in this class
     * I'm using reflection because more keywords may be added in future
     * @param keyword The keyword whose validity is to be confirmed
     * @return true or false
     */
    public static boolean isValidKeyword(String keyword) {
        try {
            String fldType;
            java.lang.reflect.Field[] fields = Class.forName("org.anieanie.cardgame.cgmp.CGMPSpecification").getDeclaredFields();
            for (Field field : fields) {
                fldType = field.getType().toString().trim();
                fldType = (fldType.indexOf(' ') > -1) ? fldType.substring(fldType.indexOf(' ')).trim() : fldType;
                if (fldType.equals("java.lang.String") && field.get(null).equals(keyword)) return true;
            }
            return false;
        }
//        catch (ClassNotFoundException e) {
//            return false;
//        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidSyntax(String op, String arg) {
        if (op.equals(REQ)) {
            return arg.equals(PLAY) || arg.equals(VIEW) || arg.equals(ENVR) || arg.equals(MOVE) || arg.equals(CARD);
        }

        else if (op.equals(ENVR)) {
            return true;
        }

        else if (op.equals(CARD)) {
            return true;
        }

        else if (op.equals(MOVE)) {
            return true;
        }

        else if (op.equals(MACK)) {
            return true;
        }

        else if (op.equals(WON)) {
            return true;
        }

        else if (op.equals(TERM)) {
            return true;
        }

        else if (op.equals(ACK)) {
            return true;
        }

        else if (op.equals(NAK)) {
            return true;
        }

        else if (op.equals(WAIT)) {
            return true;
        }

        else if (op.equals(ERR)) {
            return true;
        }

        else if (op.equals(HANDSHAKE)) {
            return true;
        }

        else {
            return false;
        }
    }

    /** Creates a new instance of CGMPSpecification */
    private CGMPSpecification() {
    }

    /**
     * The Error inner class contains information on errors and error codes
     *
     * These errors are used only in the internal communication between CGMPRelay objects
     * to specify errors arising in the use of the protocol itself
     */
    public static final class Error {
        /**
         * The types of errors encountered in CGMP transmission
         */
        private static final String[] inWords = {
            "An inappropriate message was received",
            "The message contains an unrecognised keyword",
            "The message has bad syntax",
            "The message has wrong protocol specification"
        };

        /** No error */
        public static final int NO_ERROR = 0;

        /** Inappropriate message error */
        public static final int BAD_MSG = 1;

        /** Unrecognised keyword error */
        public static final int BAD_KWD = 2;

        /** Syntax error */
        public static final int BAD_SYN = 3;

        /** Protocol specifier not given */
        public static final int BAD_PROTO = 4;

        public static String describeError(int errorcode) {
            try {
                return inWords[errorcode];
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("invalid error code supplied");
            }
        }
    }
    
    /**
     * This class holds default connection information for sockets
     */
    public static final class Connection {
       
        /** Default connection port */
        public static final int DEFAULT_PORT = 5550;
        
        /** Default connection ip */
        public static final String DEFAULT_IP = "127.0.0.1";
        
    }

}
