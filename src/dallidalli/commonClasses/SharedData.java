package dallidalli.commonClasses;

import core.game.GameDescription;
import tools.GameAnalyzer;

import java.util.ArrayList;
import java.util.Random;

public class SharedData {

    /**
     * The amount of times used to check the one step look ahead and do nothing algorithm
     */
    public static final int REPETITION_AMOUNT = 50;
    /**
     * the amount of time to evaluate a single level
     */
    public static final long EVALUATION_TIME = 1000;
    /**
     * The amount of time given for each time step
     */
    public static final long EVALUATION_STEP_TIME = 30;
    /**
     * very small value
     */
    public static final double EIPSLON = 1e-9;
    /**
     * used for calculating the minimum required score for the generated level
     */
    public static final double MAX_SCORE_PERCENTAGE = 0.1;
    /**
     * minimum level size
     */
    public static double MIN_SIZE = 10;
    /**
     * maximum level size
     */
    public static double MAX_SIZE = 10;
    /**
     * minimum acceptable solution
     */
    public static final double MIN_SOLUTION_LENGTH = 120;
    /**
     * minimum acceptable do nothing steps before dying
     */
    public static final double MIN_DOTHING_STEPS = 40;
    /**
     * minimum acceptable cover percentage of sprites
     */
    public static final double MIN_COVER_PERCENTAGE = 0.05;
    /**
     * maximum acceptable cover percentage of sprites
     */
    public static final double MAX_COVER_PERCENTAGE = 0.3;
    /**
     * minimum amount of unique rules that should be applied
     */
    public static final double MIN_UNIQUE_RULE_NUMBER = 3;
    /**
     * starting the MCTS with seeds from the constructive algorithm
     */
    public static final boolean CONSTRUCTIVE_INITIALIZATION = false;
    /**
     * The name of a the best agent with some human error
     */
    public static final String AGENT_NAME = "tracks.singlePlayer.tools.repeatOLETS.Agent";
    /**
     * The name of a naive agent
     */
    public static final String NAIVE_AGENT_NAME = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
    /**
     * The name of the do nothing agent
     */
    public static final String DO_NOTHING_AGENT_NAME = "tracks.singlePlayer.simple.doNothing.Agent";

    /**
     * The game description object
     */
    public static GameDescription gameDescription;
    /**
     * A game analyzer object to help in constructing the level
     */
    public static GameAnalyzer gameAnalyzer;
    /**
     * random object to help in choosing random stuff
     */
    public static Random random;
    /**
     * constructive level generator to help in speeding up the level generation process
     */
    public static tracks.levelGeneration.constructiveLevelGenerator.LevelGenerator constructiveGen;

    public static double DRAW_FITNESS = 0.5;

    public static double MCTS_Cvalue = 0.05;
    public static double MCTS_restart = 150000;
    public static int NMCS_level = 1;
    public static boolean NMCS_injected = true;
    public static boolean useNewConstraints = true;
    public static double NRPA_alpha = 1;
    public static int NRPA_cutoff = 1;
    public static int NRPA_level = 2;
    public static int NRPA_numIterations = 100; //(int) (5000/NRPA_level/MAX_SIZE);
    public static int NRPA_B = 4;

    public static ArrayList<Double> eval_weights = new ArrayList<Double>();


}

