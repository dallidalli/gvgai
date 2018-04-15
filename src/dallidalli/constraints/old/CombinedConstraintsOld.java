package dallidalli.constraints.old;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class CombinedConstraintsOld extends OldAbstractConstraint {

	
	/**
	 * array of all constraints need to be checked
	 */
	private ArrayList<OldAbstractConstraint> constraints;
	
	/**
	 * 
	 */
	public CombinedConstraintsOld(){
		constraints = new ArrayList<OldAbstractConstraint>();
	}
	

	/**
	 * Add multiple constraints to the combined constraints class
	 * @param conStrings	array of name of the constraint classes needed
	 */
	@SuppressWarnings("unchecked")
	public void addConstraints(String[] conStrings){
		for(String c:conStrings){
			try{
				Class constrainClass = Class.forName("dallidalli.constraints.old" + c);
				Constructor constrainConstructor = constrainClass.getConstructor();
				OldAbstractConstraint constraint = (OldAbstractConstraint) constrainConstructor.newInstance();
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
		for(OldAbstractConstraint c:constraints){
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
		for(OldAbstractConstraint c:constraints){
			score += c.checkConstraint();
		}
		return score / constraints.size();
	}

}
