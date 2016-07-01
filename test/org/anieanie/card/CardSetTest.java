/*
 * WhotCardTest.java
 * JUnit based test
 *
 * Created on February 24, 2005, 12:08 AM
 */

package org.anieanie.card;

import static org.testng.Assert.*;

import org.testng.annotations.Test;


/**
 *
 * @author ALMAUDOH
 */
public class CardSetTest {

    @Test
    public void IsDuplicate() {
        CardSet set1 = new CardSet();
        set1.add(new CardTestImpl(0, 0));
        set1.add(new CardTestImpl(1, 0));
        set1.add(new CardTestImpl(2, 0));

        assertTrue(set1.isDuplicate(new CardTestImpl(2, 0)));
    }

    @Test
    public void AddCloneAndRemove() {
        CardSet newpack = getCardSet(4, 4);
        CardSet anotherpack = (CardSet) newpack.clone();
        assertEquals(anotherpack.size(), 16);

        anotherpack.remove(new CardTestImpl(0, 1));
        anotherpack.remove(new CardTestImpl(1, 1));
        anotherpack.remove(new CardTestImpl(2, 1));
        anotherpack.remove(new CardTestImpl(0, 2));
        anotherpack.remove(new CardTestImpl(0, 3));
        anotherpack.remove(new CardTestImpl(3, 2));
        assertEquals(anotherpack.size(), 10);

        // Try removing what's already been removed.
        anotherpack.remove(new CardTestImpl(0, 2));
        anotherpack.remove(new CardTestImpl(3, 2));
        assertEquals(anotherpack.size(), 10);
    }

    @Test(expectedExceptions = { RuntimeException.class })
    public void AddDuplicate() {
        CardSet newpack = new CardSet();
        newpack.add(new CardTestImpl(0, 2));
        newpack.add(new CardTestImpl(4, 4));
        newpack.add(new CardTestImpl(4, 4));
    }

    @Test
    public void GetFirst() {
        CardSet pack = getCardSet(6, 10);
        assertEquals(pack.getFirst(), new CardTestImpl(0, 0));
    }

    @Test
    public void getLast() {
        CardSet pack = getCardSet(8, 10);
        assertEquals(pack.getLast(), new CardTestImpl(7, 9));
    }

    @Test
    public void RemoveFirst() {
        CardSet pack = getCardSet(6, 10);
        pack.removeFirst();
        assertEquals(pack.getFirst(), new CardTestImpl(0, 1));
        pack.removeFirst();
        assertEquals(pack.getFirst(), new CardTestImpl(0, 2));
        assertEquals(pack.size(), 58);
    }

    @Test
    public void RemoveLast() {
        CardSet pack = getCardSet(4, 10);
        pack.removeLast();
        assertEquals(pack.getLast(), new CardTestImpl(3, 8));
        pack.removeLast();
        assertEquals(pack.getLast(), new CardTestImpl(3, 7));
        assertEquals(pack.size(), 38);
    }

    @Test
    public void ShuffleAndClone() {
        CardSet newpack = getCardSet(4, 52);
        CardSet savedpack = (CardSet) newpack.clone();
        newpack.shuffle(20);
        assertNotEquals(newpack, savedpack);
        newpack.sort();
        assertEquals(newpack, savedpack);
    }

    @Test
    public void Clear() {
        CardSet pack = getCardSet(10, 10);
        assertEquals(pack.size(), 100);
        pack.clear();
        assertEquals(pack.size(), 0);
    }

    @Test
    public void ContainsShapeAndContainsLabel() {
        CardSet pack = getCardSet(4, 13);
        assertTrue(pack.containsShape(2));
        assertFalse(pack.containsShape(4));
        assertTrue(pack.containsLabel(6));
        assertFalse(pack.containsShape(13));
    }

    @Test
    public void ContainingShapeAndLabel() {
        CardSet pack = new CardSet();
        pack.add(new CardTestImpl(1, 13));
        pack.add(new CardTestImpl(2, 10));
        pack.add(new CardTestImpl(3, 4));
        pack.add(new CardTestImpl(4, 4));
        pack.add(new CardTestImpl(8, 3));
        pack.add(new CardTestImpl(3, 3));
        pack.add(new CardTestImpl(2, 3));
        pack.add(new CardTestImpl(4, 12));
        pack.add(new CardTestImpl(4, 5));
        assertEquals(pack.containingShape(13).size(), 0);
        assertEquals(pack.containingShape(2).size(), 2);
        assertEquals(pack.containingShape(0).size(), 0);
        assertEquals(pack.containingShape(4).size(), 3);
        assertEquals(pack.containingShape(8).size(), 1);
        assertEquals(pack.containingLabel(13).size(), 1);
        assertEquals(pack.containingLabel(10).size(), 1);
        assertEquals(pack.containingLabel(4).size(), 2);
        assertEquals(pack.containingLabel(6).size(), 0);
        assertEquals(pack.containingLabel(3).size(), 3);
    }

    protected static void printCardSet(CardSet pack) {
        System.out.println("\nCardset contains: ");
        for (Card card : pack.getCardlist()) {
            System.out.println(card.toString());
        }
    }

    protected CardSet getCardSet(int shapes, int labels) {
        CardSet newpack = new CardSet();
        for (int i = 0; i < shapes; i++) {
            for (int j = 0; j < labels; j++) {
                newpack.add(new CardTestImpl(i, j));
            }
        }
        return newpack;
    }

}
