package tracks.levelGeneration.nmcsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.ArrayList;
import java.util.function.Supplier;

public class NMCS {

    public ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allPossibleActions = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public int evaluated = 0;
    public LevelEvaluationFunction eval;

    public NMCS(int width, int height, boolean empty){


        level = new GeneratedLevel(width, height);

        if (empty){
            level.InitializeEmpty();
        } else {
            if(SharedData.CONSTRUCTIVE_INITIALIZATION){
                level.InitializeConstructive();
            }
            else{
                level.InitializeRandom();
            }
        }

        ArrayList<String> tmp = new ArrayList<String>();
        for (GameDescription.SpriteData sprite : SharedData.gameDescription.getAllSpriteData()) {
            tmp.add(sprite.name);
        }
        allSprites.clear();
        allSprites.addAll(tmp);

        calcActions();

        eval = new LevelEvaluationFunction();
        eval.generateEvaluationFunction();

        System.out.println("number of actions: " + allPossibleActions.size());
    }

    public ArrayList<Pair<GeneratedLevel.SpritePointData, String>> getAllPossibleActions() {
        return allPossibleActions;
    }

    private void calcActions(){
        double counter = 0;
        for (GeneratedLevel.SpritePointData position : level.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites){
                allPossibleActions.add(new Pair<GeneratedLevel.SpritePointData, String>(position, sprite));
            }
        }
        possiblePositions = counter;
    }

    public Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>> selectAction(int level, ArrayList<Pair<GeneratedLevel.SpritePointData, String>> actions, Supplier<Boolean> isCanceled){
        if(level == 0){
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> seq = new ArrayList<>();
        double fitness = (getEvalValue(seq));

        while(!isTerminal(actions, seq, fitness)){
            int selectedChild = SharedData.random.nextInt(actions.size());
            seq.add(actions.get(selectedChild));

            actions = customActionsSingle(actions, actions.get(selectedChild));
            fitness = (getEvalValue(seq));
        }
        evaluated++;
        return new Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData,String>>>(fitness, seq);
    } else {
            ArrayList<Pair<GeneratedLevel.SpritePointData, String>> seq = new ArrayList<>();
            Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>> globalBestResult = new Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>>(Double.MIN_VALUE, null);

            while (!isTerminal(actions, seq, 0) && !isCanceled.get()){

                Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>> currentBestResult = new Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>>(Double.MIN_VALUE, null);
                Pair<GeneratedLevel.SpritePointData, String> currentBestAction = null;


                for(int i = 0; i < actions.size(); i++){
                    ArrayList<Pair<GeneratedLevel.SpritePointData, String>> newActions = customActionsSingle(actions, actions.get(i));
                    Pair<Double, ArrayList<Pair<GeneratedLevel.SpritePointData, String>>> result = selectAction(level-1, newActions, isCanceled);

                    if (result.first >= currentBestResult.first){
                        currentBestAction = actions.get(i);
                        currentBestResult = result;
                    }

                }



                if(currentBestResult.first > globalBestResult.first){
                    seq.add(currentBestAction);
                    globalBestResult = currentBestResult;
                    globalBestResult.second.addAll(0, seq);
                } else {
                    currentBestAction = globalBestResult.second.get(seq.size());
                    seq.add(currentBestAction);
                }

                System.out.println(evaluated);
                actions = customActionsSingle(actions, currentBestAction);
            }

            return globalBestResult;
        }

    }

    public GeneratedLevel getLevel(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> prev, boolean verbose) {
        for(int i = 0; i<prev.size(); i++){
            if (prev.get(i) != null){
                level.setPosition(prev.get(i).first, prev.get(i).second);
            }
        }

        if(verbose){
            level.calculateSoftConstraints(true);
            System.out.println(level.getConstrainFitness());
        }

        return level;
    }

    public void resetLevel(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> prev){
        for(int i = 0; i<prev.size(); i++){
                level.clearPosition(prev.get(i).first);
        }

        level.resetCalculated();
    }

    private Double getEvalValue(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> seq){
        getLevel(seq, false);
        double value = 0;
        level.calculateSoftConstraints(false);
        value = level.getConstrainFitness();
        resetLevel(seq);
        return value;
    }

    private boolean isTerminal(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> actions, ArrayList<Pair<GeneratedLevel.SpritePointData, String>> seq, double fitness) {
        double currentCoverage = (possiblePositions - (actions.size() / allSprites.size())) / possiblePositions;
        //System.out.println((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) + " " +  (getSoftValue(seq) >= 1) + " "+ seq.size());
        return ((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) || fitness >= 1);
    }


    private ArrayList<Pair<GeneratedLevel.SpritePointData, String>> customActionsSingle(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allActions, Pair<GeneratedLevel.SpritePointData, String> prev){
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> reducedActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>) allActions.clone();
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> toBeDeleted = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();

        for (Pair<GeneratedLevel.SpritePointData, String> action:reducedActions) {
                if(prev.first.x == action.first.x && prev.first.y == action.first.y) {
                    toBeDeleted.add(action);
                }
        }

        reducedActions.removeAll(toBeDeleted);
        toBeDeleted = null;
        return reducedActions;
    }

}
