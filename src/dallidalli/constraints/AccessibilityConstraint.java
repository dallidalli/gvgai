package dallidalli.constraints;

import tools.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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
/*
        ArrayList<Pair<Integer, Integer>> visited = new ArrayList<Pair<Integer, Integer>>();
        ArrayList<Pair<Integer, Integer>> next = new ArrayList<Pair<Integer, Integer>>();
        next.addAll(getNeighbors(avatarPosition, visited, next));
        while(!next.isEmpty()){
            next.addAll(getNeighbors(next.get(0), visited, next));
            visited.add(next.remove(0));
        }
*/
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
            //return result/availableRegion;
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

        while (!queue.isEmpty())
        {
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

    private ArrayList<Pair<Integer, Integer>> getNeighbors(Pair<Integer, Integer> position, ArrayList<Pair<Integer, Integer>> visited, ArrayList<Pair<Integer, Integer>> next) {
        int x = position.first;
        int y = position.second;

        ArrayList<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();


        int x1 = x;
        int y1 = y-1;

        int x2 = x;
        int y2 = y+1;

        int x3 = x+1;
        int y3 = y;

        int x4 = x-1;
        int y4 = y;

        int counter= 0;

        if(x1 < level[y].length && x1 >= 0 && y1 < level.length && y1 >= 0){
            if(level[y1][x1].size() > 0){
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x1,y1);
                for (int i = 0; i < solidSprites.size(); i++){
                    if(!level[y1][x1].contains(solidSprites.get(i)) && !visited.contains(newPosition) && !next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x1, y1));
                    }
                }
            } else {
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x1,y1);
                if(!visited.contains(newPosition) && !next.contains(newPosition)){
                    result.add(new Pair<Integer, Integer>(x1, y1));
                }
            }
        }
        if(x2 < level[y].length && x2 >= 0 && y2 < level.length && y2 >= 0){
            if(level[y2][x2].size() > 0){
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x2,y2);
                for (int i = 0; i < solidSprites.size(); i++){
                    if(!level[y2][x2].contains(solidSprites.get(i)) && !visited.contains(newPosition) && !next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x2, y2));
                    }
                }
            }else {
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x2,y2);
                if(!visited.contains(newPosition) && !next.contains(newPosition)){
                    result.add(new Pair<Integer, Integer>(x2, y2));
                }
            }
        }
        if(x3 < level[y].length && x3 >= 0 && y3 < level.length && y3 >= 0){
            if(level[y3][x3].size() > 0){
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x3,y3);
                for (int i = 0; i < solidSprites.size(); i++){
                    if(!level[y3][x3].contains(solidSprites.get(i)) && !visited.contains(newPosition) && !next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x3, y3));
                    }
                }
            }else {
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x3,y3);
                if(!visited.contains(newPosition) && !next.contains(newPosition)){
                    result.add(new Pair<Integer, Integer>(x3, y3));
                }
            }
        }
        if(x4 < level[y].length && x4 >= 0 && x4 < level.length && x4 >= 0){
            if(level[x4][x4].size() > 0){
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x4,x4);
                for (int i = 0; i < solidSprites.size(); i++){
                    if(!level[x4][x4].contains(solidSprites.get(i)) && !visited.contains(newPosition) && !next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x4, x4));
                    }
                }
            }else {
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x4,x4);
                if(!visited.contains(newPosition) && !next.contains(newPosition)){
                    result.add(new Pair<Integer, Integer>(x4, x4));
                }
            }
        }

        return result;
    }
}
