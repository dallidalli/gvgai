package tracks.levelGeneration.mctsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.*;

public class TreeNode{

    public static ArrayList<String> allSprites = new ArrayList<String>();
    public static double possiblePositions = 0;
    public static LinkedList<TreeNode> visited = new LinkedList<TreeNode>();
    public HashMap<GeneratedLevel, Double> previousEvaluations = new HashMap<GeneratedLevel, Double>();
    public HashMap<GeneratedLevel, Double> previousSoftConstraints = new HashMap<GeneratedLevel, Double>();
    public HashMap<GeneratedLevel, Double> previousHardConstraints = new HashMap<GeneratedLevel, Double>();


    private TreeNode[] children;

    private double nVisits, totValue;
    private Pair<GeneratedLevel.SpritePointData, String> selectedAction;
    public static ArrayList<Pair<GeneratedLevel.SpritePointData, String>> actions = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();
    public static ArrayList<Pair<GeneratedLevel.SpritePointData, String>> workedActions = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();

    public static GeneratedLevel currentLevel;
    public static ArrayList<Pair<GeneratedLevel.SpritePointData, String>> currentSeq = new ArrayList<Pair<GeneratedLevel.SpritePointData, String>>();

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

    public TreeNode(Pair<GeneratedLevel.SpritePointData, String> performedAction){
        selectedAction = performedAction;
    }

    public void selectAction() {
        workedActions = (ArrayList<Pair<GeneratedLevel.SpritePointData, String>>) actions.clone();
        visited.clear();
        currentSeq.clear();

        TreeNode cur = this;
        visited.add(cur);
        while (!cur.isLeaf()) {
            cur = cur.select(nVisits);
            visited.add(cur);
            currentSeq.add(cur.selectedAction);
        }
        cur.expand(workedActions.size());
        TreeNode newNode = cur.select(nVisits);
        double value = rollOut(newNode);
        for (TreeNode node : visited) {
            // would need extra logic for n-player game
            node.updateStats(value);
        }
    }

    private void expand(int size) {
        if (children == null) {
            children = new TreeNode[size];
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
            children[selected] = new TreeNode(workedActions.get(selected));
        }

        workedActions = customActionsSingle(workedActions, workedActions.get(selected));

        return children[selected];
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
        currentSeq.clear();
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
            currentSeq.add(cur.selectedAction);
        }

        return cur;
    }

    public boolean isLeaf() {
        return children == null;
    }

    private boolean isTerminal(double fitness) {
        double currentCoverage = (possiblePositions - (workedActions.size() / allSprites.size())) / possiblePositions;

        return ((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) || (fitness >= 1));
    }


    public double rollOut(TreeNode tn) {

        TreeNode cur = tn;
        visited.add(cur);
        currentSeq.add(cur.selectedAction);


        getLevel(currentSeq, false);
        currentLevel.calculateSoftConstraints(false);
        double softConstraint = currentLevel.getConstrainFitness();
        resetLevel(currentSeq);

        if (!isTerminal(softConstraint)){
            if (cur.children == null) {
                cur.children = new TreeNode[workedActions.size()];
            }
            int selectedChild = SharedData.random.nextInt(workedActions.size());

            if (cur.children[selectedChild] == null){
                cur.children[selectedChild] = new TreeNode(workedActions.get(selectedChild));
            }

            workedActions = customActionsSingle(workedActions, workedActions.get(selectedChild));
            return rollOut(cur.children[selectedChild]);
        } else {
            return softConstraint;
        }
    }

    public GeneratedLevel getLevel(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> prev, boolean verbose) {
        for (int i = 0; i < prev.size(); i++) {
            if (prev.get(i) != null) {
                currentLevel.setPosition(prev.get(i).first, prev.get(i).second);
            }
        }

        if (verbose) {
            currentLevel.calculateSoftConstraints(true);
            System.out.println(currentLevel.getConstrainFitness());
        }

        return currentLevel;
    }

    public void resetLevel(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> prev) {
        for (int i = 0; i < prev.size(); i++) {
            currentLevel.clearPosition(prev.get(i).first);
        }

        currentLevel.resetCalculated();
    }

    private Double getEvalValue(ArrayList<Pair<GeneratedLevel.SpritePointData, String>> seq) {
        getLevel(seq, false);
        double value = 0;
        currentLevel.calculateSoftConstraints(false);
        value = currentLevel.getConstrainFitness();
        resetLevel(seq);
        return value;
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
