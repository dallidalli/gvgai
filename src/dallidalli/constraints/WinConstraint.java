package dallidalli.constraints;

import ontology.Types;
import ontology.Types.WINNER;

public class WinConstraint extends AbstractConstraint {

	/**
	 * the winning state of the player
	 */
	public Types.WINNER bestPlayer;
	public double DRAW_FITNESS = 0.5;
	
	/**
	 * check if the player win the game
	 * @return 	1 if the automated player wins and 0 otherwise
	 */
	@Override
	public double checkConstraint() {
		double result = 0;
		if(bestPlayer == WINNER.PLAYER_WINS){
			result += 1;
		}
		if(bestPlayer == WINNER.NO_WINNER){
			result += DRAW_FITNESS;
		}
		return result;
	}
}
