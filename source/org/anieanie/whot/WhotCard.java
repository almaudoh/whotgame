// This class represents the basic card object which describes all the features
// of the cards in a whot game. It contains all information about the cards in a card pack
package org.anieanie.whot;
import java.util.*;
import java.io.*;
import java.lang.*;
import org.anieanie.cardgame.*;

public class WhotCard extends AbstractCard {
    // static initializer for ramdom number generator
    private static Random generator = new Random(System.currentTimeMillis());
    
    // class constants that specify the information for the whot cards
    public static final int U_LIMIT = 14;
    public static final int L_LIMIT = 1;
    public static final int N_STAR = 7;
    public static final int N_CROSS = 9;
    public static final int N_BALL = 12;
    public static final int N_CARPET = 9;
    public static final int N_ANGLE  = 12;
    public static final int N_WHOT = 5;
    public static final int N_SHAPES = 6;
    public static final String[] SHAPES =  {"Star",
                                            "Cross",
                                            "Circle",
                                            "Square",
                                            "Triangle",
                                            "Whot"};
    public static final int[][] EXCLUDED_NUMBERS = {{6,9,10,11,12,13,14},
    {4,6,8,9,12},{6,9},
    {4,6,8,9,12},{6,9},
    {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}};
    //this array must be sorted
    public static final int STAR = 0;
    public static final int CROSS = 1;
    public static final int BALL = 2;
    public static final int CARPET = 3;
    public static final int ANGLE  = 4;
    public static final int WHOT  = 5;
    
    /** variable to keep track of number of Whots created */
//    protected static int num_whots = 0;
    
    // Static methods
    /** 
     * This method returns true if that combination of shape and label is not allowed
     * False otherwise
     */
    public static boolean isIllegal(int shape, int label) {
        if (shape < STAR || shape > WHOT) return true;
        return (Arrays.binarySearch(EXCLUDED_NUMBERS[shape], label) >= 0) || (shape == WHOT && label != 20);
    }
    
    //Constructors: a card is generated randomly if no arguments are specified
    public WhotCard(int shape, int label) {
        if (shape < STAR || shape > WHOT)
            throw new IllegalArgumentException("You specified an illegal shape");
        this.shape = shape;
        if (isIllegal(shape, label))
            throw new IllegalArgumentException("Number not allowed for this shape");
        this.label = label;
        if (this.shape == WHOT) {
//            num_whots++;
        }
    }
    
    public WhotCard() {
        // use random number generator object to generate random values
        int shape = WhotCard.generator.nextInt(N_SHAPES); // random integer from 0 to N_SHAPES - 1
        int label = WhotCard.generator.nextInt(U_LIMIT) + 1; // random integer from 1 to U_LIMIT
        
        if (shape == WHOT) {
            label = 20;
//            num_whots++;
        }
        else {
            Arrays.sort(EXCLUDED_NUMBERS[shape]); // sort EXCLUDED_NUMBERS array to enable binary search
            while (isIllegal(shape, label)) {
                /*this loop checks to ensure that the label generated is not in the excluded list
                 * for that particular shape
                 */
                label = WhotCard.generator.nextInt(U_LIMIT) + 1; // random number from 1 to U_LIMIT
            }
        }
        
        this.label = label;
        this.shape = shape;
    }
    
    public String getShapeString() { return SHAPES[this.shape]; }
    
    private void setLabel(int label) {
        if (isIllegal(this.shape, label))
            throw new IllegalArgumentException("Number not allowed for this shape");
        this.label = label;
    }
    
    private void setShape(int shape) {
        if (shape < STAR || shape > WHOT)
            throw new IllegalArgumentException("You specified an illegal shape");
        this.shape = shape;
        this.label = 1;
    }
    
    private void setShape(String shape) {
        for (int i=0; i<5; i++) {
            if (shape == SHAPES[i]) {
                this.shape = i;
                break;
            }
        }
        this.shape = 5;
    }
    
    private void setCard(int shape, int label) {
        this.setShape(shape);
        this.setLabel(label);
    }
    
    public int compareTo(WhotCard anotherCard) {
        if (this.shape == anotherCard.getShape()) {
            if (this.label > anotherCard.getLabel()) {
                return 1;
            }
            else if (this.label < anotherCard.getLabel()) {
                return -1;
            }
            else if (this.label == anotherCard.getLabel()) {
                return 0;
            }
        }
        else if (this.shape > anotherCard.getShape()) {
            return 1;
        }
        else {
            return -1;
        }
        return 0;
    }
    
    // public method implementing comparable
    public int compareTo(Object anObject) {
        if (anObject instanceof org.anieanie.whot.WhotCard) {
            return compareTo((WhotCard) anObject);
        }
        else {
            throw new ClassCastException("Object type mismatch");
        }
        //return 0;
    }
    
    public boolean equals(WhotCard anotherCard) {
//        if (anotherCard.getShape() == WHOT) {
//            if (num_whots > N_WHOT) return true;
//            else return false;
//        } else
            return ((anotherCard.getShape() == this.shape) && (anotherCard.getLabel() == this.label));
    }
    
    //public methods overriding object
    public AbstractCard clone() {
        return new WhotCard(this.shape, this.label);
    }
    
    public String toString() {
        return SHAPES[this.shape] + " " + Integer.toString(this.label);
    }
    
    public boolean equals(Object anObject) {
        if (anObject instanceof org.anieanie.whot.WhotCard) {
            return equals((WhotCard) anObject);
        }
        else {
            throw new ClassCastException("Object type mismatch");
        }
    }
    
    // this protected class method allows the num_whots variable to be reset
//    protected static void reset_whots() {
////        num_whots = 0;
//    }

    protected AbstractCard getInstance(String string) {
        String shape = string.substring(0, string.indexOf(' '));
        int intshape = -1;
        for (int i=0; i<5; i++) {
            if (shape == SHAPES[i]) {
                intshape = i;
                break;
            }
        }
        int intlabel = Integer.parseInt(string.substring(string.indexOf(' ')).trim());
        if (isIllegal(intshape, intlabel))
            throw new IllegalArgumentException("WhotCard.getInstance(): Illegal card string supplied");
        return new WhotCard(intshape, intlabel);
    }
}