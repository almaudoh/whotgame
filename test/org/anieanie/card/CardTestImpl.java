package org.anieanie.card;

public class CardTestImpl implements Card {
    private int shape;
    private int label;

    public CardTestImpl(int shape, int label) {
        this.shape = shape;
        this.label = label;
    }

    @Override
    public int getLabel() {
        return label;
    }

    @Override
    public int getShape() {
        return shape;
    }

    @Override
    public Card clone() {
        return new CardTestImpl(shape, label);
    }

    @Override
    public int compareTo(Object o) {
        CardTestImpl other = (CardTestImpl) o;
        if (this.shape == other.getShape()) {
            if (this.label > other.getLabel()) {
                return 1;
            }
            else if (this.label < other.getLabel()) {
                return -1;
            }
            else if (this.label == other.getLabel()) {
                return 0;
            }
        }
        else if (this.shape > other.getShape()) {
            return 1;
        }
        else {
            return -1;
        }
        return 0;
    }

    public boolean equals(CardTestImpl card) {
        return card.getLabel() == label && card.getShape() == shape;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CardTestImpl) {
            return this.equals((CardTestImpl) o);
        } else {
            return super.equals(o);
        }
    }

    public String toString() {
        return "[shape:" + shape + " label:" + label + " (" + super.toString() + ")]";
    }
}
