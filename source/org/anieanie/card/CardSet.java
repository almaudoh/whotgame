// This is the CardSet class that contains Card objects of any given number
// no null or duplicate entries are allowed
package org.anieanie.card;

import java.util.*;
import java.io.*;
import java.lang.*;


public class CardSet extends AbstractSet implements Cloneable, Serializable, Set {
    // This class uses an internal LinkedList object to store the elements of the CardSet
    // and also to improve the functionality

    // An internal list to be used to store the elements of the set
    protected LinkedList<Card> cardlist;
    
    // public constructors
    public CardSet() {
        cardlist = new LinkedList<Card>();
    }
    
    //  public CardSet(Card[] cards) {
    //  }

    // copy constructor
    public CardSet(CardSet original) {
        this();
        for (Card card : original.getCardlist()) {
            if (isDuplicate(card)) {
                throw new RuntimeException("Cannot clone cardset, duplicate entries found.");
            } else {
                cardlist.add(card.clone());
            }
        }

    }

    public CardSet(Collection<Card> cards) {
        this();
        for (Card card : cards) {
            if (isDuplicate(card)) {
                throw new RuntimeException("Attempt to insert duplicate entries in cardset");
            } else {
                cardlist.add(card);
            }
        }
    }
    
    // private constructor for clone method
    protected CardSet(LinkedList<Card> list) {
        cardlist = list;
    }

    public LinkedList<Card> getCardlist() {
        return cardlist;
    }
    
    // public instance methods
    public boolean add(Card card) {
        if (isDuplicate(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in cardset");
        } else {
            return cardlist.add(card);
        }
    }
    
    public void add(int index, Card card) {
        if (isDuplicate(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in cardset");
        } else {
            cardlist.add(index, card);
        }
    }
    
    public void addFirst(Card card) {
        if (isDuplicate(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in cardset");
        } else {
            cardlist.addFirst(card);
        }
    }
    
    public void addLast(Card card) {
        if (isDuplicate(card)) {
            throw new RuntimeException("Attempt to insert duplicate entries in cardset");
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
    
    public void shuffle(int times) {
        // shuffle a number of times
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
    
    public boolean add(Object obj) {
        try {
            return !isDuplicate((Card) obj) && cardlist.add((Card) obj);
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

    public boolean isDuplicate(Card card) {
        return cardlist.contains(card);
    }
    
    // Public methods overriding Object
    public Object clone() {
        LinkedList<Card> list = (LinkedList) cardlist.clone();
        Collections.copy(list, cardlist);
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
