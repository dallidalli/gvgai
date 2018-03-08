package tracks.levelGeneration.nrpaLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import tracks.levelGeneration.commonClasses.GeneratedLevel;
import tracks.levelGeneration.commonClasses.MultiKeyHashMap;
import tracks.levelGeneration.commonClasses.SharedData;
import tracks.levelGeneration.commonClasses.SpritePointData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class NRPA {

    public final ArrayList<ArrayList<SpritePointData>> generatedSequences = new ArrayList<>();
    public final ConcurrentLinkedQueue listOfSequences = new ConcurrentLinkedQueue();
    public final ConcurrentHashMap tmpMap = new ConcurrentHashMap();
    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public double numberOfSprites;
    public MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> policy = new MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>();

    public int cutoff = 0;
    public int evaluated = 0;
    public int assumedDepth = 2;
    public int numberOfIterations = 500;
    public double alpha = 1;
    public double exploration = 0.000; // 0.002

    public NRPA(int width, int height, boolean empty) {

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

        System.out.println(SharedData.gameAnalyzer.getGoalSprites());
        System.out.println(SharedData.gameAnalyzer.getCollectableSprites());
        System.out.println(SharedData.gameAnalyzer.getOtherSprites());

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
        numberOfSprites = allSprites.size();

        numberOfIterations = (int) (allPossibleActions.size()*0.3 + cutoff*allPossibleActions.size()*0.003);
        //addSequence(allPossibleActions, new ArrayList<>());
        //generateTableFromMap(0);

        System.out.println("number of actions: " + allPossibleActions.size());
    }

    private void calcActions() {
        double counter = 0;
        for (SpritePointData position : level.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites) {
                SpritePointData tmp = new SpritePointData(sprite, position.x, position.y);
                allPossibleActions.add(tmp);
            }
        }
        possiblePositions = counter;

    }


    public ArrayList<SpritePointData> getAllPossibleActions() {
        return allPossibleActions;
    }

    private ArrayList<SpritePointData> customActionsSingleCalc(ArrayList<SpritePointData> allActions, SpritePointData prev, int indexKnown) {
        int index;

        if(indexKnown >= 0){
            index = indexKnown;
        } else {
            index = allActions.indexOf(prev);
        }


        int start = (int) (Math.floor(index / numberOfSprites)*numberOfSprites);
        int end = (int) (start+numberOfSprites);

        ArrayList<SpritePointData> reducedActions = (ArrayList<SpritePointData>) allActions.clone();
        ArrayList<SpritePointData> toBeDeleted = new ArrayList<>();

        for (int i = start; i < end; i++) {
            toBeDeleted.add(allActions.get(i));
        }

        reducedActions.removeAll(toBeDeleted);
        toBeDeleted = null;
        return reducedActions;
    }


    public Pair<Double, ArrayList<SpritePointData>> selectAction(int level, MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> currentPolicy, Supplier<Boolean> isCanceled) {

        if (level == 0) {
            ArrayList<SpritePointData> seq = new ArrayList<SpritePointData>();
            double fitness = 0;
            ArrayList<SpritePointData> actions = new ArrayList<>(allPossibleActions);

            while (!isTerminal(actions, fitness)) {
                ArrayList<SpritePointData> candidates = getSuitableActions(seq, actions, currentPolicy);
                int selectedChild = SharedData.random.nextInt(candidates.size());
                seq.add(candidates.get(selectedChild));

                actions = customActionsSingleCalc(actions, candidates.get(selectedChild), -1);
                //fitness = (getEvalValue(seq));
            }
            evaluated++;

            //fitness = (getSimulationEval(seq));
            fitness = getEvalValue(seq);
            return new Pair<Double, ArrayList<SpritePointData>>(fitness, seq);
        } else {
            //currentPolicy = (MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>) currentPolicy.clone();
            Pair<Double, ArrayList<SpritePointData>> bestResult = new Pair<Double, ArrayList<SpritePointData>>(Double.MIN_VALUE, null);
            for (int i = 0; i < numberOfIterations; i++) {

                if (!isCanceled.get()) {

                    Pair<Double, ArrayList<SpritePointData>> result = selectAction(level - 1, currentPolicy,isCanceled);

                    if (result.first > bestResult.first) {
                        bestResult = result;
                        //System.out.println(bestResult.first);
                        //policy = currentPolicy;

                    }

                    currentPolicy = adaptPolicy(currentPolicy, bestResult.second);
                } else {
                    return bestResult;
                }

            }
            policy = currentPolicy;
            return bestResult;
        }

    }

    private double getSimulationEval(ArrayList<SpritePointData> seq) {
        getLevel(seq, false);
        double value = 0;
        level.calculateSoftConstraints(false);
        ArrayList<Double> finalFitness = level.calculateFitness(SharedData.EVALUATION_TIME);
        value = level.getConstrainFitness();
        resetLevel(seq);
        if(value >= 1){
            return (finalFitness.get(0) + finalFitness.get(1))/2;
        } else {
            return value;
        }
    }

    private MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> adaptPolicy(MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> currentPolicy, ArrayList<SpritePointData> seq) {
        MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> tmpPol = (MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>) currentPolicy.clone();


        for (int i = 0; i < seq.size(); i++) {
            ArrayList<SpritePointData> key1;

            if (i == 0) {
                key1 = new ArrayList<>();
            } else {
                if (i > cutoff){
                    key1 = new ArrayList<SpritePointData>(seq.subList(i - cutoff, i));
                } else {
                    key1 = new ArrayList<SpritePointData>(seq.subList(0, i));
                }
            }

            Collections.sort(key1);

            SpritePointData key2 = seq.get(i);
            double currentValue = tmpPol.get(key1, key2);
            double newValue = currentValue + alpha;

            tmpPol.put(key1, key2, newValue);

            Collection<Double> collectionValues = currentPolicy.get(key1).values();
            double sum = 0;
            for (Double value : collectionValues) {
                sum = sum + Math.exp(value);
            }


            Set<Map.Entry<SpritePointData, Double>> others = currentPolicy.get(key1).entrySet();
            for (Map.Entry<SpritePointData, Double> other : others) {
                SpritePointData k2 = other.getKey();
                double tmp = tmpPol.get(key1, k2);
                double tmp2 = currentPolicy.get(key1, k2);
                tmp = tmp - (alpha * Math.exp(tmp2) / sum);
                tmpPol.put(key1, k2, tmp);
            }



        }

        return tmpPol;
    }

    private ArrayList<SpritePointData> getSuitableActions(ArrayList<SpritePointData> sequence, ArrayList<SpritePointData> actions, MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> currentPolicy) {

        ArrayList<SpritePointData> tmpSequence = new ArrayList<>(sequence);

        if(tmpSequence.size() > cutoff){
            tmpSequence = new ArrayList<SpritePointData>(tmpSequence.subList(tmpSequence.size()-cutoff, tmpSequence.size()));
        }

        Collections.sort(tmpSequence);

        ArrayList<SpritePointData> best = new ArrayList<>();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        ArrayList<ArrayList<SpritePointData>> sets = new ArrayList<>();
        ArrayList<Double> sumOfSets = new ArrayList<>();

        for (SpritePointData action : actions) {
            double currentPolicyValue = 0;

            if (currentPolicy.containsKey(tmpSequence, action)) {
                currentPolicyValue = currentPolicy.get(tmpSequence, action);
            } else {
                currentPolicy.put(tmpSequence, new SpritePointData(action.name, action.x, action.y), currentPolicyValue);
            }

            double actualValue = Math.exp(currentPolicyValue);

            if (sumOfSets.contains(actualValue)) {
                int index = sumOfSets.indexOf(actualValue);
                sets.get(index).add(action);
            } else {
                sumOfSets.add(actualValue);
                ArrayList tmp = new ArrayList<SpritePointData>();
                tmp.add(action);
                sets.add((ArrayList<SpritePointData>) tmp.clone());
            }

            sum = sum + actualValue;

            if (actualValue < min) {
                min = actualValue;
            }

            if (actualValue > max) {
                max = actualValue;
            }
        }

        double explore = SharedData.random.nextDouble();

        if (explore < exploration) {
            return actions;
        } else {

            if (min == max) {
                best.addAll(actions);
                return best;
            }

            double threshold = SharedData.random.nextDouble() * (sum);
            double tmpSum = 0;

            for (int i = 0; i < sumOfSets.size(); i++) {
                tmpSum = tmpSum + sumOfSets.get(i) * sets.get(i).size();
                if (tmpSum >= threshold) {
                    return sets.get(i);
                }
            }


        }

        return actions;
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

}
