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
    protected static String __subclass;
    
    public static String getSubClass() {
        return "org.anieanie.card.whot.WhotCard";
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
     * Creates a new AbstractCard subclass whose type is specified
     */
    public static AbstractCard fromString(String cardspec) {
        try {
            return ((AbstractCard)Class.forName(getSubClass()).newInstance()).getInstance(cardspec);
        }
        catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        catch (InstantiationException ie) {
            ie.printStackTrace();
        }
        catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
        catch (ClassCastException cce) {
            cce.printStackTrace();
        }
        return null;
    }
    
    /**
     * Specifies subclass of AbstractClass currently active
     */
    public static void setSubClass(String subclass) {
        __subclass = subclass;
    }
    
    /** 
     * This method is needed to create a valid instance of a subclass of AbstractCard
     * @param cardspec The string from which the Card object is created
     * @return A card object created from a string
     */
    protected abstract AbstractCard getInstance(String cardspec);

    /**
     * Creates a new object that is <code>equal()</code> to this object
     * This method must be overridden by its concrete subclass
     * @return A card object created from a string
     */
    public abstract Card clone();

}
