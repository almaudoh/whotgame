// this interface is implemented by all card objects
package org.anieanie.card;

public interface Card extends Comparable, Cloneable {
    public int getLabel();
    public int getShape();

    /**
     * Creates a new object that is <code>equal()</code> to this object
     * This method must be overridden by its concrete subclass
     * @return A card object created from a string
     */
    public Card clone();

    /**
     * This method must be overridden by its concrete subclass
     * @param string The string from which the Card object is created
     * @param basis  An object of type card which will be used to create a new instance
     * @return A card object created from a string
     */
    //public AbstractCard getInstance(String string);
    
}