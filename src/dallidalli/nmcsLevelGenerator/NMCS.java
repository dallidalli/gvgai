package dallidalli.nmcsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import dallidalli.commonClasses.GeneratedLevel;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;

import java.util.ArrayList;
import java.util.function.Supplier;

public class NMCS {

    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public ArrayList<SpritePointData> allPossibleActionsWorked = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public int evaluated = 0;
    public boolean useNewConstraint = SharedData.useNewConstraints;

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

        /*
        ArrayList<String> tmpHarmful = new ArrayList<>();
        ArrayList<String> tmpOther = new ArrayList<>();
        ArrayList<String> tmpCollectable = new ArrayList<>();
        ArrayList<String> tmpSolid = new ArrayList<>();
        ArrayList<String> tmpAvatar = new ArrayList<>();

        for (String sprite:allSprites) {
            if(SharedData.gameAnalyzer.getHarmfulSprites().contains(sprite)){
                tmpHarmful.add(sprite);
            } else if(SharedData.gameAnalyzer.getOtherSprites().contains(sprite)){
                tmpOther.add(sprite);
            } else if(SharedData.gameAnalyzer.getCollectableSprites().contains(sprite)){
                tmpCollectable.add(sprite);
            } else if(SharedData.gameAnalyzer.getSolidSprites().contains(sprite)){
                tmpSolid.add(sprite);
            } else if(SharedData.gameAnalyzer.getAvatarSprites().contains(sprite)){
                tmpAvatar.add(sprite);
            }
        }

        int maxAmount = Math.max(tmpHarmful.size(), Math.max(tmpOther.size(), Math.max(tmpCollectable.size(), Math.max(tmpSolid.size(), tmpAvatar.size()))));

        for (int i = 0; i < maxAmount - tmpHarmful.size(); i++){
            allSprites.add(tmpHarmful.get(SharedData.random.nextInt(tmpHarmful.size())));
        }
        for (int i = 0; i < maxAmount - tmpOther.size(); i++){
            allSprites.add(tmpOther.get(SharedData.random.nextInt(tmpOther.size())));
        }
        for (int i = 0; i < maxAmount - tmpCollectable.size() -1; i++){
            allSprites.add(tmpCollectable.get(SharedData.random.nextInt(tmpCollectable.size())));
        }
        for (int i = 0; i < maxAmount - tmpSolid.size() +1; i++){
            allSprites.add(tmpSolid.get(SharedData.random.nextInt(tmpSolid.size())));
        }
        for (int i = 0; i < maxAmount - tmpAvatar.size(); i++){
            allSprites.add(tmpAvatar.get(SharedData.random.nextInt(tmpAvatar.size())));
        }
        */

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

    public void resetActions(){
        calcActions();
    }

    public Pair<Double, ArrayList<Integer>> selectAction2(int level, ArrayList<SpritePointData> currentState, Pair<Double, ArrayList<Integer>> oldResult, Supplier<Boolean> isCanceled){
        int ply = 0;
        double fitness = 0;
        double bestScore = oldResult.first;
        ArrayList<Integer> curSeq = new ArrayList<Integer>();
        ArrayList<SpritePointData> curSeqAction = new ArrayList<SpritePointData>();
        ArrayList<Integer> bestSeq = oldResult.second;
        ArrayList<SpritePointData> backupState = new ArrayList<>(currentState);

        while (!isTerminal(currentState.size(), fitness) && !isCanceled.get()) {

            double tmpBest = Double.MIN_VALUE;
            ArrayList<Integer> tmpSeq = new ArrayList<>();
            int bestIndex = -1;

            if(level == 1){

                for(int i = 0; i < currentState.size(); i++){
                    ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                    Pair<Double, ArrayList<Integer>> move = rollout(copy, i);

                    if(move.first > tmpBest){
                        tmpBest = move.first;
                        tmpSeq = new ArrayList<Integer>(move.second);
                    }
                }
            } else {

                for(int i = 0; i < currentState.size(); i++){
                    ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                    Pair<Double, ArrayList<Integer>> move = selectAction2(level-1, customActionsSingleCalc(copy, i), new Pair<Double, ArrayList<Integer>>(Double.MIN_VALUE, new ArrayList<Integer>()), isCanceled);
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

            bestIndex = bestSeq.get(ply);
            curSeq.add(bestIndex);
            curSeqAction.add(currentState.get(bestIndex));

            currentState = customActionsSingleCalc(currentState, bestIndex);

            ply++;
        }
        //System.out.println(evaluated+ " " + currentState.size() + " "+ bestSeq.size() + " " + curSeq.size());

        fitness = getEvalValueRollout(curSeqAction);

        /*
        sorted = new ArrayList<>(bestSeq);
        Collections.sort(sorted);
        if(results.containsKey(sorted)){
            bestScore = results.get(sorted);
        } else {
            bestScore = getEvalValue(bestSeq);
            results.put(sorted, bestScore);
        }
        */

        return new Pair<Double, ArrayList<Integer>>(fitness, curSeq);
        /*
        if(fitness > bestScore){
            return new Pair<Double, ArrayList<SpritePointData>>(fitness, curSeq);
        } else {
            return new Pair<Double, ArrayList<SpritePointData>>(bestScore, bestSeq);
        }
        */
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
        level.calculateSoftConstraints(false, useNewConstraint);
        value = level.getConstrainFitness();
        resetLevel(seq);
        return value;
    }


    private Double getEvalValueRollout(ArrayList<SpritePointData> seq) {
        getLevel(seq, false);
        double value = 0;
        level.calculateSoftConstraints(false, useNewConstraint);
        value = level.getConstrainFitness();
        resetLevel(seq);
        return value;
    }

    private boolean isTerminal(double size, double fitness) {
        double currentCoverage = (possiblePositions - (size / allSprites.size())) / possiblePositions;
        //System.out.println((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) + " " +  (getSoftValue(seq) >= 1) + " "+ seq.size());
        return ((currentCoverage >= SharedData.MAX_COVER_PERCENTAGE) || fitness >= 1);
    }

    public ArrayList<SpritePointData> customActionsSingleCalc(ArrayList<SpritePointData> allActions, int indexKnown) {
        int start = (int) (Math.floor(indexKnown / allSprites.size())*allSprites.size());
        int end = (int) (start+allSprites.size());


        for (int i = start; i < end; i++) {
            allActions.remove(start);
        }

        return allActions;
    }


}
