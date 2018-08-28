package dallidalli.nmcsLevelGenerator;

import core.game.GameDescription;
import dallidalli.commonClasses.GeneratedLevel;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;
import tools.Pair;

import java.util.ArrayList;

/**
 * Class to apply NMCS to generate a level
 */
public class NMCS {

    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public ArrayList<SpritePointData> allPossibleActionsWorked = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public int evaluated = 0;
    public boolean useNewConstraint = SharedData.useNewConstraints;
    int countBetter = 0;
    int countWorse = 0;

    private ArrayList<SpritePointData> freePositions = new ArrayList<>();


    public NMCS(int width, int height, boolean empty) {

        level = new GeneratedLevel(width, height);

        if (empty) {
            level.InitializeEmpty();
        } else {
            if (SharedData.CONSTRUCTIVE_INITIALIZATION) {
                level.InitializeConstructive();
            } else {
                level.InitializeRandom();
            }
        }

        ArrayList<String> tmp = new ArrayList<String>();
        for (GameDescription.SpriteData sprite : SharedData.gameDescription.getAllSpriteData()) {
            if(SharedData.gameAnalyzer.checkIfSpawned(sprite.name) > 0){
                tmp.add(sprite.name);
            }
        }
        allSprites.clear();
        allSprites.addAll(tmp);

        calcPositions();
        calcActionsInitial();
        calcActions();

        System.out.println("number of actions: " + allPossibleActions.size());
    }

    public ArrayList<SpritePointData> getAllPossibleActions() {
        return allPossibleActions;
    }

    private void calcPositions(){
        double counter = 0;
        for (SpritePointData position : level.getFreePositions(allSprites)) {
            counter++;
            freePositions.add(position);
        }
        possiblePositions = counter;
    }

    private void calcActionsInitial() {
        allPossibleActions.clear();

        for (SpritePointData position : level.getFreePositions(allSprites)) {
            for (String sprite : allSprites) {
                SpritePointData tmp = new SpritePointData(sprite, position.x, position.y);
                allPossibleActions.add(tmp);
            }
        }
    }

    private void calcActions() {
        allPossibleActionsWorked.clear();

        for (SpritePointData position : allPossibleActions) {
            allPossibleActionsWorked.add(position);
        }
    }

    public Pair<Double, ArrayList<Integer>> selectAction(int level, ArrayList<SpritePointData> currentState, Pair<Double, ArrayList<Integer>> oldResult) {


        if(level == 0){
            return rollout2(currentState);
        }

        Pair<Double, ArrayList<Integer>> globalBest = oldResult;
        ArrayList<Integer> visitedActions = new ArrayList<Integer>();

        while (!isTerminal(currentState.size(), 0)) {


            Pair<Double, ArrayList<Integer>> currentBest = new Pair<>(Double.MIN_VALUE, new ArrayList<Integer>());
            int currentBestAction = -1;

            for(int i = 0; i < currentState.size(); i++){
                ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                Pair<Double, ArrayList<Integer>> move = selectAction(level-1, customActionsSingleCalc(copy, i), new Pair<Double, ArrayList<Integer>>(Double.MIN_VALUE, new ArrayList<Integer>()));

                if(move.first > currentBest.first){
                    currentBestAction = i;
                    currentBest = move;
                }

            }


            if(currentBest.first >= globalBest.first){
                visitedActions.add(currentBestAction);
                globalBest = currentBest;
                globalBest.second.addAll(0, visitedActions);
            } else {
                if(visitedActions.size() > 0 && visitedActions.size() >= globalBest.second.size()){
                    //break;
                }

                currentBestAction = globalBest.second.get(visitedActions.size());
                visitedActions.add(currentBestAction);
            }

            currentState = customActionsSingleCalc(currentState, currentBestAction);
        }

        double fitness = getEvalValue(new ArrayList<>(allPossibleActions),globalBest.second);

        if(fitness >= globalBest.first){
            countBetter++;
        }else{
            countWorse++;
        }

        globalBest.first = fitness;

        if(globalBest.first > oldResult.first)
            return globalBest;
        else
            return oldResult;

    }

    public Pair<Double, ArrayList<Integer>> selectAction2(int level, ArrayList<SpritePointData> currentState, Pair<Double, ArrayList<Integer>> oldResult){
        int ply = 0;
        double fitness = 0;
        double bestScore = oldResult.first;
        ArrayList<Integer> curSeq = new ArrayList<Integer>();
        ArrayList<SpritePointData> curSeqAction = new ArrayList<SpritePointData>();
        ArrayList<Integer> bestSeq = new ArrayList<>(oldResult.second);

        ArrayList<SpritePointData> backupState = new ArrayList<>(currentState);

        while (!isTerminal(currentState.size(), fitness)) {


            double tmpBest = Double.MIN_VALUE;
            ArrayList<Integer> tmpSeq = new ArrayList<>();
            int bestIndex = -1;

            if(level == 1){

                for(int i = 0; i < currentState.size(); i++){
                    ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                    Pair<Double, ArrayList<Integer>> move = rollout(new ArrayList<>(currentState), i);

                    if(move.first > tmpBest){
                        tmpBest = move.first;
                        tmpSeq = new ArrayList<Integer>(move.second);
                    }
                }
            } else {

                for(int i = 0; i < currentState.size(); i++){
                    ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                    Pair<Double, ArrayList<Integer>> move = selectAction2(level-1, customActionsSingleCalc(copy, i), new Pair<Double, ArrayList<Integer>>(Double.MIN_VALUE, new ArrayList<Integer>()));
                    move.second.add(0,i);

                    if(move.first > tmpBest){
                        tmpBest = move.first;
                        tmpSeq = new ArrayList<>(move.second);
                    }
                }
            }

            if(tmpBest > bestScore){
                bestScore = tmpBest;
                bestSeq = new ArrayList<>(tmpSeq);
                ply = 0;
            }

            if(ply >= bestSeq.size() && ply != 0){
                break;
            }

            bestIndex = bestSeq.get(ply);
            curSeq.add(bestIndex);
            curSeqAction.add(currentState.get(bestIndex));

            currentState = customActionsSingleCalc(currentState, bestIndex);

            ply++;


        }
        fitness = getEvalValueRollout(curSeqAction);

        if(fitness >= bestScore){
            countBetter++;
            return new Pair<Double, ArrayList<Integer>>(fitness, curSeq);
        } else {
            countWorse++;
            return new Pair<Double, ArrayList<Integer>>(bestScore, bestSeq);
        }

    }

    private Pair<Double,ArrayList<Integer>> rollout2(ArrayList<SpritePointData> currentState) {
        ArrayList<Integer> seqIndex = new ArrayList<>();
        ArrayList<SpritePointData> seq = new ArrayList<>();

        double fitness = 0;


        while(!isTerminal(currentState.size(), fitness)){
            int selectedChild = SharedData.random.nextInt(currentState.size());
            seqIndex.add(selectedChild);
            seq.add(currentState.get(selectedChild));
            currentState = customActionsSingleCalc(currentState, selectedChild);
        }


        fitness = getEvalValueRollout(seq);


        evaluated++;
        return new Pair<Double, ArrayList<Integer>>(fitness, seqIndex);
    }

    private Pair<Double,ArrayList<Integer>> rollout(ArrayList<SpritePointData> currentState, int lastMove) {
        ArrayList<Integer> seqIndex = new ArrayList<>();
        ArrayList<SpritePointData> seq = new ArrayList<>();


        seqIndex.add(lastMove);
        seq.add(currentState.get(lastMove));

        currentState = customActionsSingleCalc(currentState, lastMove);

        double fitness = 0;


        while(!isTerminal(currentState.size(), fitness)){
            int selectedChild = SharedData.random.nextInt(currentState.size());
            seqIndex.add(selectedChild);
            seq.add(currentState.get(selectedChild));
            currentState = customActionsSingleCalc(currentState, selectedChild);
        }


        fitness = getEvalValueRollout(seq);


        evaluated++;
        return new Pair<Double, ArrayList<Integer>>(fitness, seqIndex);
    }


    public GeneratedLevel getLevel(ArrayList<SpritePointData> prev, boolean verbose) {
        for (int i = 0; i < prev.size(); i++) {
            if (prev.get(i) != null) {
                level.setPosition(prev.get(i), prev.get(i).name);
            }
        }

        if (verbose) {
            level.calculateSoftConstraints(true, useNewConstraint);
            System.out.println(level.getConstrainFitness());
        }

        return level;
    }

    public void resetLevel(ArrayList<SpritePointData> prev) {
        for (int i = 0; i < prev.size(); i++) {
            level.clearPosition(prev.get(i));
        }

        level.resetCalculated();
    }

    public ArrayList<SpritePointData> translateSequence(ArrayList<SpritePointData> state, ArrayList<Integer> seqIndex) {
        ArrayList<SpritePointData> seq = new ArrayList<>();

        for (int i = 0; i < seqIndex.size(); i++) {
            seq.add(state.get(seqIndex.get(i)));
            state = customActionsSingleCalc(state, seqIndex.get(i));
        }

        return seq;
    }

    private Double getEvalValue(ArrayList<SpritePointData> state, ArrayList<Integer> seqIndex) {
        ArrayList<SpritePointData> seq = translateSequence(state, seqIndex);

        getLevel(seq, false);
        double value = 0;
        value = level.calculateFitness(SharedData.EVALUATION_TIME);
        resetLevel(seq);
        return value;
    }


    private Double getEvalValueRollout(ArrayList<SpritePointData> seq) {
        getLevel(seq, false);
        double value = 0;
        value = level.calculateFitness(SharedData.EVALUATION_TIME);
        resetLevel(seq);
        return value;
    }

    private boolean isTerminal(double size, double fitness) {
        double currentCoverage = (possiblePositions - (size / allSprites.size())) / possiblePositions;
        return ((currentCoverage >= SharedData.desiredCoverage) || fitness >= 1);
    }

    public ArrayList<SpritePointData> customActionsSingleCalc(ArrayList<SpritePointData> allActions, int indexKnown) {
        int start = (int) (Math.floor(indexKnown / allSprites.size())*allSprites.size());
        int end = (start + allSprites.size());


        for (int i = start; i < end; i++) {
            allActions.remove(start);
        }

        return allActions;
    }


}
