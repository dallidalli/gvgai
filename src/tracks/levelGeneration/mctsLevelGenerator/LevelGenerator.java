package tracks.levelGeneration.mctsLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.Pair;
import tracks.levelGeneration.commonClasses.SharedData;
import tracks.levelGeneration.commonClasses.SpritePointData;

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

        List<MCTS> list = new LinkedList<MCTS>();
        System.out.println("list: " + list);

        MCTS search = new MCTS(width, height, true, false);
        list.add(search);

        //some variables to make sure not getting out of time
        double worstTime = SharedData.EVALUATION_TIME * 1;
        double avgTime = worstTime;
        double totalTime = 0;
        int numberOfIterations = 0;
        double avgScore = 0;
        double stuck = 0;
        double stuckvalue = 0;

        System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){
            ElapsedCpuTimer timer = new ElapsedCpuTimer();

            list.get(0).selectActionMap();
            avgScore += list.get(0).currentValue;

            numberOfIterations += 1;
            totalTime += timer.elapsedMillis();
            avgTime = totalTime / numberOfIterations;

            if(numberOfIterations % 100 == 0){
                double maxVisit = 0, averageVisit = 0, counter = 0, minVisit = 99999;

                for (Pair<Double, Double> pair:list.get(0).UCTvalues.get(new ArrayList<SpritePointData>()).values()) {
                    if(pair.second >= 0){
                        counter++;
                    }
                    averageVisit += pair.second;

                    if(pair.second > maxVisit){
                        maxVisit = pair.second;
                    }

                    if(pair.second <= minVisit){
                        minVisit = pair.second;
                    }
                }

                double amountOfNodes = list.get(0).UCTvalues.size();

                if(amountOfNodes > stuckvalue){
                    stuck = 0;
                    stuckvalue = amountOfNodes;
                } else {
                    stuck++;
                }

                if(stuck == 99999){
                    list.get(0).UCTvalues.clear();
                    stuck = 0;
                    stuckvalue = 0;
                } else {
                    System.out.println(amountOfNodes + " " + minVisit+" "+ maxVisit+ " "+averageVisit/list.get(0).UCTvalues.get(new ArrayList<SpritePointData>()).values().size() + " " +counter/list.get(0).actions.size()+ " " +search.visitedMap.size());
                    ArrayList<SpritePointData> current = list.get(0).currentSequence;
                    System.out.println(avgScore/numberOfIterations);
                    //list.get(0).getLevel(current, true);
                    //list.get(0).resetLevel(current);
                    System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
                }


            }
        }

        //TreeView tv = new TreeView(root);
        //tv.showTree("After " + numberOfIterations + " play outs");
        ArrayList<SpritePointData> best = list.get(0).getBestMap();



        list.get(0).getLevel(best, true);
        resultMCTS = list.get(0);
        System.out.println("Done");
        return list.get(0).getCurrentLevel().getLevelString(list.get(0).getCurrentLevel().getLevelMapping());
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

