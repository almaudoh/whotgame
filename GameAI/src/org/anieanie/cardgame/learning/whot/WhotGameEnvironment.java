package org.anieanie.cardgame.learning.whot;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.UnknownKeyException;
import org.anieanie.cardgame.gameplay.GameEnvironment;

import java.util.Arrays;

import static org.anieanie.cardgame.learning.whot.WhotGameWorld.*;

public class WhotGameEnvironment extends WhotGameObject {

    public static final String VAR_CARDS = "cards";
    public static final String VAR_TOP_CARDS = "TopCards";
    public static final String VAR_CALLED_CARD = "CalledCard";
    public static final String VAR_MARKET_MODE = "MarketMode";
    public static final String VAR_PLAYERS = "PlayerCount";

    static {
        keys = Arrays.asList(
                VAR_TOP_CARDS,
                VAR_CALLED_CARD,
                VAR_MARKET_MODE,
                VAR_PLAYERS);
    }

    private String topCard;
    private String calledCard;
    private String marketMode;
    private String playerCount;

    public WhotGameEnvironment() {
        this(new GameEnvironment());
    }

    public WhotGameEnvironment(GameEnvironment env) {
        this(env, "environment");
    }

    public WhotGameEnvironment(GameEnvironment env, String name) {
        this.topCard = env.get(VAR_TOP_CARDS);
        this.calledCard = env.get(VAR_CALLED_CARD);
        this.marketMode = env.get(VAR_MARKET_MODE);
        this.playerCount = env.get(VAR_PLAYERS);
        this.name = name;
        this.className = CLASS_ENVIRONMENT;
    }

    private WhotGameEnvironment(String topCard, String calledCard, String marketMode, String playerCount, String name) {
        this.topCard = topCard;
        this.calledCard = calledCard;
        this.marketMode = marketMode;
        this.playerCount = playerCount;
        this.name = name;
        this.className = CLASS_ENVIRONMENT;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new WhotGameEnvironment(topCard, calledCard, marketMode, playerCount, objectName);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        if (variableKey.equals(VAR_TOP_CARDS)) {
            this.topCard = (String)value;
        }
        else if (variableKey.equals(VAR_CALLED_CARD)) {
            this.calledCard = (String)value;
        }
        else if (variableKey.equals(VAR_MARKET_MODE)) {
            this.marketMode = (String)value;
        }
        else if (variableKey.equals(VAR_PLAYERS)) {
            this.playerCount = (String)value;
        }
        else
            throw new UnknownKeyException(variableKey);

        return this;
    }

    @Override
    public Object get(Object variableKey) {
        if (variableKey.equals(VAR_TOP_CARDS)) {
            return this.topCard;
        }
        else if (variableKey.equals(VAR_CALLED_CARD)) {
            return this.calledCard;
        }
        else if (variableKey.equals(VAR_MARKET_MODE)) {
            return this.marketMode;
        }
        else if (variableKey.equals(VAR_PLAYERS)) {
            return this.playerCount;
        }
        else
            throw new UnknownKeyException(variableKey);
    }

    @Override
    public State copy() {
        return new WhotGameEnvironment(topCard, calledCard, marketMode, playerCount, name);
    }

}
