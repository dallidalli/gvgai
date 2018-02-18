package tracks.levelGeneration.constraints;

import tools.Pair;

import java.awt.*;
import java.util.ArrayList;

public class GoalDistanceConstraint extends AbstractConstraint{

    public int width;
    public int height;
    public Pair<Integer, Integer> avatarPosition;
    public ArrayList<Pair<Integer, Integer>> listOfGoals;

    @Override
    public double checkConstraint() {
        double maxDistance = new Point(0,0).distance(new Point(width-1, height-1));

        if(listOfGoals.size() == 0){
            return 0;
        }

        double measuredDistance = 0;
        for (int i = 0; i < listOfGoals.size(); i++){
            measuredDistance = measuredDistance + new Point(avatarPosition.first, avatarPosition.second).distance(new Point(listOfGoals.get(i).first, listOfGoals.get(i).second));
        }

        measuredDistance = (measuredDistance / listOfGoals.size());


        return measuredDistance/maxDistance;
    }
}
