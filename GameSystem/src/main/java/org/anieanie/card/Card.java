// this interface is implemented by all card objects
package org.anieanie.card;

public interface Card extends Comparable, Cloneable {
    int getLabel();
    int getShape();

    /**
     * Creates a new object that is <code>equal()</code> to this object
     * This method must be overridden by its concrete subclass
     * @return A card object created from a string
     */
    Card clone() throws CloneNotSupportedException;

    /**
     * This method must be overridden by its concrete subclass
     * @param string The string from which the Card object is created
     * @param basis  An object of type card which will be used to create a new instance
     * @return A card object created from a string
     */
    //AbstractCard getInstance(String string);
    
}