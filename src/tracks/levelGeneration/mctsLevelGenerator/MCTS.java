package tracks.levelGeneration.mctsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;
import tracks.levelGeneration.commonClasses.*;

import java.util.*;

public class MCTS{

    public ArrayList<String> allSprites = new ArrayList<String>();
    public double possiblePositions = 0;
    public LinkedList<TreeNode> visited = new LinkedList<TreeNode>();

    public ArrayList<SpritePointData> actions = new ArrayList<SpritePointData>();
    public ArrayList<SpritePointData> workedActions = new ArrayList<SpritePointData>();

    public GeneratedLevel currentLevel;
    public ArrayList<SpritePointData> currentSeq = new ArrayList<SpritePointData>();

    public TreeNode root;

    public MultiKeyHashMap<ArrayList<SpritePointData>, SpritePointData, Pair<Double, Double>> nodes = new MultiKeyHashMap<>();

    public MCTS(int width, int height, boolean empty) {
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

        root = new TreeNode();

        System.out.println("number of actions: " + actions.size());
    }

    public void selectAction() {
        workedActions = (ArrayList<SpritePointData>) actions.clone();
        visited.clear();
        currentSeq.clear();

        TreeNode cur = root;
        visited.add(cur);
        while (!cur.isLeaf()) {
            cur = select(cur);
            visited.add(cur);
            currentSeq.add(cur.getSelectedAction());
        }
        cur.expand(workedActions.size());
        TreeNode newNode = select(cur);
        double value = rollOut(newNode);
        for (TreeNode node : visited) {
            // would need extra logic for n-player game
            node.updateStats(value);
        }
    }


    private TreeNode select(TreeNode cur) {
        int selected = 0;
        double bestValue = Double.MIN_VALUE;
        for (int i = 0; i < cur.getChildren().length; i++) {
            TreeNode c = cur.getChildren()[i];
            double totValueChild;
            double nVisitsChild;

            if (c != null){
                totValueChild = c.getTotValue();
                nVisitsChild = c.getnVisits();
            } else {
                totValueChild = 0;
                nVisitsChild = 0;
            }

            double uctValue = totValueChild / (nVisitsChild + SharedData.EIPSLON) + Math.sqrt(2)*Math.sqrt(Math.log(root.getnVisits()+1) / (nVisitsChild + SharedData.EIPSLON)) + SharedData.random.nextDouble() * SharedData.EIPSLON;

            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValue) {
                selected = i;
                bestValue = uctValue;
            }
        }

        if (cur.getChildren()[selected] == null){
            cur.addChild(selected, new TreeNode(this.workedActions.get(selected)));
        }

        this.workedActions = customActionsSingle(this.workedActions, this.workedActions.get(selected));

        return cur.getChildren()[selected];
    }

    private ArrayList<SpritePointData> customActionsSingle(ArrayList<SpritePointData> allActions, SpritePointData prev) {
        ArrayList<SpritePointData> toBeDeleted = new ArrayList<SpritePointData>();

        for (SpritePointData action : allActions) {
            if (prev.x == action.x && prev.y == action.y) {
                toBeDeleted.add(action);
            }
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

    public TreeNode getBest(){
        currentSeq.clear();
        TreeNode cur = root;

        while(!cur.isLeaf()){
            int selected = 0;
            double bestValue = Double.MIN_VALUE;
            for (int i = 0; i < cur.getChildren().length; i++) {
                TreeNode c = cur.getChildren()[i];
                double totValueChild;
                double nVisitsChild;

                if (c != null){
                    totValueChild = c.getTotValue();
                    nVisitsChild = c.getnVisits();
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
            cur = cur.getChildren()[selected];
            currentSeq.add(cur.getSelectedAction());
        }

        return cur;
    }

    private boolean isTerminal(double fitness) {
        double currentCoverage = (possiblePositions - (workedActions.size() / allSprites.size())) / possiblePositions;

        return ((currentCoverage > SharedData.MAX_COVER_PERCENTAGE) || (fitness >= 1));
    }


    public double rollOut(TreeNode tn) {

        TreeNode cur = tn;
        visited.add(cur);
        currentSeq.add(cur.getSelectedAction());


        getLevel(currentSeq, false);
        currentLevel.calculateSoftConstraints(false);
        double softConstraint = currentLevel.getConstrainFitness();
        resetLevel(currentSeq);

        if (!isTerminal(softConstraint)){
            if (cur.getChildren() == null) {
                cur.expand(workedActions.size());
            }
            int selectedChild = SharedData.random.nextInt(workedActions.size());

            if (cur.getChildren()[selectedChild] == null){
                cur.addChild(selectedChild, new TreeNode(workedActions.get(selectedChild)));
            }

            workedActions = customActionsSingle(workedActions, workedActions.get(selectedChild));
            return rollOut(cur.getChildren()[selectedChild]);
        } else {
            return softConstraint;
        }
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

    private Double getEvalValue(ArrayList<SpritePointData> seq) {
        getLevel(seq, false);
        double value = 0;
        currentLevel.calculateSoftConstraints(false);
        value = currentLevel.getConstrainFitness();
        resetLevel(seq);
        return value;
    }

    public GeneratedLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(GeneratedLevel currentLevel) {
        this.currentLevel = currentLevel;
    }
}
