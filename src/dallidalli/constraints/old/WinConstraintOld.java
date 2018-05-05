package dallidalli.constraints.old;

import ontology.Types.WINNER;
import dallidalli.commonClasses.SharedData;

public class WinConstraintOld extends OldAbstractConstraint {

	/**
	 * the winning state of the player
	 */
	public WINNER bestPlayer;
	
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
			result += SharedData.DRAW_FITNESS;
		}
		return result;
	}
}
