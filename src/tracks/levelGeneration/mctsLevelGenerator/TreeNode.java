package tracks.levelGeneration.mctsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.*;

public class TreeNode{

    private TreeNode[] children;
    private double nVisits, totValue;
    private Pair<GeneratedLevel.SpritePointData, String> selectedAction;

    public TreeNode(){

    }

    public TreeNode(Pair<GeneratedLevel.SpritePointData, String> performedAction){
        selectedAction = performedAction;
    }

    public void expand(int size) {
        if (this.children == null) {
            this.children = new TreeNode[size];
        }
       }

    public void updateStats(double value) {
        this.nVisits++;
        this.totValue += value;
    }

    public void addChild(int index, TreeNode child){
        children[index] = child;
    }

    public Pair<GeneratedLevel.SpritePointData, String> getSelectedAction() {
        return selectedAction;
    }

    public boolean isLeaf() {
        return this.children == null;
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

}
