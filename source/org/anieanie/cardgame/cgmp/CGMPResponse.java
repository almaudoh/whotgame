package org.anieanie.cardgame.cgmp;

/**
 * Created by almaudoh on 5/21/16.
 */
public class CGMPResponse {

    private String protocolString;

    private String keyword;

    private String arguments;

    public static CGMPResponse fromString (String stringResponse) throws InvalidProtocolException {
        if (!isValidProtocol(stringResponse)) {
            throw new InvalidProtocolException();
        }
        String message = stringResponse.substring(CGMPSpecification.MARKER.length()).trim();
        String[] parts = message.split(" ", 2);
        String arguments = (parts.length > 1) ? parts[1] : "";
        return new CGMPResponse(stringResponse.substring(0, CGMPSpecification.MARKER.length()).trim(), parts[0], arguments);
    }

    public static boolean isValidProtocol(String response) {
        return response.length() > CGMPSpecification.MARKER.length() &&
                response.substring(0, CGMPSpecification.MARKER.length()).equals(CGMPSpecification.MARKER);
    }

    // Private constructor
    private CGMPResponse(String protocolString, String keyword, String arguments) {
        this.protocolString = protocolString;
        this.keyword = keyword;
        this.arguments = arguments;
    }

    public boolean isValidProtocol() {
        return this.protocolString.equals(CGMPSpecification.MARKER);
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

    public boolean isError() {
        return keyword.equals(CGMPSpecification.ERR);
    }

    public int getError() {
        if (isError()) {
            return Integer.parseInt(arguments);
        }
        else {
            return CGMPSpecification.Error.NO_ERROR;
        }
    }

}

