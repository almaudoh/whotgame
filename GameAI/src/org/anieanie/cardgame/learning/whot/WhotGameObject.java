package org.anieanie.cardgame.learning.whot;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.List;


@DeepCopyState
public abstract class WhotGameObject implements ObjectInstance, MutableState {

    public String name;

    protected String className;

    protected static List<Object> keys = null;

    @Override
    public String className() {
        return className;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }

}