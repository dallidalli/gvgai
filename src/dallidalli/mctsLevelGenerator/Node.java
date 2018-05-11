package dallidalli.mctsLevelGenerator;


public class Node {

    private Node[] children = null;
    private double totalValue;
    private double totalVisits;
    private double bestValue = -1;

    public Node(){
        this.totalValue = 0;
        this.totalVisits = 0;
    }

    public void expand(int numberOfChildren){
        this.children = new Node[numberOfChildren];
        /*
        for(int i = 0; i < numberOfChildren; i++){
            this.children[i] = new Node();
        }
        */
    }

    public void reset(){
        this.children = null;
        this.totalValue = 0;
        this.totalVisits = 0;
        this.bestValue = -1;
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

        if(value > bestValue){
            bestValue = value;
        }
    }

    public int numberOfChildren(){
        int counter = 0;
        if(children != null){
            for (int i = 0; i < children.length; i++) {
                if(children[i] != null){
                    counter++;
                }
            }
        }

        return counter;
    }

    public double getBestValue() {
        return bestValue;
    }
}
