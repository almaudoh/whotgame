//TestDriver for the WhotCard Class
//this test driver creates WhotCard objects and displays them
package org.anieanie.whot;

import java.util.*;
import java.io.*;
import org.anieanie.whot.*;
import org.anieanie.cardgame.*;

public class TestDriver {
    public static void main(String[] args) {
        //Clock clock = new Clock();		// create new thread for clock
        //clock.setPriority(Thread.currentThread().getPriority() - 1); 	// set clock priority higher than current thread
        
        CardSet newpack = new WhotCardSet();
        System.out.println("\nnew CardSet created");
        printCardSet(newpack);
        newpack.shuffle(20);
        System.out.println("\nnew CardSet shuffled 20 times");
        printCardSet(newpack);
        newpack.sort();
        System.out.println("\nnew CardSet sorted");
        printCardSet(newpack);
        
        //newpack.clear();
        //System.out.println("\nCardSet cleared");
        //printCardSet(newpack);
        
        System.out.println("\nCardSet cloned");
        CardSet anotherpack = (CardSet) newpack.clone();
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.WHOT, 20));
        anotherpack.remove(new WhotCard(WhotCard.CROSS, 2));
        newpack.remove(new WhotCard(WhotCard.CROSS, 2));
        newpack.remove(new WhotCard(WhotCard.WHOT, 20));
        newpack.add(new WhotCard(WhotCard.WHOT, 20));
//        newpack.add(new WhotCard(WhotCard.WHOT, 20));  // This line will throw an exception
//        newpack.add(new WhotCard(WhotCard.WHOT, 20));  // This line will throw an exception
        anotherpack.shuffle(20);
        System.out.println("\nOld pack");
        printCardSet(newpack);
        System.out.println("\nCloned pack: shuffled 20 times");
        printCardSet(anotherpack);
        
        System.out.println("\nIndex of Star 7: " + anotherpack.indexOf(new WhotCard(WhotCard.STAR, 7)));
        
        
        //try { clock.join(); }			// wait for clock thread to finish
        //catch (InterruptedException e) {}
        
    }
    
    public static void printCardSet(CardSet pack) {
        System.out.println("\nCardset contains: ");
        WhotCard card;
        for (Iterator iter = pack.iterator(); iter.hasNext();) {
            card = (WhotCard) iter.next();
            System.out.println(card.toString());
        }
    }
}