/*
 * Watcher.java
 *
 * Created on May 19, 2005, 1:51 AM
 */

package org.anieanie.cardgame;

/**
 * This interface is to be implemented  by all classes to observe and publish the outcome
 * of a game. For example the User-Interface elements that will display the game must implement
 * the watcher element
 *
 * @author  ALMAUDOH
 */
public interface Watcher {
    public String getName();
    public void watch(Object env);    
}
