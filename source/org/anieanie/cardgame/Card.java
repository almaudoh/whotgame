// this interface is implemented by all card objects
package org.anieanie.cardgame;

public interface Card {
    public int getLabel();
    public int getShape();
    
    /**
     * This method must be overridden by its concrete subclass
     * @param string The string from which the Card object is created
     * @param basis  An object of type card which will be used to create a new instance
     * @return A card object created from a string
     */
    //public AbstractCard getInstance(String string);
    
}