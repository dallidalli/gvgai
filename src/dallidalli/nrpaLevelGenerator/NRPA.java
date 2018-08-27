package dallidalli.nrpaLevelGenerator;

import core.game.GameDescription;
import dallidalli.commonClasses.GeneratedLevel;
import dallidalli.commonClasses.MultiKeyHashMap;
import dallidalli.commonClasses.SharedData;
import dallidalli.commonClasses.SpritePointData;
import tools.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NRPA {

    public Policy emptyPolicy;
    private final List<Integer> actionIndicies;

    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public double numberOfSprites;
    public MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double> policy = new MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Double>();

    public int cutoff = SharedData.NRPA_cutoff;
    public int evaluated = 0;
    public int numberOfIterations = SharedData.NRPA_numIterations;
    public double alpha = SharedData.NRPA_alpha;
    public boolean useNewConstraint = SharedData.useNewConstraints;

    public ArrayList<Policy> policiesL = new ArrayList<>();
    public ArrayList<Double> scoreL = new ArrayList<>();
    //public ArrayList<ArrayList<Integer>> sequenceL = new ArrayList<>();

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

        emptyPolicy = new Policy(allPossibleActions.size(), (int) numberOfSprites, 0);
        actionIndicies = IntStream.range(0, allPossibleActions.size()).boxed().collect(Collectors.toList());

        for(int i = 0; i <= SharedData.NRPA_level; i++){
            scoreL.add(Double.MIN_VALUE);
            policiesL.add(new Policy(emptyPolicy, true));
        }

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


    private ArrayList<Integer> customActionsSingleCalc(ArrayList<Integer> currentActions, int action) {
        int start = (int) (Math.floor(action / numberOfSprites)*numberOfSprites);
        int end = (int) (start+numberOfSprites);

        List<Integer> range = IntStream.range(start, end).boxed().collect(Collectors.toList());

        currentActions.removeAll(range);
        return currentActions;
    }

    public Pair<Pair<Double, ArrayList<Integer>>, Policy> recursiveNRPA(int level, Policy p, Pair<Double, ArrayList<Integer>> prevBest){
        //double best = scoreL.get(level);
        //double best = Double.MIN_VALUE;


        Pair<Pair<Double, ArrayList<Integer>>, Policy> bestResult, curResult;

        if(level == 0){
            ArrayList<Integer> seq = new ArrayList<>();
            double fitness = 0;
            ArrayList<Integer> legalActions = new ArrayList<>(actionIndicies);

            ArrayList<Integer> tmpState;

            while(!isTerminal(legalActions, fitness)){

                if(seq.size() <= cutoff){
                    tmpState = new ArrayList<>(seq);
                }else{
                    tmpState = new ArrayList<>(seq.subList(seq.size()-cutoff,seq.size()));
                }

                Collections.sort(tmpState);


                int randIndex;

                if(p.contains(tmpState)){
                    List<Double> values = p.getValues(tmpState, legalActions);

                    double sum = 0;

                    for (int j = 0; j < values.size(); j++) {
                        sum += Math.exp(values.get(j));
                    }

                    double threshold = SharedData.random.nextDouble() * sum;
                    double tmpSum = 0;
                    randIndex = SharedData.random.nextInt(values.size());
                    tmpSum += Math.exp(values.remove(randIndex));

                    while (tmpSum < threshold) {
                        randIndex = SharedData.random.nextInt(values.size());
                        tmpSum += Math.exp(values.remove(randIndex));
                    }
                } else {
                    randIndex = SharedData.random.nextInt(legalActions.size());
                }


                seq.add(legalActions.get(randIndex));
                legalActions = customActionsSingleCalc(legalActions, legalActions.get(randIndex));
            }

            evaluated++;

            bestResult = new Pair<>(new Pair<>(getEvalValue(translate(seq)), seq), p);

        }else {



            bestResult = new Pair<>(prevBest, p);
            boolean foundBetter = false;

            if(level == SharedData.NRPA_level){
                foundBetter = true;
            }

            for (int i = 0; i < numberOfIterations; i++) {
                if(level > 1){
                    curResult = recursiveNRPA(level-1, new Policy(p, true), new Pair<>(Double.MIN_VALUE, new ArrayList<Integer>()));
                } else {
                    curResult = recursiveNRPA(level-1, new Policy(p, false), new Pair<>(Double.MIN_VALUE, new ArrayList<Integer>()));

                }

                if(curResult.first.first >= bestResult.first.first){
                    if(curResult.first.first >= bestResult.first.first){
                        foundBetter = true;
                        //System.out.println("better");
                    }

                    bestResult = curResult;
                }


                if(foundBetter){
                    p = adapt(bestResult.first, p);
                    foundBetter = false;
                }

            }

            /*
            if(foundBetter){
                p = adapt(bestResult.first, p);
            }
            */
        }

        bestResult.second = p;

        if(level == 1){
            //System.out.println(p.size());
        }
        //scoreL.set(level, bestResult.first.first);
        return bestResult;
    }

    public ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> recursiveBeamNRPA(int level, Policy p, ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> prevBest){
        //double best = scoreL.get(level);
        //double best = Double.MIN_VALUE;

        if(level == 0){
            ArrayList<Integer> seq = new ArrayList<>();
            double fitness = 0;
            ArrayList<Integer> legalActions = new ArrayList<>(actionIndicies);

            ArrayList<Integer> tmpState;

            while(!isTerminal(legalActions, fitness)){

                if(seq.size() <= cutoff){
                    tmpState = new ArrayList<>(seq);
                }else{
                    tmpState = new ArrayList<>(seq.subList(seq.size()-cutoff,seq.size()));
                }

                Collections.sort(tmpState);


                int randIndex;

                if(p.contains(tmpState)){
                    List<Double> values = p.getValues(tmpState, legalActions);

                    double sum = 0;

                    for (int j = 0; j < values.size(); j++) {
                        sum += Math.exp(values.get(j));
                    }

                    double threshold = SharedData.random.nextDouble() * sum;
                    double tmpSum = 0;
                    randIndex = SharedData.random.nextInt(values.size());
                    tmpSum += Math.exp(values.remove(randIndex));

                    while (tmpSum < threshold) {
                        randIndex = SharedData.random.nextInt(values.size());
                        tmpSum += Math.exp(values.remove(randIndex));
                    }
                } else {
                    randIndex = SharedData.random.nextInt(legalActions.size());
                }


                seq.add(legalActions.get(randIndex));
                legalActions = customActionsSingleCalc(legalActions, legalActions.get(randIndex));
            }

            evaluated++;

            ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>>  rolloutResult = new ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>>();
            rolloutResult.add(new Pair<>(new Pair<>(getEvalValue(translate(seq)), seq), null));
            return rolloutResult;
        }else {

            ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> beam = new ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>>();


            if(prevBest == null){
            } else {
                beam.addAll(prevBest);
            }

            beam.add(new Pair<>(new Pair<>(Double.MIN_VALUE, new ArrayList<Integer>()), p));


            for (int i = 0; i < numberOfIterations; i++) {

                ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> newBeam = new ArrayList<>();

                for (int j = 0; j < beam.size(); j++) {

                    boolean added = false;
                    for (int l = 0; l < newBeam.size(); l++) {
                        if(newBeam.get(l).first.first < beam.get(j).first.first){
                            newBeam.add(l, beam.get(j));
                            added = true;
                            break;
                        }
                    }

                    if(!added){
                        newBeam.add(beam.get(j));
                    }

                    ArrayList<Pair<Pair<Double, ArrayList<Integer>>, Policy>> beam1 = recursiveBeamNRPA(level-1, new Policy(p, true),null);

                    for (int k = 0; k < beam1.size(); k++) {

                        int index = 0;
                        boolean added2 = false;
                        for (int l = 0; l < newBeam.size(); l++) {
                            if(newBeam.get(l).first.first < beam1.get(k).first.first){
                                index = l;
                                added2 = true;
                                break;
                            }
                        }

                        if(!added2 || index >= SharedData.NRPA_B){
                            //newBeam.add(beam1.get(k));
                        } else {
                            beam1.get(k).second = adapt(beam1.get(k).first, p);
                            newBeam.add(index, beam1.get(k));
                        }
                    }
                }

                int end = SharedData.NRPA_B;

                if(beam.size() < SharedData.NRPA_B){
                    end = beam.size();
                }

                beam = new ArrayList<>(newBeam.subList(0, end));

            }

            return beam;
        }

    }

    private Policy adapt(Pair<Double, ArrayList<Integer>> bestResult, Policy p){
        Policy newP = new Policy(p, true);

        ArrayList<Integer> state = new ArrayList<>();
        ArrayList<Integer> legalActions = new ArrayList<>(actionIndicies);

        for (int i = 0; i < bestResult.second.size()-1; i++) {
            ArrayList<Integer> tmpState = new ArrayList<>(state);
            Collections.sort(tmpState);

            newP.verifyState(tmpState, actionIndicies, (int) numberOfSprites);
            newP.changeSingleValue(tmpState, bestResult.second.get(i), alpha);

            p.verifyState(tmpState, actionIndicies, (int) numberOfSprites);
            List<Double> values = p.getValues(tmpState, legalActions);

            double sum = 0;

            for (int j = 0; j < values.size(); j++) {
                sum += Math.exp(values.get(j));
            }

            for (int j = 0; j < legalActions.size(); j++) {
                newP.changeSingleValue(tmpState, legalActions.get(j), -(alpha * Math.exp(p.getSingleValue(tmpState, legalActions.get(j))) / sum));
            }

            if(cutoff > 0){
                state.add(bestResult.second.get(i));
                if(state.size() > cutoff){
                    state = new ArrayList<Integer>(state.subList(1,cutoff+1));
                }
            }

            legalActions = customActionsSingleCalc(legalActions, bestResult.second.get(i));
        }

        return newP;
    }


    public ArrayList<SpritePointData> translate(ArrayList<Integer> seq){
        ArrayList<SpritePointData> tmpSeq = new ArrayList<>();

        for (int i = 0; i < seq.size(); i++) {
            tmpSeq.add(allPossibleActions.get(seq.get(i)));
        }

        return tmpSeq;
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

    private boolean isTerminal(ArrayList<Integer> actions, double fitness) {
        double currentCoverage = (possiblePositions - (actions.size() / allSprites.size())) / possiblePositions;
        //System.out.println((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) + " " +  (getSoftValue(seq) >= 1) + " "+ seq.size());
        return ((currentCoverage >= SharedData.desiredCoverage) || fitness >= 1);
    }

}
