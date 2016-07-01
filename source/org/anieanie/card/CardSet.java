// This is the CardSet class that contains Card objects of any given number
// no null or duplicate entries are allowed
package org.anieanie.card;

import java.util.*;
import java.io.*;
import java.lang.*;


public class CardSet extends AbstractSet<Card> implements Cloneable, Serializable, Set<Card> {
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

    protected CardSet newInstance(LinkedList<Card> list) {
        return new CardSet(list);
    }

    public LinkedList<Card> getCardlist() {
        return cardlist;
    }


    /** Initializes the cardset with its actual contents */
    public void initialize() {}

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
        return cardlist.getFirst();
    }
    
    public Card getLast() {
        return cardlist.getLast();
    }
    
    public Card removeFirst() {
        return cardlist.removeFirst();
    }
    
    public Card removeLast() {
        return cardlist.removeLast();
    }
    
    public boolean remove(Card card) {
        return cardlist.remove(card);
    }
    
    public Card remove(int index) {
        return cardlist.remove(index);
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
    
    public void clear() {
        cardlist.clear();
    }
    
    public boolean contains(Object obj) {
        return cardlist.contains(obj);
    }
    
    public boolean isEmpty() {
        return (cardlist.size() < 1);
    }
    
    public Iterator<Card> iterator() {
        return cardlist.listIterator(0);
    }

    public boolean remove(Object obj) {
        return cardlist.remove(obj);
    }

    @Override
    public boolean addAll(Collection<? extends Card> c) {
        for (Card card: c) {
            add(card);
        }
        return true;
    }

    public int size() {
        return cardlist.size();
    }

    public boolean isDuplicate(Card card) {
        return cardlist.contains(card);
    }
    
    // Public methods overriding Object
    public Object clone() {
        LinkedList<Card> list = (LinkedList<Card>) cardlist.clone();
//        Collections.copy(list, cardlist);
        return new CardSet(list);
    }
    
    // trial methods
    public Card refOf(Card card) {
        return cardlist.get(cardlist.indexOf(card));
    }
    
    public int indexOf(Card card) {
        return cardlist.indexOf(card);
    }

    public boolean containsShape(int shape) {
        for (Card card: cardlist) {
            if (card.getShape() == shape) return true;
        }
        return false;
    }

    public boolean containsLabel(int label) {
        for (Card card: cardlist) {
            if (card.getLabel() == label) return true;
        }
        return false;
    }

    public CardSet containingShape(int shape) {
        LinkedList<Card> list = new LinkedList<>();
        for (Card card: cardlist) {
            if (card.getShape() == shape) {
                list.add(card);
            }
        }
        return newInstance(list);
    }

    public CardSet containingLabel(int label) {
        LinkedList<Card> list = new LinkedList<>();
        for (Card card: cardlist) {
            if (card.getLabel() == label) {
                list.add(card);
            }
        }
        return newInstance(list);
    }


}
