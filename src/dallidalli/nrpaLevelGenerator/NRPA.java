package dallidalli.nrpaLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import dallidalli.commonClasses.GeneratedLevel;
import dallidalli.commonClasses.MultiKeyHashMap;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class NRPA {

    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public double numberOfSprites;
    public MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> policy = new MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>();

    public HashMap<ArrayList<SpritePointData>, HashMap<Integer, Double>> keyMap = new HashMap<ArrayList<SpritePointData>, HashMap<Integer, Double>>((int)(10000000 / 0.75) + 1);

    public int cutoff = SharedData.NRPA_cutoff;
    public int evaluated = 0;
    public int numberOfIterations = SharedData.NRPA_numIterations;
    public double alpha = SharedData.NRPA_alpha;
    public double exploration = 0.000; // 0.002
    public boolean useNewConstraint = SharedData.useNewConstraints;


    private ArrayList<SpritePointData> tmpSequence = new ArrayList<>();
    private ArrayList<SpritePointData> seq = new ArrayList<>();
    private ArrayList<SpritePointData> actions = new ArrayList<>();
    private ArrayList<SpritePointData> best = new ArrayList<>();
    private ArrayList<Integer> best2 = new ArrayList<>();
    private ArrayList<ArrayList<SpritePointData>> sets = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> sets2 = new ArrayList<>();
    private ArrayList<Double> sumOfSets = new ArrayList<>();
    private ArrayList<SpritePointData> candidates = new ArrayList<>();
    private ArrayList<Integer> candidates2 = new ArrayList<>();
    private MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> tmpPol;
    private Set<Map.Entry<SpritePointData, Double>> others;
    private Set<Map.Entry<Integer, Double>> others2;

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

        calcActions();
        numberOfSprites = allSprites.size();


        //numberOfIterations = (int) (allPossibleActions.size()*0.3 + cutoff*allPossibleActions.size()*0.003);
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

        if(indexKnown >= 0){

        } else {
            indexKnown = allActions.indexOf(prev);
        }


        int start = (int) (Math.floor(indexKnown / allSprites.size())*allSprites.size());
        int end = (int) (start+allSprites.size());


        for (int i = start; i < end; i++) {
            allActions.remove(start);
        }

        return allActions;
    }


    public Pair<Double, ArrayList<SpritePointData>> selectAction(int level, MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> currentPolicy, Supplier<Boolean> isCanceled) {

        if (level == 0) {
            seq.clear();
            double fitness = 0;
            actions = new ArrayList<>(allPossibleActions);

            while (!isTerminal(actions, fitness)) {
                candidates.clear();
                candidates = getSuitableActions(seq, actions, currentPolicy);
                int selectedChild = SharedData.random.nextInt(candidates.size());
                seq.add(candidates.get(selectedChild));

                actions = customActionsSingleCalc(actions, candidates.get(selectedChild), -1);
                //fitness = (getEvalValue(seq));
            }
            evaluated++;

            //fitness = (getSimulationEval(seq));
            fitness = getEvalValue(seq);
            return new Pair<Double, ArrayList<SpritePointData>>(fitness, new ArrayList<>(seq));
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

                } else {
                    return bestResult;
                }

            }
            currentPolicy = adaptPolicy(currentPolicy, bestResult.second);
            policy = currentPolicy;
            return bestResult;
        }

    }

    public Pair<Pair<Double, ArrayList<Integer>>, ArrayList<SpritePointData>> selectAction2(int level, HashMap<ArrayList<SpritePointData>, HashMap<Integer, Double>> currentPolicy, Supplier<Boolean> isCanceled) {

        if (level == 0) {
            seq.clear();
            double fitness = 0;
            actions = new ArrayList<>(allPossibleActions);
            ArrayList<Integer> resultRollout = new ArrayList<>();

            while (!isTerminal(actions, fitness)) {
                candidates2 = getSuitableActions2(seq, actions, currentPolicy);
                int selectedChild;

                if(candidates2 == null){
                    selectedChild = SharedData.random.nextInt(actions.size());
                    best2 = new ArrayList<>();
                } else {
                    selectedChild = candidates2.get(SharedData.random.nextInt(candidates2.size()));
                }

                seq.add(actions.get(selectedChild));
                resultRollout.add(selectedChild);

                actions = customActionsSingleCalc(actions, actions.get(selectedChild), selectedChild);
                //fitness = (getEvalValue(seq));
            }
            evaluated++;

            //fitness = (getSimulationEval(seq));
            fitness = getEvalValue(seq);
            Pair<Double, ArrayList<Integer>> res1 = new Pair<Double, ArrayList<Integer>>(fitness, resultRollout);
            Pair<Pair<Double, ArrayList<Integer>>, ArrayList<SpritePointData>> res2 = new Pair<>(res1, new ArrayList<>(seq));

            return res2;
        } else {
            //currentPolicy = (MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>) currentPolicy.clone();
            Pair<Pair<Double, ArrayList<Integer>>, ArrayList<SpritePointData>> bestResult = new  Pair<Pair<Double, ArrayList<Integer>>, ArrayList<SpritePointData>>(new Pair<Double, ArrayList<Integer>>(Double.MIN_VALUE, null), null);
            for (int i = 0; i < numberOfIterations; i++) {

                if (!isCanceled.get()) {

                    Pair<Pair<Double, ArrayList<Integer>>, ArrayList<SpritePointData>> result = selectAction2(level - 1, currentPolicy,isCanceled);

                    if (result.first.first > bestResult.first.first) {
                        bestResult = result;
                        //System.out.println(bestResult.first);
                        //policy = currentPolicy;

                    }

                } else {
                    return bestResult;
                }

            }
            currentPolicy = adaptPolicy2(currentPolicy, bestResult.second, bestResult.first.second);
            //System.out.println(sets2.size());

            keyMap = currentPolicy;
            return bestResult;
        }

    }

    private MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> adaptPolicy(MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> currentPolicy, ArrayList<SpritePointData> seq) {
        tmpPol = (MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>) currentPolicy.clone();


        for (int i = 0; i < seq.size(); i++) {
            tmpSequence = new ArrayList<>();

            if (i == 0 || cutoff == 0) {

            } else {
                if (i > cutoff){
                    tmpSequence = new ArrayList<SpritePointData>(seq.subList(i - cutoff, i));
                } else {
                    tmpSequence = new ArrayList<SpritePointData>(seq.subList(0, i));
                }
            }

            Collections.sort(tmpSequence);

            SpritePointData key2 = seq.get(i);
            double currentValue = tmpPol.get(tmpSequence, key2);
            double newValue = currentValue + alpha;

            tmpPol.put(tmpSequence, key2, newValue);

            Collection<Double> collectionValues = currentPolicy.get(tmpSequence).values();
            double sum = 0;
            for (Double value : collectionValues) {
                sum = sum + Math.exp(value);
            }


            others = currentPolicy.get(tmpSequence).entrySet();
            for (Map.Entry<SpritePointData, Double> other : others) {
                SpritePointData k2 = other.getKey();
                double tmp = tmpPol.get(tmpSequence, k2);
                double tmp2 = currentPolicy.get(tmpSequence, k2);
                tmp = tmp - (alpha * Math.exp(tmp2) / sum);
                tmpPol.put(tmpSequence, k2, tmp);
            }



        }

        return tmpPol;
    }

    private HashMap<ArrayList<SpritePointData>, HashMap<Integer, Double>> adaptPolicy2(HashMap<ArrayList<SpritePointData>, HashMap<Integer, Double>> currentPolicy, ArrayList<SpritePointData> seq, ArrayList<Integer> seqIndex) {

        for (int i = 0; i < seq.size(); i++) {
            Integer actionIndex = seqIndex.get(i);

            tmpSequence = new ArrayList<>();

            if (i == 0 || cutoff == 0) {

            } else {
                if (i > cutoff){
                    tmpSequence = new ArrayList<SpritePointData>(seq.subList(i - cutoff, i));
                } else {
                    tmpSequence = new ArrayList<SpritePointData>(seq.subList(0, i));
                }
            }

            Collections.sort(tmpSequence);
            HashMap<Integer, Double> tmpKey2Map = new HashMap<>(keyMap.get(tmpSequence));


            double currentValue = tmpKey2Map.get(actionIndex);
            double newValue = currentValue + alpha;

            tmpKey2Map.put(actionIndex, newValue);

            Collection<Double> collectionValues = currentPolicy.get(tmpSequence).values();
            double sum = 0;
            for (Double value : collectionValues) {
                sum = sum + Math.exp(value);
            }


            others2 = currentPolicy.get(tmpSequence).entrySet();
            for (Map.Entry<Integer, Double> other : others2) {
                Integer k2 = other.getKey();

                double tmp = tmpKey2Map.get(k2);
                double tmp2 = currentPolicy.get(tmpSequence).get(k2);

                tmp = tmp - (alpha * Math.exp(tmp2) / sum);
                tmpKey2Map.put(k2, tmp);
            }


            currentPolicy.put(tmpSequence, tmpKey2Map);
        }

        return currentPolicy;
    }

    private ArrayList<SpritePointData> getSuitableActions(ArrayList<SpritePointData> sequence, ArrayList<SpritePointData> actions, MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> currentPolicy) {

        tmpSequence = new ArrayList<>(sequence);

        if(tmpSequence.size() > cutoff){
            tmpSequence = new ArrayList<SpritePointData>(tmpSequence.subList(tmpSequence.size()-cutoff, tmpSequence.size()));
        }

        Collections.sort(tmpSequence);

        best.clear();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        sets.clear();
        sumOfSets.clear();

        for (SpritePointData action : actions) {
            double currentPolicyValue = 0;


                if (currentPolicy.containsKey(tmpSequence, action)) {
                    currentPolicyValue = currentPolicy.get(tmpSequence, action);
                } else {
                    currentPolicy.put(tmpSequence, action, currentPolicyValue);
                }


            double actualValue = Math.exp(currentPolicyValue);

            if (sumOfSets.contains(actualValue)) {
                int index = sumOfSets.indexOf(actualValue);
                sets.get(index).add(action);
            } else {
                sumOfSets.add(actualValue);
                ArrayList tmp = new ArrayList<SpritePointData>();
                tmp.add(action);
                sets.add(new ArrayList<>(tmp));
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

    private ArrayList<Integer> getSuitableActions2(ArrayList<SpritePointData> sequence, ArrayList<SpritePointData> actions,  HashMap<ArrayList<SpritePointData>, HashMap<Integer, Double>> currentPolicy) {

        tmpSequence = new ArrayList<>(sequence);

        if(tmpSequence.size() > cutoff){
            tmpSequence = new ArrayList<SpritePointData>(tmpSequence.subList(tmpSequence.size()-cutoff, tmpSequence.size()));
        }

        Collections.sort(tmpSequence);

        best2.clear();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        sets2.clear();
        sumOfSets.clear();

        if(!currentPolicy.containsKey(tmpSequence)){
            currentPolicy.put(tmpSequence, new HashMap<Integer, Double>((int)(actions.size() / 0.75) + 1));
        }

        HashMap<Integer, Double> k2Map = currentPolicy.get(tmpSequence);

        boolean done = false;

        for (int i = 0; i < actions.size(); i++) {
            Integer indexAction = new Integer(i);
            double currentPolicyValue = 0;

            if(k2Map.containsKey(indexAction)){
                currentPolicyValue = k2Map.get(indexAction);
            } else {
                k2Map.put(indexAction, currentPolicyValue);
            }


            double actualValue = Math.exp(currentPolicyValue);

            if (sumOfSets.contains(actualValue)) {
                int index = sumOfSets.indexOf(actualValue);
                sets2.get(index).add(indexAction);
            } else {
                sumOfSets.add(actualValue);
                ArrayList tmp = new ArrayList<Integer>();
                tmp.add(indexAction);
                sets2.add(new ArrayList<>(tmp));
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
            best2 = null;
            done = true;
        } else if (!done){

            if (min == max) {
                best2 = null;
                done = true;
            }

            if(!done){
                ArrayList<Integer> current;
                double threshold = SharedData.random.nextDouble() * (sum);
                double tmpSum = 0;

                do{
                    int selected = SharedData.random.nextInt(sets2.size());
                    tmpSum = tmpSum + sumOfSets.get(selected) * sets2.get(selected).size();
                    current = new ArrayList<>(sets2.remove(selected));
                    if(sets2.size() == 0){
                        return current;
                    }
                }while(tmpSum < threshold);

                return current;
            }


        }

        return best2;
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

    private Double getEvalValue(ArrayList<SpritePointData> seq) {
        getLevel(seq, false);
        double value = 0;
        //level.calculateSoftConstraints(false, useNewConstraint);
        //value = level.getConstrainFitness();
        value = level.calculateFitness(SharedData.EVALUATION_TIME);
        resetLevel(seq);
        return value;
    }

    private boolean isTerminal(ArrayList<SpritePointData> actions, double fitness) {
        double currentCoverage = (possiblePositions - (actions.size() / allSprites.size())) / possiblePositions;
        //System.out.println((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) + " " +  (getSoftValue(seq) >= 1) + " "+ seq.size());
        return ((currentCoverage >= SharedData.MAX_COVER_PERCENTAGE) || fitness >= 1);
    }

}
