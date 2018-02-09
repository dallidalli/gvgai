package tracks.levelGeneration.constraints;

import core.game.GameDescription;

import java.util.ArrayList;
import java.util.HashMap;

public class EndsInitiallyConstraint extends AbstractConstraint {

    public ArrayList<GameDescription.TerminationData> terminationConditions;

    public HashMap<String, Integer> spriteOccurrences;

    @Override
    public double checkConstraint() {
        double totalValue = 0;
        double numberOfConditions = terminationConditions.size();

        for (GameDescription.TerminationData td:terminationConditions) {
            double subTotal = 0;
            double numSubCondition = 0;

            if(td.type.equals("SpriteCounter")){
                for (String sprite:td.sprites) {
                    numSubCondition++;

                    if (spriteOccurrences.get(sprite) > td.limit){
                        subTotal++;
                    }

                }
            }

            if(td.type.equals("SpriteCounterMore")){
                for (String sprite:td.sprites) {
                    numSubCondition++;

                    if (spriteOccurrences.get(sprite) < td.limit){
                        subTotal++;
                    }
                }
            }

            if(td.type.equals("MultiSpriteCounter")){
                numSubCondition++;
                int sum = 0;
                for (String sprite:td.sprites) {
                    sum = sum + spriteOccurrences.get(sprite);
                }
                if(sum != td.limit){
                    subTotal++;
                }
            }

            if(td.type.equals("MultiSpriteCounterSubTypes")){
                numSubCondition++;
                int sum = 0;
                for (String sprite:td.sprites) {
                    sum = sum + spriteOccurrences.get(sprite);
                }
                if(sum != td.limit){
                    subTotal++;
                }
            }

            if(td.type.equals("StopCounter")){
                int sum = 0;
                numSubCondition++;
                for (String sprite:td.sprites) {
                    sum = sum + spriteOccurrences.get(sprite);
                }
                if (sum == td.limit){
                    subTotal++;
                }
            }

            if(td.type.equals("TimeOut")){

            }

            totalValue = totalValue + (subTotal/numSubCondition);
        }

        return (totalValue/numberOfConditions);
    }
}
