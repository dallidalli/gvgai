package tracks.levelGeneration.constraints;

import java.util.HashMap;
import java.util.Map.Entry;

import tools.GameAnalyzer;

public class SpriteNumberConstraint extends AbstractConstraint{
	

	/**
	 * hashmap contains the number of objects for each type
	 */
	public HashMap<String, Integer> spriteOccurrences;
	/**
	 * Object for game analyzer
	 */
	public GameAnalyzer gameAnalyzer;
	
	/**
	 * 
	 * @return	1 if all objects appears at least once and 
	 * 			percentage of different objects in the level otherwise
	 */
	@Override
	public double checkConstraint() {
		double totalNum = 0;
		double achievedNum = 0;
		
		for(Entry<String, Integer> n: spriteOccurrences.entrySet()){
			if(gameAnalyzer.checkIfSpawned(n.getKey()) > 0){
				totalNum += 1;
				if(n.getValue() > 0){
					achievedNum += 1;
				}
			}
		}
		
		return achievedNum / totalNum;
	}

}
