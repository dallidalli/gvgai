package tracks.levelGeneration.nmcsLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.LevelMapping;
import tools.Pair;

import java.util.*;

public class LevelGenerator extends AbstractLevelGenerator{

    private final Object bestLevel;
    private ArrayList<Double> bestFitness;
    private LevelMapping root;

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

        NMCS search = new NMCS(width, height, true);


        //some variables to make sure not getting out of time
        double worstTime = SharedData.EVALUATION_TIME * 1;
        double avgTime = worstTime;
        double totalTime = 0;
        int numberOfIterations = 0;
        Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>> result = null;

        System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){
            ElapsedCpuTimer timer = new ElapsedCpuTimer();


            Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>> tmp = search.selectAction(1,search.getAllPossibleActions());
            if(result == null){
                result = tmp;
            }else {
                if(tmp.first >= result.first){
                    result = tmp;
                }
            }

            System.out.println(result.first);
            System.out.println(result.second);

            numberOfIterations += 1;
            totalTime += timer.elapsedMillis();
            avgTime = totalTime / numberOfIterations;
            System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        }

        System.out.println(result.first);
        System.out.println(result.second);

        root = search.getLevel(result.second).getLevelMapping();
        System.out.println("Done");

        return search.getLevel(result.second).getLevelString(root);
    }

    /**
     * get the current used level mapping to create the level string
     * @return	the level mapping used to create the level string
     */

    @Override
    public HashMap<Character, ArrayList<String>> getLevelMapping(){
        return root.getCharMapping();
    }


    public static void main(String[] args) {

    }

}

