// This is the CardSet class that contains Card objects of any given number
// no null or duplicate entries are allowed
package org.anieanie.cardgame;
import java.util.*;
import java.io.*;
import java.lang.*;


public class CardSet extends AbstractSet implements Cloneable, Serializable, Set {
    // This class uses an internal LinkedList object to store the elements of the CardSet
    // and also to improve the functionality
    
    // private instance fields
    protected LinkedList cardlist;	// an internal list to be used to store the elements of the set
    
    // public constructors
    public CardSet() {
        cardlist = new LinkedList();
    }
    
    //  public CardSet(Card[] cards) {
    //  }
    //
    //  public CardSet(CardSet anotherCardSet) {
    //
    //  }
    //
    public CardSet(Collection c) {
        this();
        Object element;
        for (Iterator i = c.iterator(); i.hasNext();) {
            element = i.next();
            if (cardlist.contains(element)) {
                throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            } else {
                cardlist.add(element);
            }
        }
    }
    
    // private constructor for clone method
    protected CardSet(LinkedList list) {
        cardlist = list;
    }
    
    // public instance methods
    public boolean add(Card card) {
        if (cardlist.contains(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
            //return false;
        } else {
            return cardlist.add(card);
        }
    }
    
    public void add(int index, Card card) {
        if (cardlist.contains(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in Set object");
        } else {
            cardlist.add(index, card);
        }
    }
    
    public void addFirst(Card card) {
        if (cardlist.contains(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
        } else {
            cardlist.addFirst(card);
        }
    }
    
    public void addLast(Card card) {
        if (cardlist.contains(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in Set object");
            //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
        } else {
            cardlist.addLast(card);
        }
    }
    
    public Card getFirst() {
        return (Card) cardlist.getFirst();
    }
    
    public Card getLast() {
        return (Card) cardlist.getLast();
    }
    
    public Card removeFirst() {
        return (Card) cardlist.removeFirst();
    }
    
    public Card removeLast() {
        return (Card) cardlist.removeLast();
    }
    
    public boolean remove(Card card) {
        return cardlist.remove(card);
    }
    
    public Card remove(int index) {
        return (Card) cardlist.remove(index);
    }
    
    public void shuffle() {
        Collections.shuffle(cardlist);
    }
    
    public void shuffle(int times) { // shuffle a number of times
        Collections.shuffle(cardlist);
        if (--times > 0) this.shuffle(times);
    }
    
    public void shuffle(Random rnd) {
        Collections.shuffle(cardlist, rnd);
    }
    
    public void shuffle(Random rnd, int times) {
        Collections.shuffle(cardlist, rnd);
        if (times-- > 0) this.shuffle(rnd, times);
    }
    
    public void sort() {
        Collections.sort(cardlist);
    }
    
    // public methods implementing set
    public boolean add(Object obj) {
        try {
            if (cardlist.contains(obj)) {
                //throw new RuntimeException("Attempt to insert duplicate entries in Set object");
                //System.out.println("Duplicate entry detected: " + card.toString() + ", could not add");
                return false;
            } else {
                return cardlist.add((Card) obj);
            }
        }
        catch (ClassCastException cce) {
            return false;
        }
    }
    
    public void clear() {
        cardlist.clear();
    }
    
    public boolean contains(Object obj) {
        return cardlist.contains(obj);
    }
    
    public boolean isEmpty() {
        return (cardlist.size() < 1);
    }
    
    public Iterator iterator() {
        return cardlist.listIterator(0);
    }
    
    public boolean remove(Object obj) {
        return cardlist.remove((Card) obj);
    }
    
    public int size() {
        return cardlist.size();
    }
    
    // Public methods overriding Object
    public Object clone() {
        Object element;
        LinkedList list = new LinkedList();
        for (Iterator i = cardlist.iterator(); i.hasNext();) {
            element = ((AbstractCard)i.next()).clone();
            if (list.contains(element)) {
                throw new RuntimeException("Cannot clone CardSet, duplicate entries found.");
            } else {
                list.add(element);
            }
        }
        return new CardSet(list);
    }
    
    // trial methods
    public Card refOf(Card card) {
        return (Card) cardlist.get(cardlist.indexOf(card));
    }
    
    public int indexOf(Card card) {
        return cardlist.indexOf(card);
    }
    
}
