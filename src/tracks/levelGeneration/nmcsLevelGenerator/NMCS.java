package tracks.levelGeneration.nmcsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import tracks.levelGeneration.commonClasses.GeneratedLevel;
import tracks.levelGeneration.commonClasses.SharedData;
import tracks.levelGeneration.commonClasses.SpritePointData;

import java.util.ArrayList;
import java.util.function.Supplier;

public class NMCS {

    public ArrayList<SpritePointData> allPossibleActions = new ArrayList<>();
    public ArrayList<SpritePointData> allPossibleActionsWorked = new ArrayList<>();
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
            tmp.add(sprite.name);
        }
        allSprites.clear();
        allSprites.addAll(tmp);

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

    public Pair<Double, ArrayList<SpritePointData>> selectAction(int level, SpritePointData selectedAction, Supplier<Boolean> isCanceled) {
        resetActions();
        int counter = 0;
        ArrayList<SpritePointData> seq = new ArrayList<SpritePointData>();

        if (level == 0) {

            if(selectedAction != null){
                seq.add(selectedAction);
                allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, selectedAction, -1);
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
                seq.add(selectedAction);
                allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, selectedAction, -1);
            }

            double fitness = getEvalValue(seq);

            while (!isTerminal(allPossibleActionsWorked, fitness) && !isCanceled.get() && !terminated) {

                for (int i = 0; i < allPossibleActionsWorked.size(); i++) {
                    ArrayList<SpritePointData> actionsBackup = new ArrayList<>(allPossibleActionsWorked);
                    SpritePointData currentAction = allPossibleActionsWorked.get(i);
                    Pair<Double, ArrayList<SpritePointData>> result = selectAction(level - 1, currentAction, isCanceled);
                    allPossibleActionsWorked = actionsBackup;
                    if (result.first >= bestValue) {
                        bestValue = result.first;
                        seq.clear();
                        seq.addAll(result.second);
                        counter = 0;
                    }

                    fitness = (getEvalValue(seq));

                    if(seq.size() == counter){
                        terminated = true;
                        break;
                    }
                    allPossibleActionsWorked = customActionsSingleCalc(allPossibleActionsWorked, seq.get(counter), -1);
                    counter = counter + 1;
                }

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
        return ((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) || fitness >= 1);
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
