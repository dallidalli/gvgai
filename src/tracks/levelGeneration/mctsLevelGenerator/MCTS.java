package tracks.levelGeneration.mctsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import tracks.levelGeneration.commonClasses.*;

import java.util.*;

public class MCTS{

    public double C = 3; //0.05
    public double useSPMCTS = 1;
    public double D = 0.01; //3.5

    public ArrayList<String> allSprites = new ArrayList<String>();
    public double possiblePositions = 0;
    public ArrayList<SpritePointData> visitedMap = new ArrayList<SpritePointData>();
    public ArrayList<SpritePointData> bestSequence = new ArrayList<SpritePointData>();
    public double bestValue = Double.MIN_VALUE;
    public ArrayList<SpritePointData> currentSequence = new ArrayList<SpritePointData>();
    public double currentValue = Double.MIN_VALUE;

    public ArrayList<SpritePointData> actions = new ArrayList<SpritePointData>();
    public ArrayList<SpritePointData> workedActions = new ArrayList<SpritePointData>();

    public GeneratedLevel currentLevel;

    public MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Pair<Double, Double>> UCTvalues = new MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Pair<Double, Double>>();
    public int nTotalVisitsTree = 0;

    public MCTS(int width, int height, boolean empty, boolean SPMCTS) {
        if(SPMCTS){
            useSPMCTS = 1;
        }


        currentLevel = new GeneratedLevel(width, height);


        if (empty){
            currentLevel.InitializeEmpty();
        } else {
            if(SharedData.CONSTRUCTIVE_INITIALIZATION){
                currentLevel.InitializeConstructive();
            }
            else{
                currentLevel.InitializeRandom();
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


        calcActions();

        System.out.println("number of actions: " + actions.size());
    }

    private boolean isLeaf(){
        ArrayList<SpritePointData> tmp = new ArrayList<>();

        if(visitedMap.size() > 0){
            tmp = new ArrayList<>(visitedMap.subList(0,visitedMap.size()-1));
            Collections.sort(tmp);
            return !UCTvalues.containsKey(tmp, visitedMap.get(visitedMap.size()-1));
        } else {
            tmp = new ArrayList<>(visitedMap);
            Collections.sort(tmp);
            return !UCTvalues.containsKey(tmp);
        }

    }

    private SpritePointData selectMap(SpritePointData current) {
        double nTotalVisitsParent = 0;

        ArrayList<SpritePointData> sequence = new ArrayList<>(visitedMap);

        if(sequence.size() == 0){
            nTotalVisitsParent = nTotalVisitsTree;
        } else {
            ArrayList<SpritePointData> tmpseq = new ArrayList<SpritePointData>(visitedMap.subList(0, visitedMap.size()-1));
            Collections.sort(tmpseq);

            nTotalVisitsParent = UCTvalues.get(tmpseq, current).second;
        }

        Collections.sort(sequence);

        nTotalVisitsParent++;
        SpritePointData selected = null;
        double bestValue = Double.MIN_VALUE;

        for (SpritePointData action:workedActions) {

            double totValueChild = 0;
            double nVisitsChild = 0;

            if(UCTvalues.containsKey(sequence, action)){
                Pair<Double, Double> values = UCTvalues.get(sequence, action);
                totValueChild = values.first;
                nVisitsChild = values.second;
            }

            nVisitsChild++;


            double uctValue = 0;

            if(nTotalVisitsParent < workedActions.size()*0.2){
                uctValue = SharedData.random.nextDouble() * SharedData.EIPSLON;
            } else{
                uctValue = totValueChild / (nVisitsChild ) + C*Math.sqrt(Math.log(nTotalVisitsParent) / (nVisitsChild )) + useSPMCTS * (Math.sqrt((Math.pow(totValueChild, 2) - (nVisitsChild ) * Math.pow(totValueChild / (nVisitsChild ),2) + D) / (nVisitsChild ))) + SharedData.random.nextDouble() * SharedData.EIPSLON;
            }

            if (uctValue > bestValue) {
                selected = action;
                bestValue = uctValue;
            }
        }


        /*if(!UCTvalues.containsKey(sequence, selected)){
            Pair<Double, Double> values = new Pair<Double, Double>(0.0,0.0);
            UCTvalues.put(sequence, selected, values);
        }*/

        workedActions = customActionsSingleCalc(workedActions, selected, -1);

        return selected;
    }

    public double rollOutMap(SpritePointData cur) {
        /*visitedMap.add(cur);

        ArrayList<SpritePointData> tmpSequence = new ArrayList<>(visitedMap);
        Collections.sort(tmpSequence);
*/
        if (!isTerminal(0)){

            int selectedChild = SharedData.random.nextInt(workedActions.size());
            SpritePointData action = workedActions.get(selectedChild);
/*
            if(!UCTvalues.containsKey(tmpSequence, action)){
                Pair<Double, Double> values = new Pair<Double, Double>(0.0,0.0);
                UCTvalues.put(tmpSequence, action, values);
            }
*/          visitedMap.add(workedActions.get(selectedChild));
            workedActions = customActionsSingleCalc(workedActions, action, selectedChild);
            return rollOutMap(action);
        } else {
            getLevel(visitedMap, false);
            currentLevel.calculateSoftConstraints(false);
            double softConstraint = currentLevel.getConstrainFitness();
            resetLevel(visitedMap);
            return softConstraint;
        }
    }

    public void selectActionMap() {
        workedActions = new ArrayList<>(actions);
        visitedMap.clear();

        SpritePointData cur = null;


        while (!isLeaf() && !isTerminal(0)) {
            cur = selectMap(cur);
            visitedMap.add(cur);
        }

        if (cur == null){
            cur = selectMap(cur);
            visitedMap.add(cur);
        }

        ArrayList<SpritePointData> sequence = new ArrayList<>(visitedMap.subList(0, visitedMap.size()-1));
        Collections.sort(sequence);
        if(!UCTvalues.containsKey(sequence, cur)){
            UCTvalues.put(sequence, cur, new Pair<Double, Double>(0.0,0.0));
        }

        int index = visitedMap.size();
        double value = rollOutMap(cur);
        if(value > bestValue){
            bestValue = value;
            bestSequence = new ArrayList<>(visitedMap);
        }
        visitedMap = new ArrayList<>(visitedMap.subList(0, index));

        currentSequence = new ArrayList<>(visitedMap);
        currentValue = value;


        nTotalVisitsTree++;
        ArrayList<SpritePointData> tmpSequence = new ArrayList<>();
        Pair<Double, Double> tmpValues = UCTvalues.get(tmpSequence, visitedMap.get(0));
        tmpValues.first = tmpValues.first + value;
        tmpValues.second = tmpValues.second + 1;
        UCTvalues.put(tmpSequence, visitedMap.get(0), tmpValues);

        for (int i = 0; i < visitedMap.size()-1; i++){
            tmpSequence.add(visitedMap.get(i));
            Collections.sort(tmpSequence);


                tmpValues = UCTvalues.get(tmpSequence, visitedMap.get(i+1));
                tmpValues.first = tmpValues.first + value;
                tmpValues.second = tmpValues.second + 1;
                UCTvalues.put(tmpSequence, visitedMap.get(i+1), tmpValues);

        }
    }

    private void expand() {
        ArrayList<SpritePointData> sequence = new ArrayList<>(visitedMap);

        Collections.sort(sequence);

        for (SpritePointData action:workedActions) {
            if(!UCTvalues.containsKey(sequence, action)){
                UCTvalues.put(sequence, action, new Pair<Double, Double>(0.0,0.0));
            }
        }

    }

    public ArrayList<SpritePointData> getBestMap(){
        return bestSequence;
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

    private void calcActions(){
        double counter = 0.0;
        for (SpritePointData position : currentLevel.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites){
                SpritePointData tmp = new SpritePointData(sprite, position.x, position.y);
                actions.add(tmp);
            }
        }
        possiblePositions = counter;
    }

    private boolean isTerminal(double fitness) {
        double currentCoverage = (possiblePositions - (workedActions.size() / allSprites.size())) / possiblePositions;

        return ((currentCoverage >= SharedData.MAX_COVER_PERCENTAGE) || (fitness >= 1));
    }

    public GeneratedLevel getLevel(ArrayList<SpritePointData> prev, boolean verbose) {
        for (int i = 0; i < prev.size(); i++) {
            if (prev.get(i) != null) {
                currentLevel.setPosition(prev.get(i), prev.get(i).name);
            }
        }

        if (verbose) {
            currentLevel.calculateSoftConstraints(true);
            System.out.println(currentLevel.getConstrainFitness());
        }

        return currentLevel;
    }

    public void resetLevel(ArrayList<SpritePointData> prev) {
        for (int i = 0; i < prev.size(); i++) {
            currentLevel.clearPosition(prev.get(i));
        }

        currentLevel.resetCalculated();
    }

    public GeneratedLevel getCurrentLevel() {
        return currentLevel;
    }

}
