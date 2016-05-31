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
