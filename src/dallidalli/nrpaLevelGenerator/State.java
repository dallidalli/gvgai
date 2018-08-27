package dallidalli.nrpaLevelGenerator;

public class State {

    int[] sequence;

    public State(int[] actions){
        sequence = actions.clone();
    }

    public int[] getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object obj) {
        return sequence.equals(((State)obj).sequence);
    }

    @Override
    public int hashCode() {
        return sequence.hashCode();
    }
}
