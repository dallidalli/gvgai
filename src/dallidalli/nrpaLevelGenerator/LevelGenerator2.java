package dallidalli.nrpaLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import dallidalli.commonClasses.CSV;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.LevelMapping;
import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LevelGenerator2 extends AbstractLevelGenerator{

    private final Object bestLevel;
    private ArrayList<Double> bestFitness;
    private LevelMapping root;

    public LevelGenerator2(GameDescription game, ElapsedCpuTimer elapsedTimer){
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

        NRPA2 search = new NRPA2(width, height, true);
        search.level.calculateSoftConstraints(true, SharedData.useNewConstraints);
        //LevelEvaluationFunction eval = new LevelEvaluationFunction();
        //eval.generateEvaluationFunction();


        //some variables to make sure not getting out of time
        double worstTime = SharedData.EVALUATION_TIME * 1;
        double avgTime = worstTime;
        double totalTime = 0;
        int numberOfIterations = 0;
        int level = SharedData.NRPA_level;

        ArrayList<String> time = new ArrayList<>();
        ArrayList<String> evaluated = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();
        ArrayList<String> avgValue = new ArrayList<>();
        //SharedData.random.setSeed(42);


        double averageScore = 0;
        Pair<Pair<Double, ArrayList<Integer>>, Policy> result = new Pair<Pair<Double, ArrayList<Integer>>, Policy>(new Pair(Double.MIN_VALUE, new ArrayList<Integer>()), search.emptyPolicy);
        //        Pair<Double, ArrayList<SpritePointData>> result = null;
        Policy test = new Policy(search.emptyPolicy);
        long endTimeMs = System.currentTimeMillis() + elapsedTimer.remainingTimeMillis();

        ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> result2 = null;

        System.out.println(numberOfIterations + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){
            ElapsedCpuTimer timer = new ElapsedCpuTimer();


            //Pair<Pair<Double, ArrayList<Integer>>, Policy> tmp = search.recursiveNRPA(SharedData.NRPA_level, search.emptyPolicy, result.first);
            ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> pairs = search.recursiveBeamNRPA(SharedData.NRPA_level, new Policy(search.emptyPolicy), result2);


            averageScore += pairs.get(0).first.first;
            numberOfIterations += 1;
            totalTime += timer.elapsedMillis();
            avgTime = totalTime / numberOfIterations;
            //search.emptyPolicy = result.second;


            if(result2 == null || pairs.get(0).first.first > result2.get(0).first.first){
                result2 = pairs;
            }

/*
            test = tmp.second;

            if(tmp.first.first >= result.first.first){
                result = tmp;
            }


            search.emptyPolicy = result.second;





            time.add(String.valueOf(totalTime));
            evaluated.add(String.valueOf(search.evaluated));
            value.add(String.valueOf(tmp.first.first));
            avgValue.add(String.valueOf((averageScore / numberOfIterations)));
*/

            if(numberOfIterations % 1 == 0){
                System.out.println(averageScore/numberOfIterations);
                for(int i = 0; i < pairs.size(); i++){
                    System.out.print(pairs.get(i).first.first);
                    System.out.print(" ");
                }
                System.out.println();
                //System.out.println(pairs.get(0).first.first);
                //System.out.println(tmp.first.first);
                //search.getLevel(tmp.second, true).getLevelMapping();
                //search.resetLevel(tmp.second);
                //System.out.println(numberOfIterations + " " + search.evaluated + " " + search.keyMap.entrySet().size() + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
                System.out.println(numberOfIterations + " " + search.evaluated+ " "+ search.emptyPolicy.size());
            }



            /*
            Pair<Double, ArrayList<SpritePointData>> tmp = search.selectAction(1,search.policy, () -> {return System.currentTimeMillis() > endTimeMs;});

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

            if(numberOfIterations % 1 == 0){
                System.out.println(averageScore/numberOfIterations);
                //search.getLevel(tmp.second, true).getLevelMapping();
                //search.resetLevel(tmp.second);
                System.out.println(numberOfIterations + " " + search.evaluated + " " + search.policy.getAllItems().size() + " " + elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
            }

            */
        }

        //System.out.println(result.first);
        //System.out.println(result.second);

        root = search.getLevel(search.translate(result.first.second), true).getLevelMapping();
        search.resetLevel(search.translate(result.first.second));
        //System.out.println("Done");

        String name = "NRPA2";
        String setting = SharedData.MIN_SIZE + "x" + SharedData.MAX_SIZE + "_level"+ level + "_cutoff"+ search.cutoff+ "_alpha" + String.valueOf(search.alpha) + "_iterations" + search.numberOfIterations;

//        CSV.writeCSV(name, setting, time,evaluated,value,avgValue);

        return search.getLevel(search.translate(result.first.second), false).getLevelString(root);
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

