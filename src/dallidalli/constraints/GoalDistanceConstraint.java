package dallidalli.constraints;

import tools.Pair;

import java.awt.*;
import java.util.ArrayList;

/**
 * Compare closest goal position to the position of the avatar. This value is compared to the maximum theoretical distance.
 */
public class GoalDistanceConstraint extends AbstractConstraint{

    public int width;
    public int height;
    public Pair<Integer, Integer> avatarPosition;
    public ArrayList<Pair<Integer, Integer>> listOfGoals;

    @Override
    public double checkConstraint() {
        if(avatarPosition.first == -1 && avatarPosition.second == -1){
            return 0;
        }


        double maxDistance = Math.max(width-2, height-2);

        if(listOfGoals.size() == 0){
            return 0;
        }

        double smallestDistance = Double.MAX_VALUE;
        for (int i = 0; i < listOfGoals.size(); i++){
            double tmp = new Point(avatarPosition.first, avatarPosition.second).distance(new Point(listOfGoals.get(i).first, listOfGoals.get(i).second));
            if (tmp < smallestDistance){
                smallestDistance = tmp;
            }
        }

        return Math.min(smallestDistance/maxDistance, 1);
    }
}
