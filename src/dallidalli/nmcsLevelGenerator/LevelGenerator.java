package dallidalli.nmcsLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import dallidalli.commonClasses.CSV;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.LevelMapping;
import tools.Pair;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;

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

        boolean useNew = true;

        NMCS search = new NMCS(width, height, true, useNew);
        search.level.calculateSoftConstraints(true, useNew);
        ArrayList<SpritePointData> workedActions = new ArrayList<>(search.allPossibleActions);
        //LevelEvaluationFunction eval = new LevelEvaluationFunction();
        //eval.generateEvaluationFunction();


        //some variables to make sure not getting out of time
        double worstTime = SharedData.EVALUATION_TIME * 1;
        double avgTime = worstTime;
        double totalTime = 0;
        int numberOfIterations = 0;
        int level = 1;
        double averageScore = 0;

        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> evaluated = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();
        ArrayList<String> avgValue = new ArrayList<>();
        //SharedData.random.setSeed(42);


        Pair<Double, ArrayList<Integer>> result = new Pair<Double, ArrayList<Integer>>(Double.MIN_VALUE, new ArrayList<Integer>());

        ArrayList<Integer> cutoff = new ArrayList<Integer>();
        boolean injected = true;

        long endTimeMs = System.currentTimeMillis() + elapsedTimer.remainingTimeMillis();

        System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){
            ElapsedCpuTimer timer = new ElapsedCpuTimer();

            Pair<Double, ArrayList<Integer>> tmp;

            tmp = search.selectAction2(level, new ArrayList<>(workedActions), result, () -> {return System.currentTimeMillis() > endTimeMs;});




            averageScore += tmp.first;
            if(result == null){
                result = tmp;
            }else {
                if(tmp.first >= result.first){
                    result = tmp;
                }
            }


            numberOfIterations += 1;
            totalTime += timer.elapsedMillis();
            avgTime = totalTime / numberOfIterations;


            time.add(String.valueOf(totalTime));
            evaluated.add(String.valueOf(search.evaluated));
            value.add(String.valueOf(tmp.first));
            avgValue.add(String.valueOf((averageScore / numberOfIterations)));


            if(numberOfIterations % 20 == 0){
                // System.out.println(averageScore/numberOfIterations);
                // System.out.println(result.first);
                //search.getLevel(tmp.second, true).getLevelMapping();
                //search.resetLevel(tmp.second);
                // System.out.println(numberOfIterations + " " + search.evaluated +" "+ elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
/*
                if(!injected){
                    injected = true;
                    cutoff.add(result.second.get(0));
                    //search.customActionsSingleCalc(workedActions, cutoff.get(0));
                } */
            }
        }

        // System.out.println(result.first);
/*
        if(injected && cutoff.size() >0){
            result.second.add(0, cutoff.get(0));
        }*/

        ArrayList<SpritePointData> finalSeq = search.translateSequence(search.allPossibleActions, result.second);
        // System.out.println(finalSeq);


        root = search.getLevel(finalSeq, true).getLevelMapping();
        search.resetLevel(finalSeq);
        // System.out.println("Done " + numberOfIterations);

        String name = "NMCS";
        String setting = SharedData.MIN_SIZE + "x" + SharedData.MAX_SIZE + "_level"+ level;

        CSV.writeCSV(name, setting, time,evaluated,value,avgValue);


        return search.getLevel(finalSeq, false).getLevelString(root);
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

