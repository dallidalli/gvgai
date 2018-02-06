package tracks.levelGeneration.nmcsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class TreeNode{

    public static ArrayList<String> allSprites = new ArrayList<String>();
    public static double possiblePositions = 0;
    public static LinkedList<TreeNode> visited = new LinkedList<TreeNode>();
    public static HashMap<GeneratedLevel, Double> previousEvaluations = new HashMap<GeneratedLevel, Double>();
    public static HashMap<GeneratedLevel, Double> previousSoftConstraints = new HashMap<GeneratedLevel, Double>();
    public static HashMap<GeneratedLevel, Double> previousHardConstraints = new HashMap<GeneratedLevel, Double>();


    private TreeNode[] children;

    public static ArrayList<Pair<GeneratedLevel.SpritePointData, String>> actions = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();
    private ArrayList<Pair<GeneratedLevel.SpritePointData, String>> reducedActions = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();


    public static GeneratedLevel level;
    private GeneratedLevel currentLevel;

    public TreeNode(int width, int height, boolean empty) {
        level = new GeneratedLevel(width, height);


        if (empty){
            level.InitializeEmpty();
        } else {
            if(SharedData.CONSTRUCTIVE_INITIALIZATION){
                level.InitializeConstructive();
            }
            else{
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
        reducedActions = actions;
        expand();

        System.out.println("number of actions: " + actions.size());
    }

    public TreeNode(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> customActions){
        reducedActions = customActions;
    }


    public Pair<Double, Pair<GeneratedLevel.SpritePointData, String>[]> selectAction(int level, TreeNode node) {
        if (level == 0){
            int ply = 0;
            Pair<GeneratedLevel.SpritePointData, String>[] seq = new Pair[1];
            ArrayList<Pair<GeneratedLevel.SpritePointData, String>> reducedActions = customActions(node.reducedActions, seq);
            //node.expand(reducedActions);
            node.simpleExpand(reducedActions);

            while (!node.isLeaf(reducedActions)){
                int selectedChild = SharedData.random.nextInt(reducedActions.size());

                if (ply >= seq.length){
                    Pair<GeneratedLevel.SpritePointData, String>[] newArray = new Pair[seq.length * 2];
                    System.arraycopy(seq, 0, newArray, 0, seq.length);
                    seq = newArray;
                }

                seq[ply] = reducedActions.get(selectedChild);
                reducedActions = customActions(node.reducedActions, seq);

                node.children[selectedChild].reducedActions = reducedActions;

                TreeNode child = node.children[selectedChild];
                child.expand(reducedActions);
                node = child;
                ply++;
            }
            node.calculateSoftConstraints(seq);
            return new Pair<Double, Pair<GeneratedLevel.SpritePointData, String>[]>(node.currentLevel.getConstrainFitness(), seq);
        } else {
            int ply = 0;
            int bestChild = 0;
            double bestResult = Double.MIN_VALUE;
            Pair<GeneratedLevel.SpritePointData, String>[] seq = new Pair[1];
            ArrayList<Pair<GeneratedLevel.SpritePointData, String>> reducedActions = customActions(node.reducedActions, seq);
            node.expand(reducedActions);

            while (!node.isLeaf(reducedActions)) {

                for (int n = 0; n < reducedActions.size(); n++) {

                    Pair<Double, Pair<GeneratedLevel.SpritePointData, String>[]> new_seq = selectAction(level-1, node.children[n]);

                    if (new_seq.first > bestResult){
                        bestResult = new_seq.first;

                        if (ply >= seq.length){
                            Pair<GeneratedLevel.SpritePointData, String>[] newArray = new Pair[seq.length * 2];
                            System.arraycopy(seq, 0, newArray, 0, seq.length);
                            seq = newArray;
                        }

                        seq[ply] = reducedActions.get(n);
                        bestChild = n;

                        for (int i = 0; i< new_seq.second.length;i++){
                            if (new_seq.second[i] != null){
                                if (ply+i+1 >= seq.length){
                                    Pair<GeneratedLevel.SpritePointData, String>[] newArray;
                                    newArray = new Pair[seq.length * 2];
                                    System.arraycopy(seq, 0, newArray, 0, seq.length);
                                    seq = newArray;
                                }
                                seq[ply+i+1] = new_seq.second[i];
                            }
                        }
                    }
                }
                reducedActions = customActions(node.reducedActions, seq);

                node = node.children[bestChild];
                node.expand(reducedActions);

                ply++;
            }

            return new Pair<Double, Pair<GeneratedLevel.SpritePointData, String>[]>(bestResult, seq);
        }
    }

    private void simpleExpand(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> customActions) {
        if (children == null) {
            children = new TreeNode[customActions.size()];
        }
    }

    public GeneratedLevel getLevel(Pair<GeneratedLevel.SpritePointData, String>[] prev) {
        GeneratedLevel toBeReturned = level.clone();
        for(int i = 0; i<prev.length; i++){
            if (prev[i] != null){
                toBeReturned.setPosition(prev[i].first, prev[i].second);
            }
        }

        return toBeReturned;
    }

    private void calculateSoftConstraints(Pair<GeneratedLevel.SpritePointData, String>[] prev) {
        currentLevel = level.clone();
        for(int i = 0; i<prev.length; i++){
            if (prev[i] != null){
                currentLevel.setPosition(prev[i].first, prev[i].second);
            }
        }
        currentLevel.calculateSoftConstraints();
    }

    private void expand() {
        if (children == null) {
            children = new TreeNode[actions.size()-1];

            for(int i = 0; i < children.length; i++){
                Pair<GeneratedLevel.SpritePointData, String>[] chosen = new Pair[1];
                chosen[0] = actions.get(i);
                children[i] = new TreeNode(customActions(actions, chosen));
            }
        }
        /*
        int selectedChild = SharedData.random.nextInt(actions.size());
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> newActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>)(actions.clone());
        newActions.remove(selectedChild);
        children[selectedChild] = new TreeNode(currentLevel, newActions);


        for (int i=0; i<actions.size(); i++) {
            ArrayList<Pair<GeneratedLevel.SpritePointData, String>> newActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>)(actions.clone());
            newActions.remove(i);
            children[i] = new TreeNode(currentLevel, newActions);
        }
        */

    }

    private void expand(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> customActions){
        if (children == null) {
            children = new TreeNode[customActions.size()];

            for(int i = 0; i < children.length; i++){
                Pair<GeneratedLevel.SpritePointData, String>[] chosen = new Pair[1];
                chosen[0] = customActions.get(i);
                children[i] = new TreeNode(customActions(customActions, chosen));
            }
        }
    }

    private ArrayList<Pair<GeneratedLevel.SpritePointData, String>> customActions(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> allActions, Pair<GeneratedLevel.SpritePointData, String>[] prev){
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> reducedActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>) allActions.clone();
        ArrayList<Pair<GeneratedLevel.SpritePointData, String>> toBeDeleted = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();

        for (Pair<GeneratedLevel.SpritePointData, String> action:reducedActions) {
            for (Pair<GeneratedLevel.SpritePointData, String> performedAction: Arrays.asList(prev)) {
                if(performedAction != null){
                    if(performedAction.first.x == action.first.x && performedAction.first.y == action.first.y){
                        toBeDeleted.add(performedAction);
                    }
                }
            }
        }

        reducedActions.removeAll(toBeDeleted);
        toBeDeleted = null;
        return reducedActions;
    }

    private void calcActions(){
        double counter = 0.0;
        for (GeneratedLevel.SpritePointData position : level.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites){
                actions.add(new Pair<GeneratedLevel.SpritePointData, String>(position, sprite));
            }
        }
        possiblePositions = counter;
    }


    public boolean isLeaf(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> possibleActions) {
        return (possibleActions.size()/allSprites.size() < possiblePositions * (SharedData.MAX_COVER_PERCENTAGE));
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }

    public TreeNode[] getChildren() {
        return children;
    }

    public void setChildren(TreeNode[] children) {
        this.children = children;
    }

}
