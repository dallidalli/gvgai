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
	public int width;
	public int height;
	
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

			double spawnNumber = gameAnalyzer.checkIfSpawned(n.getKey());

			if(spawnNumber == 0){
				if(n.getValue() > 0){
					return 0;
				}
			} else {
				totalNum += 1;

				if(gameAnalyzer.getAvatarSprites().contains(n.getKey())){
					if(n.getValue() == 1){
						achievedNum += 1;
					}
				} else if(gameAnalyzer.getSolidSprites().contains(n.getKey())){
					if(n.getValue()-(width*2 + (height-2)*2) >= spawnNumber){
						achievedNum += 1;
					}
				} else if(n.getValue() >= spawnNumber){
					achievedNum += 1;
				}
			}

		}
		
		return achievedNum / totalNum;
	}

}
