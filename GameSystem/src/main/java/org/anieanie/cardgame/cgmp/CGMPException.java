/*
 * CGMPException.java
 *
 * Created on February 21, 2005, 1:11 PM
 */

package org.anieanie.cardgame.cgmp;

/**
 *
 * @author  aaudoh1
 */
public class CGMPException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>CGMPException</code> without detail message.
     */
    public CGMPException() {
    }
    
    public CGMPException(int errorcode) {
        super(CGMPSpecification.Error.describeError(errorcode));
    }
    
    /**
     * Constructs an instance of <code>CGMPException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CGMPException(String msg) {
        super(msg);
    }
}
