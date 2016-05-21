/*
 * WhotCardTest.java
 * JUnit based test
 *
 * Created on February 24, 2005, 12:08 AM
 */

package org.anieanie.card.whot;

import static org.testng.Assert.*;
import org.anieanie.card.CardSet;
import org.anieanie.card.CardSetTest;
import org.testng.annotations.Test;

/**
 *
 * @author ALMAUDOH
 */
public class WhotCardSetTest {

    @Test
    public void AddAndShuffle() {
        CardSet newpack = new CardSet();
        newpack.shuffle(20);
        System.out.println("\nOld pack");
        System.out.println("\nCloned pack: shuffled 20 times");
        System.out.println("\nIndex of Star 7: " + newpack.indexOf(new WhotCard(WhotCard.STAR, 7)));

        System.out.println("\nCardSet cloned");
        CardSet anotherpack = (CardSet) newpack.clone();
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.CROSS, 2));
        newpack.remove(new WhotCard(WhotCard.CROSS, 2));
        anotherpack.shuffle(20);
        System.out.println("\nOld pack");
        System.out.println("\nCloned pack: shuffled 20 times");
        System.out.println("\nIndex of Star 7: " + anotherpack.indexOf(new WhotCard(WhotCard.STAR, 7)));
    }

    @Test
    public void MultiWhots() {
        WhotCardSet newpack = new WhotCardSet();
        try {
            // This should throw a RuntimeException.
            newpack.add(new WhotCard(WhotCard.WHOT, 20));
            fail("Exception should be thrown on more than five whot cards added.");
        }
        catch (RuntimeException ex) {
            assertEquals(ex.getMessage(), "Attempt to insert duplicate entries in cardset");
        }
        catch (Exception e) {
            fail("Exception should be thrown on more than five whot cards added.");
        }

        newpack.removeLast();
        // This should work now.
        newpack.add(new WhotCard(WhotCard.WHOT, 20));
    }
    
}
