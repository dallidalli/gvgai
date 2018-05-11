package dallidalli.mctsLevelGenerator;

import core.game.GameDescription;
import dallidalli.commonClasses.*;

import java.util.*;

public class MCTS{

    public double C = SharedData.MCTS_Cvalue; //0.05
    public double useSPMCTS = 0;
    public double D = 0; //3.5
    public boolean useNewConstraint = SharedData.useNewConstraints;

    public ArrayList<String> allSprites = new ArrayList<String>();

    public double possiblePositions = 0;
    public ArrayList<Integer> visitedIndex = new ArrayList<Integer>();
    public ArrayList<SpritePointData> visitedAction = new ArrayList<SpritePointData>();
    public ArrayList<SpritePointData> bestSequence = new ArrayList<SpritePointData>();
    public double bestValue = Double.MIN_VALUE;
    public ArrayList<SpritePointData> currentSequence = new ArrayList<SpritePointData>();
    public double currentValue = Double.MIN_VALUE;
    public int numberOfNodes = 1;

    public ArrayList<SpritePointData> actions = new ArrayList<SpritePointData>();
    public ArrayList<SpritePointData> workedActions = new ArrayList<SpritePointData>();

    public GeneratedLevel currentLevel;

    public Node root;

    public ArrayList<SpritePointData> freePositions = new ArrayList<SpritePointData>();
    public ArrayList<SpritePointData> totalActions = new ArrayList<>();

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

        root = new Node();

        System.out.println("number of actions: " + actions.size());
    }

    private void calcPositions() {
        double counter = 0;

        for (SpritePointData position : currentLevel.getFreePositions(allSprites)) {
            counter++;
            freePositions.add(position);
        }
        possiblePositions = counter;
    }

    private boolean isLeaf(Node current){
        return current.getChildren() == null;
    }

    private int select(Node current) {
        double nTotalVisitsParent = current.getTotalVisits();

        nTotalVisitsParent++;

        int selectedIndex = -1;
        double bestValue = Double.MIN_VALUE;

        for (int i = 0; i < workedActions.size(); i++) {
            double totValueChild = 0;
            double nVisitsChild = 0;

            if(current.getChildren()[i] != null){
                totValueChild = current.getChildren()[i].getTotalValue();
                nVisitsChild = current.getChildren()[i].getTotalVisits();
            }



            double uctValue = 0;

            /*
            if(nTotalVisitsParent < actions.size()*0.2){
                uctValue = SharedData.random.nextDouble() * SharedData.EIPSLON;
            } else{
                uctValue = totValueChild / (nVisitsChild ) + C*Math.sqrt(Math.log(nTotalVisitsParent) / (nVisitsChild )) + SharedData.random.nextDouble() * SharedData.EIPSLON;
            }
            */


            uctValue = totValueChild / (nVisitsChild + SharedData.EIPSLON)
                    + C*Math.sqrt(Math.log(nTotalVisitsParent) / (nVisitsChild + SharedData.EIPSLON))
                    + SharedData.random.nextDouble() * SharedData.EIPSLON;

            if (uctValue > bestValue) {
                selectedIndex = i;
                bestValue = uctValue;
            }
        }


        return selectedIndex;
    }

    public double rollOut(int selectedChild) {

        if (!isTerminal(0)){
            selectedChild = SharedData.random.nextInt(workedActions.size());
            SpritePointData action = workedActions.get(selectedChild);
            visitedAction.add(action);
            workedActions = customActionsSingleCalc(workedActions, selectedChild);
            return rollOut(selectedChild);
        } else {
            getLevel(visitedAction, false);
            //currentLevel.calculateSoftConstraints(false, useNewConstraint);
            //double softConstraint = currentLevel.getConstrainFitness();
            double softConstraint = currentLevel.calculateFitness(SharedData.EVALUATION_TIME);
            resetLevel(visitedAction);
            return softConstraint;
        }
    }

    public void selectAction() {
        workedActions = new ArrayList<>(actions);
        visitedAction = new ArrayList<>();
        visitedIndex = new ArrayList<>();

        int curIndex = Integer.MAX_VALUE;
        Node currentNode = root;


        while (!isLeaf(currentNode) && !isTerminal(0)) {
            curIndex = select(currentNode);
            visitedIndex.add(curIndex);

            if(currentNode.getChildren()[curIndex] == null){
                currentNode.setChild(curIndex, new Node());
                numberOfNodes++;
            }

            currentNode = currentNode.getChildren()[curIndex];

            visitedAction.add(workedActions.get(curIndex));
            workedActions = customActionsSingleCalc(workedActions, curIndex);
        }


        if(isLeaf(currentNode)){
            currentNode.expand(workedActions.size());
        }

        if(curIndex == Integer.MAX_VALUE){
            curIndex = select(currentNode);
            visitedIndex.add(curIndex);

            if(currentNode.getChildren()[curIndex] == null){
                currentNode.setChild(curIndex, new Node());
                numberOfNodes++;
            }

            visitedAction.add(workedActions.get(curIndex));
            workedActions = customActionsSingleCalc(workedActions, curIndex);
        }


        double value = rollOut(curIndex);
        if(value > bestValue){
            bestValue = value;
            bestSequence = new ArrayList<>(visitedAction);
        }

        currentSequence = new ArrayList<>(visitedAction);
        currentValue = value;


        root.update(value);
        currentNode = root;
        for (int i = 0; i < visitedIndex.size(); i++) {
            currentNode = currentNode.getChildren()[visitedIndex.get(i)];
            currentNode.update(value);
        }

        //calcActions();
    }


    public ArrayList<SpritePointData> getBestMap(){
        return bestSequence;
    }

    private ArrayList<SpritePointData> customActionsSingleCalc(ArrayList<SpritePointData> allActions, int indexKnown) {


        int start = (int) (Math.floor(indexKnown / allSprites.size())*allSprites.size());
        int end = (int) (start+allSprites.size());


        for (int i = start; i < end; i++) {
            allActions.remove(start);
        }

        return allActions;
    }

    private void calcActions(){
        actions.clear();

        for (SpritePointData position : totalActions) {
            actions.add(position);
        }
    }

    private void calcActionsInitial(){
        totalActions.clear();

        for (SpritePointData position : freePositions) {
            for (String sprite : allSprites){
                SpritePointData tmp = new SpritePointData(sprite, position.x, position.y);
                totalActions.add(tmp);
            }
        }
    }

    private boolean isTerminal(double fitness) {
        double currentCoverage = (possiblePositions - (workedActions.size() / allSprites.size())) / possiblePositions;
        return ((currentCoverage >= SharedData.desiredCoverage) || (fitness >= 1));
    }

    public GeneratedLevel getLevel(ArrayList<SpritePointData> prev, boolean verbose) {
        for (int i = 0; i < prev.size(); i++) {
            if (prev.get(i) != null) {
                currentLevel.setPosition(prev.get(i), prev.get(i).name);
            }
        }

        if (verbose) {
            currentLevel.calculateSoftConstraints(true, useNewConstraint);
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

    public void restart() {
        root = new Node();
        numberOfNodes = 1;
    }

    public void restart2(){
        int deleted = 0;

        for (int i = 0; i < root.getChildren().length; i++) {
            if(root.getChildren()[i] != null){
                if(SharedData.random.nextDouble() < (0.5) && root.getChildren()[i].getBestValue() < bestValue){
                    deleted += amountNodes(root.getChildren()[i]);
                    root.getChildren()[i].reset();
                }
            }
        }

        if(deleted > 0){
            System.gc();
            numberOfNodes -= deleted;
        }

    }

    public int amountNodes(Node current){
        int counter = 1;

        if(current.getChildren() != null){
            for (int i = 0; i < current.getChildren().length; i++) {

                if(current.getChildren()[i] != null){
                    counter = counter + amountNodes(current.getChildren()[i]);
                }
            }
        }

        return counter;
    }

}
