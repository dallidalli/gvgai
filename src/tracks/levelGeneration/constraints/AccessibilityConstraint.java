package tracks.levelGeneration.constraints;

import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;

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

        ArrayList<Pair<Integer, Integer>> visited = new ArrayList<Pair<Integer, Integer>>();
        ArrayList<Pair<Integer, Integer>> next = new ArrayList<Pair<Integer, Integer>>();
        next.addAll(getNeighbors(avatarPosition, visited, next));
        while(!next.isEmpty()){
            next.addAll(getNeighbors(next.get(0), visited, next));
            visited.add(next.remove(0));
        }

        double levelSize = level.length * level[0].length;
        double placedSolids = 0;

        for(int i = 0; i < solidSprites.size(); i++){
            placedSolids = placedSolids + spriteOccurrences.get(solidSprites.get(i));
        }

        double availableRegion = levelSize-placedSolids;
        double diff = availableRegion - visited.size();

        if(diff < 0){
            return 0;
        } else if (diff > 0){
            return visited.size()/availableRegion;
        } else {
            return 1;
        }

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
            }
        }
        if(x4 < level[y].length && x4 >= 0 && y4 < level.length && y4 >= 0){
            Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x4,y4);
            for (int i = 0; i < solidSprites.size(); i++){
                if(!level[y4][x4].contains(solidSprites.get(i)) && !visited.contains(newPosition) && !next.contains(newPosition)){
                    result.add(new Pair<Integer, Integer>(x4, y4));
                }
            }
        }

        return result;
    }
}
