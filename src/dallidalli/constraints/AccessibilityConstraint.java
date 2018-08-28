package dallidalli.constraints;

import tools.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Constraint to represent if all tiles are reachable from the position of the avatar
 */
public class AccessibilityConstraint extends AbstractConstraint {

    public Pair<Integer, Integer> avatarPosition;
    public ArrayList<String>[][] level;
    public HashMap<String, Integer> spriteOccurrences;
    public ArrayList<String> solidSprites;

    @Override
    public double checkConstraint() {
        if(avatarPosition.first == -1 && avatarPosition.second == -1){
            return 0;
        }

        double levelSize = level.length * level[0].length;
        double placedSolids = 0;

        for(int i = 0; i < solidSprites.size(); i++){
            placedSolids = placedSolids + spriteOccurrences.get(solidSprites.get(i));
        }

        int result = floodfill(avatarPosition.first, avatarPosition.second);

        double availableRegion = levelSize-placedSolids;
        double diff = availableRegion - result;

        if(diff < 0){
            return 0;
        } else if (diff > 0){
            return 0;
        } else {
            return 1;
        }

    }

    private int floodfill(int x, int y){
        int result = 0;

        Queue<Point> queue = new LinkedList<Point>();
        Queue<Point> visited = new LinkedList<Point>();
        queue.add(new Point(x,y));

        while (!queue.isEmpty()) {
            Point p = queue.remove();
            visited.add(p);
            result++;

            ArrayList<Point> points = new ArrayList<>();

            points.add(new Point(p.x,p.y - 1));
            points.add(new Point(p.x,p.y + 1));
            points.add(new Point(p.x - 1,p.y));
            points.add(new Point(p.x + 1,p.y));

            for (Point tmpP:points) {

                if((tmpP.x >= 0 && tmpP.x < level[0].length && tmpP.y >= 0 && tmpP.y < level.length) && !queue.contains(tmpP) && !visited.contains(tmpP) && (level[tmpP.y][tmpP.x].isEmpty() || !solidSprites.contains(level[tmpP.y][tmpP.x].get(0)))){
                    queue.add(tmpP);
                }
            }
        }
        return result;
    }
}
