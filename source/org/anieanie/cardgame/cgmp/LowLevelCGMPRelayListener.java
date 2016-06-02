package org.anieanie.cardgame.cgmp;

/**
 * This interface provides methods for listening and responding to low-level CGMP communications.
 *
 * Created by almaudoh on 6/1/16.
 */
public interface LowLevelCGMPRelayListener extends CGMPRelayListener {

    void onBufferOut(String message);

    void onBufferIn(String message);

}
