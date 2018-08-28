package dallidalli.constraints;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class CombinedConstraints extends AbstractConstraint{

	
	/**
	 * array of all constraints need to be checked
	 */
	private ArrayList<AbstractConstraint> constraints;
	private ArrayList<Double> weights;
	/**
	 * 
	 */
	public CombinedConstraints(){
		constraints = new ArrayList<AbstractConstraint>();
	}
	

	/**
	 * Add multiple constraints to the combined constraints class
	 * @param conStrings	array of name of the constraint classes needed
	 */
	@SuppressWarnings("unchecked")
	public void addConstraints(String[] conStrings){
		for(String c:conStrings){
			try{
				Class constrainClass = Class.forName("dallidalli.constraints." + c);
				Constructor constrainConstructor = constrainClass.getConstructor();
				AbstractConstraint constraint = (AbstractConstraint) constrainConstructor.newInstance();
				constraints.add(constraint);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * Set the parameters of all the constraints added
	 * @param parameters	a hashmap contains all the objects needed for all constraints
	 */
	@Override
	public void setParameters(HashMap<String, Object> parameters) {
		for(AbstractConstraint c:constraints){
			c.setParameters(parameters);
		}
	}
	

	/**
	 * Check if all constraints are satisfied
	 * @return	return a percentage of how many constraints are satisfied
	 */
	@Override
	public double checkConstraint() {
		double score = 0;
		double weightsSum = 0;



		for(int i = 0; i < constraints.size(); i++){
			if(weights.get(i) > 0){
				double tmpScore = (weights.get(i) * constraints.get(i).checkConstraint());
				//System.out.println(i + " " + tmpScore);
				score += tmpScore;
				weightsSum += weights.get(i);
			}

		}

		return score / weightsSum;
	}

	public void listConstraints() {
		for(AbstractConstraint c:constraints){
			System.out.println(c.getClass() + ": "+ c.checkConstraint());
		}
	}

	public void setWeights(ArrayList<Double> weights){
		this.weights = weights;
	}
}
