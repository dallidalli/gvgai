package tracks.levelGeneration.nmcsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import tracks.levelGeneration.commonClasses.GeneratedLevel;
import tracks.levelGeneration.commonClasses.SharedData;
import tracks.levelGeneration.commonClasses.SpritePointData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Supplier;

public class NMCS {

    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public ArrayList<SpritePointData> allPossibleActionsWorked = new ArrayList<>();
    public HashMap<ArrayList<SpritePointData>, Double> results = new HashMap<>(100000000);
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public int evaluated = 0;

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

        calcActions();

        System.out.println("number of actions: " + allPossibleActions.size());
    }

    public ArrayList<SpritePointData> getAllPossibleActions() {
        return allPossibleActions;
    }

    private void calcActions() {
        double counter = 0;
        for (SpritePointData position : level.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites) {
                SpritePointData tmp = new SpritePointData(sprite, position.x, position.y);
                allPossibleActions.add(tmp);
                allPossibleActionsWorked.add(tmp);
            }
        }
        possiblePositions = counter;
    }

    public void resetActions(){
        allPossibleActionsWorked = new ArrayList<>(allPossibleActions);
    }

    public Pair<Double, ArrayList<SpritePointData>> selectAction2(int level, ArrayList<SpritePointData> currentState, Supplier<Boolean> isCanceled){
        int ply = 0;
        double fitness = 0;
        double bestScore = Double.MIN_VALUE;
        ArrayList<SpritePointData> curSeq = new ArrayList<SpritePointData>();
        ArrayList<SpritePointData> bestSeq = new ArrayList<SpritePointData>();

        while (!isTerminal(currentState, fitness) && !isCanceled.get()) {

            double tmpBest = Double.MIN_VALUE;
            ArrayList<SpritePointData> tmpSeq = new ArrayList<>();
            SpritePointData bestMove = null;


            if(level == 1){

                for(int i = 0; i < currentState.size(); i++){
                    ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                    Pair<Double, ArrayList<SpritePointData>> move = rollout(customActionsSingleCalc(copy, copy.get(i), i), currentState.get(i));

                    if(move.first > tmpBest){
                        tmpBest = move.first;
                        tmpSeq = new ArrayList<>(move.second);
                    }
                }
            } else {

                for(int i = 0; i < currentState.size(); i++){
                    ArrayList<SpritePointData> copy = new ArrayList<>(currentState);

                    Pair<Double, ArrayList<SpritePointData>> move = selectAction2(level-1, customActionsSingleCalc(copy, copy.get(i), i), isCanceled);
                    move.second.add(0,currentState.get(i));

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

            bestMove = bestSeq.get(ply);
            curSeq.add(bestMove);

            //System.out.println(bestSeq.size() + " " + curSeq.size());

            currentState = customActionsSingleCalc(currentState, bestMove, -1);
            ply++;
        }

        ArrayList<SpritePointData> sorted = new ArrayList<>(curSeq);
        Collections.sort(sorted);
        if(results.containsKey(sorted)){
            fitness = results.get(sorted);
        } else {
            fitness = getEvalValue(curSeq);
            results.put(sorted, fitness);
        }

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

        System.out.println(evaluated + " " + results.size());
        System.out.println(fitness);

        return new Pair<Double, ArrayList<SpritePointData>>(fitness, curSeq);

        /*
        if(fitness > bestScore){
            return new Pair<Double, ArrayList<SpritePointData>>(fitness, curSeq);
        } else {
            return new Pair<Double, ArrayList<SpritePointData>>(bestScore, bestSeq);
        }
        */
    }

    public Pair<Double,ArrayList<SpritePointData>> iterativeNested(ArrayList<SpritePointData> allActions, int level, Supplier<Boolean> isCanceled){
        Pair<Double, ArrayList<SpritePointData>> best = new Pair<>(Double.MIN_VALUE, new ArrayList<SpritePointData>());
        double sum = 0;
        double counter = 0;

        while (!isCanceled.get()){
            counter++;
            Pair<Double, ArrayList<SpritePointData>> tmp = selectAction2(level, new ArrayList<>(allActions), isCanceled);

            sum+=tmp.first;
            System.out.println(sum/counter);

            if(tmp.first > best.first){
                best = new Pair<>(tmp.first, tmp.second);
            }
        }

        return best;
    }

    private Pair<Double,ArrayList<SpritePointData>> rollout(ArrayList<SpritePointData> currentState, SpritePointData lastMove) {
        ArrayList<SpritePointData> seq = new ArrayList<>();
        seq.add(lastMove);
        double fitness = 0;



        while(!isTerminal(currentState, fitness)){
            int selectedChild = SharedData.random.nextInt(currentState.size());
            seq.add(currentState.get(selectedChild));
            currentState = customActionsSingleCalc(currentState, currentState.get(selectedChild), selectedChild);
        }

        ArrayList<SpritePointData> sorted = new ArrayList<>(seq);
        Collections.sort(sorted);
        if(results.containsKey(sorted)){
            fitness = results.get(sorted);
        } else {
            fitness = getEvalValue(seq);
            results.put(sorted, fitness);
        }

        evaluated++;
        return new Pair<Double, ArrayList<SpritePointData>>(fitness, seq);
    }

    public Pair<Double, ArrayList<SpritePointData>> selectAction(int level, ArrayList<SpritePointData> selectedAction, Supplier<Boolean> isCanceled) {
        resetActions();
        int counter = 0;
        ArrayList<SpritePointData> seq = new ArrayList<SpritePointData>();

        if (level == 0) {

            if(selectedAction != null){
                for (SpritePointData action:selectedAction) {
                    seq.add(action);
                    allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, action, -1);
                }
            }

            double fitness = getEvalValue(seq);

            while (!isTerminal(allPossibleActionsWorked, fitness)) {
                int selectedChild = SharedData.random.nextInt(allPossibleActionsWorked.size());
                seq.add(allPossibleActionsWorked.get(selectedChild));

                allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, allPossibleActionsWorked.get(selectedChild), selectedChild);
                fitness = (getEvalValue(seq));
            }
            evaluated++;
            return new Pair<Double, ArrayList<SpritePointData>>(fitness, seq);
        } else {
            double bestValue = Double.MIN_VALUE;

            boolean terminated = false;

            if(selectedAction != null){
                    seq.add(selectedAction.get(0));
                    allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, selectedAction.get(0), -1);
            }

            double fitness = getEvalValue(seq);

            while (!isTerminal(allPossibleActionsWorked, fitness) && !isCanceled.get() && !terminated) {
                for (int i = 0; i < allPossibleActionsWorked.size(); i++) {
                    ArrayList<SpritePointData> actionsBackup = new ArrayList<>(allPossibleActionsWorked);
                    SpritePointData currentAction = allPossibleActionsWorked.get(i);
                    ArrayList<SpritePointData> next = new ArrayList<SpritePointData>();
                    next.add(currentAction);
                    Pair<Double, ArrayList<SpritePointData>> result = selectAction(level - 1,next , isCanceled);
                    allPossibleActionsWorked = actionsBackup;
                    if (result.first >= bestValue) {
                        bestValue = result.first;
                        seq.clear();
                        seq.addAll(result.second);
                        counter = 0;
                    }


                    if(seq.size() == counter){
                        terminated = true;
                        break;
                    }
                }

                allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, seq.get(counter), -1);
                fitness = (getEvalValue(seq));
                counter = counter + 1;

            }

            return new Pair<Double, ArrayList<SpritePointData>>(bestValue, seq);
        }

    }

    public GeneratedLevel getLevel(ArrayList<SpritePointData> prev, boolean verbose) {
        for (int i = 0; i < prev.size(); i++) {
            if (prev.get(i) != null) {
                level.setPosition(prev.get(i), prev.get(i).name);
            }
        }

        if (verbose) {
            level.calculateSoftConstraints(true);
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

    private Double getEvalValue(ArrayList<SpritePointData> seq) {
        getLevel(seq, false);
        double value = 0;
        level.calculateSoftConstraints(false);
        value = level.getConstrainFitness();
        resetLevel(seq);
        return value;
    }

    private boolean isTerminal(ArrayList<SpritePointData> actions, double fitness) {
        double currentCoverage = (possiblePositions - (actions.size() / allSprites.size())) / possiblePositions;
        //System.out.println((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) + " " +  (getSoftValue(seq) >= 1) + " "+ seq.size());
        return ((currentCoverage >= SharedData.MAX_COVER_PERCENTAGE) || fitness >= 1);
    }

    private ArrayList<SpritePointData> customActionsSingleCalc(ArrayList<SpritePointData> allActions, SpritePointData prev, int indexKnown) {
        int index;

        if(indexKnown >= 0){
            index = indexKnown;
        } else {
            index = allActions.indexOf(prev);
        }


        int start = (int) (Math.floor(index / allSprites.size())*allSprites.size());
        int end = (int) (start+allSprites.size());

        ArrayList<SpritePointData> toBeDeleted = new ArrayList<>();

        for (int i = start; i < end; i++) {
            toBeDeleted.add(allActions.get(i));
        }

        allActions.removeAll(toBeDeleted);
        toBeDeleted = null;
        return allActions;
    }


}
