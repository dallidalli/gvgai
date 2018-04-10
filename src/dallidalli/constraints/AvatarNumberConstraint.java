package dallidalli.constraints;

import java.util.ArrayList;
import java.util.HashMap;

public class AvatarNumberConstraint extends AbstractConstraint{

	/**
	 * hashmap contains the number of objects for each type
	 */
	public HashMap<String, Integer> spriteOccurrences;
	/**
	 * Object for game analyzer
	 */
	public ArrayList<String> avatarSpritesIn;
	
	/**
	 * Check if there is only 1 avatar in the level
	 * @return	1 if constraint is staisfied and 0 otherwise
	 */
	@Override
	public double checkConstraint() {
		int totalAvatars = 0;
		for(String avatar:avatarSpritesIn){
			if(spriteOccurrences.containsKey(avatar)){
				totalAvatars += spriteOccurrences.get(avatar);
			}
		}
		
		return totalAvatars == 1? 1:0;
	}

}
