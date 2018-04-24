package dallidalli.mctsLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import dallidalli.commonClasses.CSV;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;

import java.util.*;

public class LevelGenerator extends AbstractLevelGenerator{

    private final Object bestLevel;
    private ArrayList<Double> bestFitness;
    private MCTS resultMCTS;

    public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer){
        SharedData.random = new Random();
        SharedData.gameDescription = game;
        SharedData.gameAnalyzer = new GameAnalyzer(game);
        SharedData.constructiveGen = new tracks.levelGeneration.constructiveLevelGenerator.LevelGenerator(game, null);
        bestLevel = null;
        bestFitness = null;
    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        //initialize the statistics objects
        bestFitness = new ArrayList<Double>();
        SharedData.gameDescription = game;

        int size = 0;
        if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
            size = 2;
        }


        //get the level size
        int width = (int)Math.max(SharedData.MIN_SIZE + size, game.getAllSpriteData().size() * (1 + 0.25 * SharedData.random.nextDouble()) + size);
        int height = (int)Math.max(SharedData.MIN_SIZE + size, game.getAllSpriteData().size() * (1 + 0.25 * SharedData.random.nextDouble()) + size);
        width = (int)Math.min(width, SharedData.MAX_SIZE + size);
        height = (int)Math.min(height, SharedData.MAX_SIZE + size);

        MCTS search = new MCTS(width, height, true, false);

        //some variables to make sure not getting out of time
        double worstTime = Double.MIN_VALUE;
        double avgTime = 0.0;
        double totalTime = 0.0;
        double lastTime = 0.0;
        ElapsedCpuTimer timer = new ElapsedCpuTimer();
        int numberOfIterations = 0;
        double avgScore = 0.0;
        double curScore = 0.0;


        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> evaluated = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();
        ArrayList<String> avgValue = new ArrayList<>();
        //SharedData.random.setSeed(42);
        double restart = SharedData.MCTS_restart;


        // System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){

            search.selectAction();
            curScore = search.currentValue;
            avgScore += curScore;

            lastTime = timer.elapsedMillis() - totalTime;

            if(lastTime > worstTime){
                worstTime = lastTime;
            }

            numberOfIterations += 1;
            totalTime += lastTime;
            avgTime = totalTime / numberOfIterations;





            if(numberOfIterations % 500 == 1){

                time.add(String.valueOf(totalTime));
                evaluated.add(String.valueOf(numberOfIterations));
                value.add(String.valueOf(curScore));
                avgValue.add(String.valueOf((avgScore / numberOfIterations)));



                if(numberOfIterations % restart == 1 && restart != 0){
                    search.restart();
                }
            }
        }


        //TreeView tv = new TreeView(root);
        //tv.showTree("After " + numberOfIterations + " play outs");
        ArrayList<SpritePointData> best = search.getBestMap();



        search.getLevel(best, true);
        resultMCTS = search;
        // System.out.println("Done " + numberOfIterations);

        String name = "MCTS";
        String setting = SharedData.MIN_SIZE + "x" + SharedData.MAX_SIZE + "_C" + String.valueOf(search.C) + "_restart"+ restart;

        CSV.writeCSV(name, setting, time,evaluated,value,avgValue);

        return search.getCurrentLevel().getLevelString(search.getCurrentLevel().getLevelMapping());
    }

    /**
     * get the current used level mapping to create the level string
     * @return	the level mapping used to create the level string
     */

    @Override
    public HashMap<Character, ArrayList<String>> getLevelMapping(){
        return resultMCTS.getCurrentLevel().getLevelMapping().getCharMapping();
    }


    public static void main(String[] args) {

    }

}

