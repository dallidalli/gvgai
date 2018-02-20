package tracks.levelGeneration.nrpaLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NRPA {

    public ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allPossibleActions = new ArrayList<>();
    public ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allPossibleActionsTmp = new ArrayList<>();
    public GeneratedLevel level;
    public ArrayList<String> allSprites = new ArrayList<>();
    public double possiblePositions;
    public MultiKeyHashMap<ArrayList<Pair<GeneratedLevel.SpritePointData, String>>, Pair<GeneratedLevel.SpritePointData, String>, Double> policy = new MultiKeyHashMap<>();

    public NRPA(int width, int height, boolean empty){


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

    private void calcActions() {
        double counter = 0;
        for (GeneratedLevel.SpritePointData position : level.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites) {
                allPossibleActions.add(new Pair<GeneratedLevel.SpritePointData, String>(position, sprite));
                allPossibleActionsTmp.add(new Pair<GeneratedLevel.SpritePointData, String>(position, sprite));
            }
        }
        possiblePositions = counter;
    }

    private ArrayList<Pair<GeneratedLevel.SpritePointData, String>> customActionsSingle(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allActions, Pair<GeneratedLevel.SpritePointData, String> prev) {
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> reducedActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>) allActions.clone();
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> toBeDeleted = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();

        for (Pair<GeneratedLevel.SpritePointData, String> action : reducedActions) {
            if (prev.first.x == action.first.x && prev.first.y == action.first.y) {
                toBeDeleted.add(action);
            }
        }

        reducedActions.removeAll(toBeDeleted);
        toBeDeleted = null;
        return reducedActions;
    }

    private void constructPolicies(int depth, ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allPossibleActionsTmp){

    }

}
