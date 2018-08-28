package dallidalli.nrpaLevelGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class representing a NRPA-policy
 */
public class Policy {

    private HashMap<List<Integer>, HashMap<Integer, Double>> policy = new HashMap<>();

    public Policy(int numOfActions, int numberOfSprites, int cutoff){

        List<Integer> actionIndicies = IntStream.range(0, numOfActions).boxed().collect(Collectors.toList());
        List<List<Integer>> states = new ArrayList<>();

        for (int i = 0; i <= cutoff ; i++) {
            states.addAll(combination(actionIndicies, i));
        }

        for (List<Integer> state:states) {
            boolean validState = true;

            Collections.sort(state);

            List<Integer> legalActions = new ArrayList<>(actionIndicies);
            List<Integer> toBeRemoved = new ArrayList<>();

            for (int i = 0; i < state.size(); i++) {
                if(validState){
                    int start = (int) (Math.floor(state.get(i) / numberOfSprites)*numberOfSprites);
                    int end = (start + numberOfSprites);

                    List<Integer> range = IntStream.range(start, end).boxed().collect(Collectors.toList());

                    for (int j = 0; j < state.size(); j++) {
                        if(i != j){
                            if(range.contains(state.get(j))){
                                validState = false;
                                break;
                            }
                        }
                    }

                    if (validState){
                        toBeRemoved.addAll(range);
                    }
                }else{
                    break;
                }
            }

            if (validState){
                legalActions.removeAll(toBeRemoved);
                HashMap<Integer, Double> values = new HashMap<>();

                for (int i = 0; i < legalActions.size(); i++) {
                    values.put(legalActions.get(i), 0.0);
                }

                policy.put(state, values);
            }
        }
    }

    public Policy(Policy other, boolean deep){
        if(deep){
            policy = new HashMap<List<Integer>, HashMap<Integer, Double>>();

            for (Map.Entry<List<Integer>, HashMap<Integer, Double>> original:other.policy.entrySet()) {
                policy.put(original.getKey(), new HashMap<>(original.getValue()));
            }
        }else{
            policy = new HashMap(other.policy);
        }
    }

    public void verifyState(List<Integer> state, List<Integer> actionIndicies, int numberOfSprites){
        if(!policy.containsKey(state)){
            List<Integer> legalActions = new ArrayList<>(actionIndicies);
            List<Integer> toBeRemoved = new ArrayList<>();

            for (int i = 0; i < state.size(); i++) {
                int start = (int) (Math.floor(state.get(i) / numberOfSprites)*numberOfSprites);
                int end = (start + numberOfSprites);

                List<Integer> range = IntStream.range(start, end).boxed().collect(Collectors.toList());

                toBeRemoved.addAll(range);
            }

            legalActions.removeAll(toBeRemoved);
            HashMap<Integer, Double> values = new HashMap<>();

            for (int i = 0; i < legalActions.size(); i++) {
                values.put(legalActions.get(i), 0.0);
            }

            policy.put(state, values);
        }
    }

    public void putSingleValue(List<Integer> state, Integer action, double value){
        HashMap tmp = policy.get(state);
        tmp.put(action, value);
        policy.put(state, tmp);
    }

    public void changeSingleValue(List<Integer> state, Integer action, double value){
        HashMap<Integer, Double> tmp = policy.get(state);
        tmp.put(action, (tmp.get(action)+value));
        policy.put(state, tmp);
    }

    public void putValues(List<Integer> state, List<Integer> action, List<Double> value){
        HashMap tmp = policy.get(state);
        for (int i = 0; i < action.size(); i++) {
            tmp.put(action.get(i), value.get(i));
        }

        policy.put(state, tmp);
    }

    public double getSingleValue(List<Integer> state, Integer action){
        return policy.get(state).get(action);
    }

    public List<Double> getValues(List<Integer> state, List<Integer> action){
        LinkedList<Double> values = new LinkedList<>();

        for (Integer curAction:action) {
            values.add(policy.get(state).get(curAction));
        }

        return values;
    }

    public HashMap<Integer, Double> getAllValues(List<Integer> state){
        return policy.get(state);
    }


    public static <T> List<List<T>> combination(List<T> values, int size) {

        if (0 == size) {
            return Collections.singletonList(Collections.emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<List<T>>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<T>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combination(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<T>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combination(subSet, size));

        return combination;
    }

    public int size(){
        return policy.size();
    }

    public boolean contains(ArrayList<Integer> state){
        return policy.containsKey(state);
    }
}
