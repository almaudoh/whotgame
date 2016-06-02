/*
 * WhotCardTest.java
 * JUnit based test
 *
 * Created on February 24, 2005, 12:08 AM
 */

package org.anieanie.card.whot;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.lang.*;
import static org.testng.Assert.*;

/**
 *
 * @author ALMAUDOH
 */
public class WhotCardTest {
    
    @BeforeClass
    public void setUp() throws java.lang.Exception {
    }

    @AfterClass
    public void tearDown() throws java.lang.Exception {
    }

    @Test(dataProvider = "legal_values", dataProviderClass = WhotCardTestProvider.class)
    public void legalInstantiation(int shape, int label) {
        WhotCard card = new WhotCard(shape, label);
        assertEquals(card.getShape(), shape);
        assertEquals(card.getLabel(), label);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
          dataProvider = "illegal_values",
          dataProviderClass = WhotCardTestProvider.class)
    public void assertLegal(int shape, int label) {
        WhotCard.assertLegal(shape, label, Integer.toString(shape));
    }

    @Test(dataProvider = "illegal_pairs", dataProviderClass = WhotCardTestProvider.class)
    public void staticIsIllegal(int shape, int label) {
        assertTrue(WhotCard.isIllegal(shape, label));
    }

    @Test(dataProvider = "shape_names", dataProviderClass = WhotCardTestProvider.class)
    public void GetShapeString(int shape, int label, String shapeString) {
        assertEquals(new WhotCard(shape, label).getShapeString(), shapeString);
    }

    @Test
    public void CompareTo() {
        WhotCard card1, card2, card3, card4;
        card1 = new WhotCard(WhotCard.STAR, 4);
        card2 = new WhotCard(WhotCard.STAR, 8);
        card3 = new WhotCard(WhotCard.BALL, 13);
        card4 = new WhotCard(WhotCard.CROSS, 13);

        assertEquals(card1.compareTo(card1), 0, "Star 4 compareTo Star 4 = 0");
        assertEquals(card1.compareTo(card2), -1, "Star 4 compareTo Star 8 = -1");
        assertEquals(card2.compareTo(card1), 1, "Star 8 compareTo Star 4 = 1");
        assertEquals(card2.compareTo(card3), -1, "Star 8 compareTo Ball 13 = -1");
        assertEquals(card3.compareTo(card4), 1, "Ball 13 compareTo Cross 13 = 1");
    }

    @Test
    public void Equals() {
        assertEquals(new WhotCard(WhotCard.CROSS, 10), new WhotCard(WhotCard.CROSS, 10));
        assertNotEquals(new WhotCard(WhotCard.CROSS, 10), new WhotCard(WhotCard.WHOT, 20));
    }

    /**
     * Test of clone method, of class WhotCard.
     */
    //@Test
    public void testClone() {

        System.out.println("testClone");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }

    @Test
    public void ToString() {
        WhotCard card1 = new WhotCard(WhotCard.CROSS, 13);
        WhotCard card2 = new WhotCard(WhotCard.STAR, 5);
        WhotCard card3 = new WhotCard(WhotCard.BALL, 1);
        assertEquals(card1.toString(), "Cross 13");
        assertEquals(card2.toString(), "Star 5");
        assertEquals(card3.toString(), "Circle 1");
    }

    @Test(dataProvider = "from_string", dataProviderClass = WhotCardTestProvider.class)
    public void FromString(String string, WhotCard card) {
        // @todo Move this to AbstractCardTest when mocking is available.
        WhotCard newCard = (WhotCard) WhotCard.fromString(string);
        assertEquals(newCard, card);
    }
    
}
