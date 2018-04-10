package dallidalli.mctsLevelGenerator;

public class Node {

    private Node[] children = null;
    private double totalValue;
    private double totalVisits;

    public Node(){
        this.totalValue = 0;
        this.totalVisits = 0;
    }

    public void expand(int numberOfChildren){
        this.children = new Node[numberOfChildren];
    }

    public Node[] getChildren() {
        return children;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public double getTotalVisits() {
        return totalVisits;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public void setTotalVisits(double totalVisits) {
        this.totalVisits = totalVisits;
    }

    public void setChild(int index, Node child) {
        this.children[index] = child;
    }

    public void update(double value){
        this.totalVisits += 1;
        this.totalValue += value;
    }
}
