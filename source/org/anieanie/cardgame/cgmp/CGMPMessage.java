package org.anieanie.cardgame.cgmp;

import static org.anieanie.cardgame.cgmp.CGMPSpecification.*;

/**
 * Encapsulates a complete CGMP message.
 *
 * Created by almaudoh on 5/21/16.
 */
public class CGMPMessage {

    private String protocolString;

    private String keyword;

    private String arguments;

    public static CGMPMessage fromString (String stringResponse) throws InvalidProtocolException {
        if (!isValidProtocol(stringResponse)) {
            throw new InvalidProtocolException();
        }
        String message = stringResponse.substring(MARKER.length()).trim();
        String[] parts = message.split(" ", 2);
        String arguments = (parts.length > 1) ? parts[1] : "";
        return new CGMPMessage(parts[0], arguments);
    }

    public static boolean isValidProtocol(String response) {
        return response.length() > MARKER.length() &&
                response.substring(0, MARKER.length()).equals(MARKER);
    }

    public CGMPMessage(String keyword) {
        this(MARKER, keyword, null);
    }

    public CGMPMessage(String keyword, String arguments) {
        this(MARKER, keyword, arguments);
    }

    // Private constructor
    private CGMPMessage(String protocolString, String keyword, String arguments) {
        this.protocolString = protocolString;
        this.keyword = keyword;
        this.arguments = arguments;
    }

    public boolean isValidProtocol() {
        return this.protocolString.equals(MARKER);
    }

    public boolean isValidKeyword() {
        return CGMPSpecification.isValidKeyword(keyword);
    }

    public boolean isValidResponse() {
        return isValidProtocol() && CGMPSpecification.isValidKeyword(keyword) && CGMPSpecification.isValidSyntax(keyword, arguments);
    }

    public String getKeyword() {
        return keyword;
    }

    public String getArguments() {
        return arguments;
    }

    // Message type helpers.
    public boolean isAcknowledgement() {
        return keyword.equals(ACK);
    }

    public boolean isHandshake() {
        return keyword.equals(HANDSHAKE);
    }

    public boolean isRequest() {
        return keyword.equals(REQ);
    }

    public boolean isMove() {
        return keyword.equals(MOVE);
    }

    public boolean isCard() {
        return keyword.equals(CARD);
    }

    public boolean isError() {
        return keyword.equals(ERR);
    }

    public int getError() {
        if (isError()) {
            return Integer.parseInt(arguments);
        }
        else {
            return CGMPSpecification.Error.NO_ERROR;
        }
    }

    public String toString() {
        return protocolString + " " + keyword + (arguments == null || arguments.equals("") ? "" : " " + arguments);
    }

    // Helper methods.
    public static CGMPMessage acknowledgement() {
        return acknowledgement(null);
    }

    public static CGMPMessage acknowledgement(String argument) {
        return new CGMPMessage(ACK, argument);
    }

    public static CGMPMessage request(String request) {
        return new CGMPMessage(REQ, request);
    }

    public static CGMPMessage terminate() {
        return new CGMPMessage(TERM);
    }

    public static CGMPMessage error(int errorcode) {
        return new CGMPMessage(ERR, String.valueOf(errorcode));
    }

    public static CGMPMessage handShake(String name) {
        return new CGMPMessage(HANDSHAKE, name);
    }

}

