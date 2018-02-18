package tracks.levelGeneration.constraints;

import tools.Pair;

import java.util.ArrayList;

public class SpaceAroundAvatarConstraint extends AbstractConstraint {

    public Pair<Integer, Integer> avatarPosition;
    public ArrayList<String>[][] level;

    @Override
    public double checkConstraint() {
        ArrayList<Pair<Integer, Integer>> next = new ArrayList<Pair<Integer, Integer>>();
        next.addAll(getNeighbors(avatarPosition, next));

        ArrayList<Pair<Integer, Integer>> tmp = new ArrayList<Pair<Integer, Integer>>();
        for (int i = 0; i < next.size(); i++){
            tmp.addAll(getNeighbors(next.get(i), next));
        }
        next.addAll(tmp);

        ArrayList<Pair<Integer, Integer>> tmp2 = new ArrayList<Pair<Integer, Integer>>();
        for (int i = 0; i < tmp.size(); i++){
            tmp2.addAll(getNeighbors(tmp.get(i), next));
        }
        next.addAll(tmp2);

        int countPos = 0;
        int freePos = 0;
        while(next.size() > 0){
            countPos++;
            Pair<Integer, Integer> cur = next.remove(0);
            if(level[cur.second][cur.first].isEmpty()){
                freePos++;
            }

        }

        return (freePos/countPos);
    }


    private ArrayList<Pair<Integer, Integer>> getNeighbors(Pair<Integer, Integer> position, ArrayList<Pair<Integer, Integer>> next) {
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
                    if(!next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x1, y1));
                    }

            }
        }
        if(x2 < level[y].length && x2 >= 0 && y2 < level.length && y2 >= 0){
            if(level[y2][x2].size() > 0){
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x2,y2);
                    if(!next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x2, y2));
                    }

            }
        }
        if(x3 < level[y].length && x3 >= 0 && y3 < level.length && y3 >= 0){
            if(level[y3][x3].size() > 0){
                Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x3,y3);
                    if(!next.contains(newPosition)){
                        result.add(new Pair<Integer, Integer>(x3, y3));
                    }

            }
        }
        if(x4 < level[y].length && x4 >= 0 && y4 < level.length && y4 >= 0){
            Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x4,y4);
                if(!next.contains(newPosition)){
                    result.add(new Pair<Integer, Integer>(x4, y4));
                }

        }

        return result;
    }
}
