// This class represents the basic card object which describes all the features
// of the cards in a whot game. It contains all information about the cards in a card pack
package org.anieanie.card.whot;

import java.util.*;
import java.lang.*;
import org.anieanie.card.AbstractCard;
import org.anieanie.card.Card;

public class WhotCard extends AbstractCard {
    // static initializer for random number generator
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
    public static final int STAR = 0;
    public static final int CROSS = 1;
    public static final int BALL = 2;
    public static final int CARPET = 3;
    public static final int ANGLE  = 4;
    public static final int WHOT  = 5;
    public static final ArrayList<String> SHAPES;
    public static final ArrayList<int[]> EXCLUDED_NUMBERS;
    static {
        // Initialize shapes.
        SHAPES = new ArrayList<>();
        SHAPES.add(STAR, "star");
        SHAPES.add(CROSS, "cross");
        SHAPES.add(BALL, "circle");
        SHAPES.add(CARPET, "square");
        SHAPES.add(ANGLE, "triangle");
        SHAPES.add(WHOT, "whot");
        
        // Initialize excluded numbers for the shapes.
        EXCLUDED_NUMBERS = new ArrayList<>();
        EXCLUDED_NUMBERS.add(STAR, new int[]{6, 9, 10, 11, 12, 13, 14});
        EXCLUDED_NUMBERS.add(CROSS, new int[]{4, 6, 8, 9, 12});
        EXCLUDED_NUMBERS.add(BALL, new int[]{6, 9});
        EXCLUDED_NUMBERS.add(CARPET, new int[]{4, 6, 8, 9, 12});
        EXCLUDED_NUMBERS.add(ANGLE, new int[]{6, 9});
        EXCLUDED_NUMBERS.add(WHOT, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});

    }

    // Static methods
    public static boolean isIllegalCardSpec(String cardspec) {
        String[] specs = cardspec.split(" ", 2);
        int shape = SHAPES.indexOf(specs[0].toLowerCase());
        return specs.length < 2 || shape < 0 || WhotCard.isIllegal(shape, Integer.parseInt(specs[1]));
    }

    /** 
     * This method returns true if that combination of shape and label is not allowed
     * False otherwise
     */
    public static boolean isIllegal(int shape, int label) {
        return shape < STAR || shape > WHOT     // shape in set (STAR, CROSS, BALL, CARPET, ANGLE, WHOT).
                || label < 1 || (shape !=  WHOT && label > 14)      // 1 <= label <= 14 for non-WHOT shapes.
                || (Arrays.binarySearch(EXCLUDED_NUMBERS.get(shape), label) >= 0)   // label is excluded for the shape.
                || (shape == WHOT && label != 20);  // WHOT shape can only have 20 label.
    }

    public static void assertLegal(int shape, int label, String shapeStr) {
        if (shape < STAR || shape > WHOT)
            throw new IllegalArgumentException("Illegal shape '" + shapeStr + "' specified.");

        if (isIllegal(shape, label))
            throw new IllegalArgumentException("Number " + label + " not allowed for the shape '" + SHAPES.get(shape) + "'.");
    }
    
    //Constructors: a card is generated randomly if no arguments are specified
    public WhotCard(int shape, int label) {
        // Ensure this has the right label for the shape.
        assertLegal(shape, label, Integer.toString(shape));
        this.shape = shape;
        this.label = label;
    }
    
    public WhotCard() {
        // use random number generator object to generate random values
        int shape = WhotCard.generator.nextInt(N_SHAPES); // random integer from 0 to N_SHAPES - 1
        int label = WhotCard.generator.nextInt(U_LIMIT) + 1; // random integer from 1 to U_LIMIT
        
        if (shape == WHOT) {
            label = 20;
        }
        else {
            // Ensure that the label generated is not in the excluded list for that particular shapeL
            while (isIllegal(shape, label)) {
                label = WhotCard.generator.nextInt(U_LIMIT) + 1; // random number from 1 to U_LIMIT
            }
        }
        
        this.label = label;
        this.shape = shape;
    }

    /**
     * Creates a new WhotCard from the provided card specifications
     */
    public static WhotCard fromString(String cardspec) {
        String[] parts = cardspec.split(" ", 2);
        // Ensure the right label is used for the specified shape.
        int intlabel = Integer.parseInt(parts[1].trim());
        int intshape = getShapeInt(parts[0]);
        assertLegal(intshape, intlabel, parts[0]);
        return new WhotCard(intshape, intlabel);
    }

    public String getShapeString() {
        return SHAPES.get(this.shape);
    }

    public static int getShapeInt(String shape) {
        for (int i = 0; i < 6; i++) {
            if (shape.equalsIgnoreCase(SHAPES.get(i))) {
                return i;
            }
        }
        return -1;
    }
    
    private void setLabel(int label) {
        assertLegal(this.shape, label, Integer.toString(this.shape));
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
            if (shape.equals(SHAPES.get(i))) {
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
    public int compareTo(Object another) {
        if (another instanceof WhotCard) {
            return compareTo((WhotCard) another);
        }
        else {
            throw new ClassCastException("Object type mismatch");
        }
    }
    
    public boolean equals(WhotCard another) {
        return ((another.getShape() == this.shape) && (another.getLabel() == this.label));
    }
    
    //public methods overriding object
    public Card clone() {
        return new WhotCard(this.shape, this.label);
    }
    
    public String toString() {
        return SHAPES.get(this.shape) + " " + Integer.toString(this.label);
    }
    
    public boolean equals(Object anObject) {
        if (anObject instanceof WhotCard) {
            return equals((WhotCard) anObject);
        }
        else {
            throw new ClassCastException("Object type mismatch");
        }
    }
    
}
