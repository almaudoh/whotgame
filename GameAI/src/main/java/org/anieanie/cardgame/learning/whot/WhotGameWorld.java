package org.anieanie.cardgame.learning.whot;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import org.anieanie.card.Card;
import org.anieanie.card.whot.WhotCardSet;
import org.anieanie.cardgame.gameplay.GameEnvironment;

import static burlap.domain.singleagent.gridworld.GridWorldDomain.ACTION_SOUTH;


public class WhotGameWorld implements DomainGenerator {

    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_ENVIRONMENT = "environment";

    public static final String ACTION_MARKET = "market";

    public static final String GAME_FINISHED = "GameFinished";


    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        WhotCardSet cards = new WhotCardSet();
        cards.initialize();
        for (Card card : cards)
            domain.addActionTypes(new WhotGameActionType(card.toString(), new String[]{CLASS_ENVIRONMENT}));

        DeepLearnedModel smodel = new DeepLearnedModel();
        RewardFunction rf = new WhotGameRewardFunction();
        TerminalFunction tf = new WhotGameTerminalFunction();

//        domain.setModel(new FactoredModel(smodel, rf, tf));
        domain.setModel(smodel);

        return domain;
    }


    public class WhotGameTerminalFunction implements TerminalFunction {

        @Override
        public boolean isTerminal(State s) {
            // @todo
            return false;
        }
    }

    public class WhotGameRewardFunction implements RewardFunction {

        @Override
        public double reward(State s, Action a, State sprime) {
            // @todo
            // If the agent won the game, then score is 20.
            GameEnvironment env = (GameEnvironment) s.get("GameEnvironment");
            String gamewon = env.get("GameWon");
            if (gamewon.matches("GameWon: winner (my_name)")) {
                return 20;
            }
            // Going to market should be discouraged.
            if (a.equals("market")) {
                return -1;
            }
            return 0;
        }
    }

    public class WhotGameActionType extends ObjectParameterizedActionType {

        public WhotGameActionType(String name, String[] parameterClasses) {
            super(name, parameterClasses);
        }

        public WhotGameActionType(String name, String[] parameterClasses, String[] parameterOrderGroups) {
            super(name, parameterClasses, parameterOrderGroups);
        }

        @Override
        protected boolean applicableInState(State s, ObjectParameterizedAction a) {
            // @todo
            // Check that the user has the specified cards in hand.
            return s.get("cards").equals(a.getObjectParameters());
        }
    }
}
