package org.anieanie.cardgame.learning.whot;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.UnknownKeyException;
import org.anieanie.card.whot.WhotCard;
import org.anieanie.card.whot.WhotCardSet;

import java.util.Arrays;

import static org.anieanie.cardgame.learning.whot.WhotGameEnvironment.VAR_CARDS;
import static org.anieanie.cardgame.learning.whot.WhotGameWorld.*;

public class WhotGameAgent extends WhotGameObject {

    private WhotCardSet cards;

    static {
        keys = Arrays.asList(VAR_CARDS);
    }

    public WhotGameAgent() {
        this.cards = new WhotCardSet();
        this.name = "agent";
        this.className = CLASS_AGENT;
    }

    public WhotGameAgent (WhotCardSet cards, String name) {
        this.cards = cards;
        this.name = name;
        this.className = CLASS_AGENT;
    }

    public void addCard(WhotCard card) {
        this.cards.add(card);
    }

    public void removeCard(WhotCard card) {
        this.cards.remove(card);
    }

    public String className() {
        return CLASS_AGENT;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new WhotGameAgent((WhotCardSet)this.cards.clone(), objectName);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if (variableKey.equals(VAR_CARDS)) {
            this.cards = (WhotCardSet) value;
            return this;
        }
        else
            throw new UnknownKeyException(variableKey);
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey.equals(VAR_CARDS)) {
            return this.cards;
        }
        else
            throw new UnknownKeyException(variableKey);
    }

    @Override
    public State copy() {
        return new WhotGameAgent((WhotCardSet)this.cards.clone(), this.name);
    }

}
