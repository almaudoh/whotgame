package org.anieanie.cardgame.learning.whot;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.visualizer.Visualizer;
import org.anieanie.cardgame.training.GameRunner;

import java.util.ArrayList;
import java.util.List;

public class DeepQLearningSolver extends MDPSolver implements LearningAgent {

    private final GameRunner gameRunner;
    QProvider qProvider;
    QFunction qinit;
    double learningRate;
    Policy learningPolicy;

    public DeepQLearningSolver(double gamma, double learningRate, double epsilon){

        this.solverInit(null, gamma, null);
        this.qinit = qinit;
        this.learningRate = learningRate;
        this.qProvider = new DeepQNetwork();
        this.learningPolicy = new EpsilonGreedy(qProvider, epsilon);
        this.gameRunner = new GameRunner("simple", "smart");
    }

    @Override
    public Episode runLearningEpisode(Environment env) {
        return this.runLearningEpisode(env, -1);
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        //initialize our episode object with the initial state of the environment
        Episode e = new Episode(env.currentObservation());

        //behave until a terminal state or max steps is reached
        State curState = env.currentObservation();
        int steps = 0;
        while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){

            //select an action
            Action a = this.learningPolicy.action(curState);

            //take the action and observe outcome
            EnvironmentOutcome eo = env.executeAction(a);

            //record result
            e.transition(eo);

            //get the max Q value of the resulting state if it's not terminal, 0 otherwise
//            double maxQ = eo.terminated ? 0. : this.qProvider.qValues(eo.op);

            //update the old Q-value
//            QValue oldQ = this.qProvider.qValue(curState, a);
//            oldQ.q = oldQ.q + this.learningRate * (eo.r + this.gamma * maxQ - oldQ.q);


            //update state pointer to next environment state observed
            curState = eo.op;
            steps++;

        }

        return e;
    }

    @Override
    public void resetSolver() {
        throw new RuntimeException("Not yet implemented");
    }

    public static void main(String[] args) {

        WhotGameWorld world = new WhotGameWorld();
//        gwd.setMapToFourRooms();
//        gwd.setProbSucceedTransitionDynamics(0.8);
//        world.setTf(new GridWorldTerminalFunction(10, 10));

        OOSADomain domain = world.generateDomain();

        //get initial state with agent in 0,0
        State s = new GridWorldState(new GridAgent(0, 0));

        //create environment
        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);

        //create Q-training
        DeepQLearningSolver agent = new DeepQLearningSolver(0.99, 0.1, 0.1);

        //run Q-training and store results in a list
        List<Episode> episodes = new ArrayList<Episode>(1000);
        for(int i = 0; i < 1000; i++){
            episodes.add(agent.runLearningEpisode(env));
            env.resetEnvironment();
        }

//        Visualizer v = GridWorldVisualizer.getVisualizer(world.getMap());
//        new EpisodeSequenceVisualizer(v, domain, episodes);

    }

}