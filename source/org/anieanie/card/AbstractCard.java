/*
 * AbstractCard.java
 *
 * Created on February 23, 2005, 9:53 PM
 */

package org.anieanie.card;

/**
 *
 * @author  ALMAUDOH
 */
public abstract class AbstractCard implements Comparable, java.io.Serializable, Cloneable, Card {
    protected int label;
    protected int shape;
    protected static String __subclass = "org.anieanie.card.whot.WhotCard";
    
    public static String getSubClass() {
        return __subclass;
    }
    
    /** Creates a new instance of AbstractCard */
    protected AbstractCard() {
    }
    
    /** Creates a new instance of AbstractCard with specified shape and label */
    protected AbstractCard(int shape, int label) {
        this.shape = shape;
        this.label = label;
    }
    
    public int getLabel() { return this.label; }
    
    public int getShape() { return this.shape; }
    
    public String toString() {
        return shape + " " + label;
    }

    /**
     * Specifies subclass of AbstractClass currently active
     */
    public static void setSubClass(String subclass) {
        __subclass = subclass;
    }
    
    /**
     * Creates a new object that is <code>equal()</code> to this object
     * This method must be overridden by its concrete subclass
     * @return A card object created from a string
     */
    public abstract Card clone();

}
