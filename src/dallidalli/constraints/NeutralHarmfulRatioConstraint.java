package dallidalli.constraints;

import core.game.GameDescription;
import tools.GameAnalyzer;

import java.util.HashMap;

/**
 * Check that neutral and harmful sprites have at least a certain ratio.
 */
public class NeutralHarmfulRatioConstraint extends AbstractConstraint {

    public HashMap<String, Integer> spriteOccurrences;
    public GameAnalyzer gameAnalyzer;
    public GameDescription description;
    public double desiredRatio;
    public String solidSprite;
    public int width;
    public int height;

    @Override
    public double checkConstraint() {
        double amountHarmful = 0;
        double amountNeutral = 0;

        if(!solidSprite.equals("non-existent")){
            amountNeutral -= width*2;
            amountNeutral -= (height-2)*2;
        }

        for (String sprite:spriteOccurrences.keySet()) {
            if(gameAnalyzer.getHarmfulSprites().contains(sprite)  || gameAnalyzer.getCollectableSprites().contains(sprite) || gameAnalyzer.getOtherSprites().contains(sprite)){
                amountHarmful += spriteOccurrences.get(sprite);
            } else if (gameAnalyzer.getSolidSprites().contains(sprite)){
                amountNeutral += spriteOccurrences.get(sprite);
            }
        }

        if(amountHarmful == 0 || amountNeutral <= 0){
            return 0;
        }

        double currentRatio = amountNeutral/(amountHarmful*desiredRatio);

        if (currentRatio >= 1){
            return 1;
        } else {
            return currentRatio;
        }
    }
}
