package tracks.levelGeneration.mctsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.*;

public class TreeNode{

    public static ArrayList<String> allSprites = new ArrayList<String>();
    public static double possiblePositions = 0;
    public static LinkedList<TreeNode> visited = new LinkedList<TreeNode>();
    public static HashMap<GeneratedLevel, Double> previousEvaluations = new HashMap<GeneratedLevel, Double>();
    public static HashMap<GeneratedLevel, Double> previousSoftConstraints = new HashMap<GeneratedLevel, Double>();
    public static HashMap<GeneratedLevel, Double> previousHardConstraints = new HashMap<GeneratedLevel, Double>();


    private TreeNode[] children;

    private double nVisits, totValue;
    private ArrayList<Pair<GeneratedLevel.SpritePointData, String>> actions = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();

    private GeneratedLevel currentLevel;

    public TreeNode(int width, int height, boolean empty) {
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
            tmp.add(sprite.name);
        }
        allSprites.clear();
        allSprites.addAll(tmp);

        calcActions();

        System.out.println("number of actions: " + actions.size());
    }

    public TreeNode(GeneratedLevel parentLevel, ArrayList<Pair<GeneratedLevel.SpritePointData, String>> possibleActions, Pair<GeneratedLevel.SpritePointData, String> performedAction){
        actions = possibleActions;
        currentLevel = parentLevel.clone();
        currentLevel.setPosition(performedAction.first,performedAction.second);
        //System.out.println("number of actions: " + actions.size());
    }

    public void selectAction() {
        visited.clear();
        TreeNode cur = this;
        visited.add(cur);
        while (!cur.isLeaf()) {
            cur = cur.select(nVisits);
            visited.add(cur);
        }
        cur.expand();
        TreeNode newNode = cur.select(nVisits);
        //visited.add(newNode);
        double value = rollOut(newNode);
        for (TreeNode node : visited) {
            // would need extra logic for n-player game
            node.updateStats(value);
        }
    }

    private void expand() {
        if (children == null) {
            children = new TreeNode[actions.size()];
        }
       }

    private TreeNode select(double totalVisits) {
        int selected = 0;
        double bestValue = Double.MIN_VALUE;
        for (int i = 0; i < children.length; i++) {
            TreeNode c = children[i];
            double totValueChild;
            double nVisitsChild;

            if (c != null){
                totValueChild = c.totValue;
                nVisitsChild = c.nVisits;
            } else {
                totValueChild = 0;
                nVisitsChild = 0;
            }

            double uctValue = totValueChild / (nVisitsChild + SharedData.EIPSLON) + Math.sqrt(2)*Math.sqrt(Math.log(totalVisits+1) / (nVisitsChild + SharedData.EIPSLON)) + SharedData.random.nextDouble() * SharedData.EIPSLON;

            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValue) {
                    selected = i;
                    bestValue = uctValue;
            }
        }

        if (children[selected] == null){
            ArrayList<Pair<GeneratedLevel.SpritePointData, String>> newActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>)(actions.clone());
            ArrayList<Pair<GeneratedLevel.SpritePointData, String>> toBeDeleted = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();
            for (Pair<GeneratedLevel.SpritePointData, String> action:newActions) {
                if(action.first.x == newActions.get(selected).first.x && action.first.y == newActions.get(selected).first.y){
                    toBeDeleted.add(action);
                }
            }
            newActions.removeAll(toBeDeleted);
            children[selected] = new TreeNode(currentLevel, newActions, actions.get(selected));
        }

        return children[selected];
    }

    private void calcActions(){
        double counter = 0.0;
        for (GeneratedLevel.SpritePointData position : currentLevel.getFreePositions(allSprites)) {
            counter++;
            for (String sprite : allSprites){
                actions.add(new Pair<GeneratedLevel.SpritePointData, String>(position, sprite));
            }
        }
        possiblePositions = counter;
    }

    public TreeNode getBest(){
        TreeNode cur = this;

        while(!cur.isLeaf()){
            int selected = 0;
            double bestValue = Double.MIN_VALUE;
            for (int i = 0; i < cur.children.length; i++) {
                TreeNode c = cur.children[i];
                double totValueChild;
                double nVisitsChild;

                if (c != null){
                    totValueChild = c.totValue;
                    nVisitsChild = c.nVisits;
                } else {
                    totValueChild = 0;
                    nVisitsChild = 0;
                }

                double value = totValueChild / (nVisitsChild + SharedData.EIPSLON);

                if (value > bestValue) {
                    selected = i;
                    bestValue = value;
                }
            }
            cur = cur.children[selected];
        }

        return cur;
    }

    public boolean isLeaf() {
        return children == null;
    }

    private boolean isTerminal(double fitness) {
        double currentCoverage = (possiblePositions - (actions.size() / allSprites.size())) / possiblePositions;

        return ((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) || (fitness >= 1));
    }


    public double rollOut(TreeNode tn) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
        TreeNode cur = tn;
        visited.add(cur);
        //System.out.println("number of actions: " + cur.actions.size() + " " + cur.getCurrentLevel());
        /*double currentCoverage = cur.currentLevel.getCoverPercentage();
        double continueProbability = 1;

        if(currentCoverage >= SharedData.MIN_COVER_PERCENTAGE && currentCoverage <= SharedData.MAX_COVER_PERCENTAGE){
            continueProbability = SharedData.random.nextDouble();
        }

        if(Math.abs(currentCoverage - SharedData.MAX_COVER_PERCENTAGE) < 0.05){
            continueProbability = 0;
        }


        if(continueProbability > 1){*/

        double softConstraint = 0;

        if(previousSoftConstraints.containsKey(cur.currentLevel)){
            softConstraint = previousSoftConstraints.get(cur.currentLevel);
        } else {
            cur.currentLevel.calculateSoftConstraints();
            softConstraint = cur.currentLevel.getConstrainFitness();
            previousSoftConstraints.put(cur.currentLevel, softConstraint);
        }

        double amountSprites = allSprites.size();
        double maxPositions = possiblePositions;


        if (!cur.isTerminal(softConstraint)){
            if (cur.children == null) {
                cur.children = new TreeNode[cur.actions.size()];
            }
            int selectedChild = SharedData.random.nextInt(cur.actions.size());

            if (cur.children[selectedChild] == null){
                ArrayList<Pair<GeneratedLevel.SpritePointData, String>> newActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>)(cur.actions.clone());
                ArrayList<Pair<GeneratedLevel.SpritePointData, String>> toBeDeleted = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();
                for (Pair<GeneratedLevel.SpritePointData, String> action:newActions) {
                    if(action.first.x == newActions.get(selectedChild).first.x && action.first.y == newActions.get(selectedChild).first.y){
                        toBeDeleted.add(action);
                    }
                }
                newActions.removeAll(toBeDeleted);
                cur.children[selectedChild] = new TreeNode(cur.currentLevel, newActions, actions.get(selectedChild));
            }

            return rollOut(cur.children[selectedChild]);
        } else {
            return softConstraint;
        }
    }

    public void updateStats(double value) {
        nVisits++;
        totValue += value;
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

    public double getnVisits() {
        return nVisits;
    }

    public void setnVisits(double nVisits) {
        this.nVisits = nVisits;
    }

    public double getTotValue() {
        return totValue;
    }

    public void setTotValue(double totValue) {
        this.totValue = totValue;
    }

    public GeneratedLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(GeneratedLevel currentLevel) {
        this.currentLevel = currentLevel;
    }
}
