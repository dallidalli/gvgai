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
            }
        }
        possiblePositions = counter;
    }

    public Pair<Double, ArrayList<SpritePointData>> selectAction(int level, ArrayList<SpritePointData> actions, int selected, Supplier<Boolean> isCanceled) {
        if (level == 0) {
            ArrayList<SpritePointData> seq = new ArrayList<SpritePointData>();

            if(selected != -1){
                seq.add(actions.get(selected));
                actions = customActionsSingle(actions, actions.get(selected));
            }

            double fitness = getEvalValue(seq);

            while (!isTerminal(actions, seq, fitness)) {
                int selectedChild = SharedData.random.nextInt(actions.size());
                seq.add(actions.get(selectedChild));

                actions = customActionsSingle(actions, actions.get(selectedChild));
                fitness = (getEvalValue(seq));
            }
            evaluated++;
            return new Pair<Double, ArrayList<SpritePointData>>(fitness, seq);
        } else {
            ArrayList<SpritePointData> seq = new ArrayList<SpritePointData>();
            Pair<Double, ArrayList<SpritePointData>> globalBestResult = new Pair<Double, ArrayList<SpritePointData>>(Double.MIN_VALUE, null);
            int counter = 0;

            if(selected != -1){
                seq.add(actions.get(selected));
                actions = customActionsSingle(actions, actions.get(selected));
            }


            double fitness = getEvalValue(seq);

            while (!isTerminal(actions, seq, fitness) && !isCanceled.get()) {

                Pair<Double, ArrayList<SpritePointData>> currentBestResult = new Pair<Double, ArrayList<SpritePointData>>(Double.MIN_VALUE, null);
                SpritePointData currentBestAction = null;


                for (int i = 0; i < actions.size(); i++) {
                    Pair<Double, ArrayList<SpritePointData>> result = selectAction(level - 1, actions, i, isCanceled);

                    if (result.first >= currentBestResult.first) {
                        currentBestAction = actions.get(i);
                        currentBestResult = result;
                    }

                }


                if (currentBestResult.first >= globalBestResult.first) {
                    seq.add(currentBestAction);
                    globalBestResult = currentBestResult;
                    counter = 0;
                } else {
                    try {
                        currentBestAction = globalBestResult.second.get(counter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    seq.add(currentBestAction);
                }

                System.out.println(evaluated);
                counter++;
                fitness = (getEvalValue(seq));
                actions = customActionsSingle(actions, currentBestAction);
            }

            if(fitness > globalBestResult.first){
                System.out.println("meta");
                return new Pair<Double, ArrayList<SpritePointData>>(fitness, seq);
            }
            System.out.println("nested");
            return globalBestResult;
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

    private boolean isTerminal(ArrayList<SpritePointData> actions, ArrayList<SpritePointData> seq, double fitness) {
        double currentCoverage = (possiblePositions - (actions.size() / allSprites.size())) / possiblePositions;
        //System.out.println((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) + " " +  (getSoftValue(seq) >= 1) + " "+ seq.size());
        return ((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) || fitness >= 1);
    }


    private ArrayList<SpritePointData> customActionsSingle(ArrayList<SpritePointData> allActions, SpritePointData prev) {
        ArrayList<SpritePointData> reducedActions = (ArrayList<SpritePointData>) allActions.clone();
        ArrayList<SpritePointData> toBeDeleted = new ArrayList<SpritePointData>();

        for (SpritePointData action : reducedActions) {
            if (prev.sameCoordinate(action)) {
                toBeDeleted.add(action);
            }
        }

        reducedActions.removeAll(toBeDeleted);
        toBeDeleted = null;
        return reducedActions;
    }

}
