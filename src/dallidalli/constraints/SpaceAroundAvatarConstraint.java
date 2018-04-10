package dallidalli.constraints;

import tools.Pair;

import java.util.ArrayList;

public class SpaceAroundAvatarConstraint extends AbstractConstraint {

    public Pair<Integer, Integer> avatarPosition;
    public ArrayList<String>[][] level;

    @Override
    public double checkConstraint() {
        if (avatarPosition.first == -1 && avatarPosition.second == -1) {
            return 0;
        }


        ArrayList<Pair<Integer, Integer>> next = new ArrayList<Pair<Integer, Integer>>();
        next.addAll(getNeighbors(avatarPosition, next));



        ArrayList<Pair<Integer, Integer>> tmp = new ArrayList<Pair<Integer, Integer>>();
        tmp.addAll(next);
        while(next.size() > 0){
            tmp.addAll(getNeighbors(next.remove(0), tmp));
        }

        next.addAll(tmp);

        /*
        ArrayList<Pair<Integer, Integer>> tmp2 = new ArrayList<Pair<Integer, Integer>>();
        for (int i = 0; i < tmp.size(); i++) {
            tmp2.addAll(getNeighbors(tmp.get(i), next));
        }
        next.addAll(tmp2);
        */

        double countPos = 0;
        double freePos = 0;
        while (next.size() > 0) {
            countPos++;
            Pair<Integer, Integer> cur = next.remove(0);
            if (level[cur.second][cur.first].isEmpty()) {
                freePos++;
            }

        }


        return (freePos / countPos);
    }


    private ArrayList<Pair<Integer, Integer>> getNeighbors(Pair<Integer, Integer> position, ArrayList<Pair<Integer, Integer>> next) {
        int x = position.first;
        int y = position.second;

        ArrayList<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();


        int x1 = x;
        int y1 = y - 1;

        int x2 = x;
        int y2 = y + 1;

        int x3 = x + 1;
        int y3 = y;

        int x4 = x - 1;
        int y4 = y;



        if (x1 < level[y].length && x1 >= 0 && y1 < level.length && y1 >= 0) {
            Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x1, y1);

            boolean duplicate = false;

            if(!duplicate){
                for(int i = 0; i < next.size(); i++){
                    if(next.get(i).first == newPosition.first && next.get(i).second == newPosition.second){
                        duplicate = true;
                        break;
                    }
                }
            }

            if(!duplicate){
                for(int i = 0; i < result.size(); i++){
                    if(result.get(i).first == newPosition.first && result.get(i).second == newPosition.second){
                        duplicate = true;
                        break;
                    }
                }
            }


            if (!duplicate) {
                result.add(newPosition);
            }

        }
        if (x2 < level[y].length && x2 >= 0 && y2 < level.length && y2 >= 0) {
            Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x2, y2);
            boolean duplicate = false;

            if(!duplicate){
                for(int i = 0; i < next.size(); i++){
                    if(next.get(i).first.intValue() == newPosition.first.intValue() && next.get(i).second.intValue() == newPosition.second.intValue()){
                        duplicate = true;
                        break;
                    }
                }
            }

            if(!duplicate){
                for(int i = 0; i < result.size(); i++){
                    if(result.get(i).first.intValue() == newPosition.first.intValue() && result.get(i).second.intValue() == newPosition.second.intValue()){
                        duplicate = true;
                        break;
                    }
                }
            }


            if (!duplicate) {
                result.add(newPosition);
            }
        }
        if (x3 < level[y].length && x3 >= 0 && y3 < level.length && y3 >= 0) {
            Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x3, y3);
            boolean duplicate = false;

            if(!duplicate){
                for(int i = 0; i < next.size(); i++){
                    if(next.get(i).first.intValue() == newPosition.first.intValue() && next.get(i).second.intValue() == newPosition.second.intValue()){
                        duplicate = true;
                        break;
                    }
                }
            }

            if(!duplicate){
                for(int i = 0; i < result.size(); i++){
                    if(result.get(i).first.intValue() == newPosition.first.intValue() && result.get(i).second.intValue() == newPosition.second.intValue()){
                        duplicate = true;
                        break;
                    }
                }
            }


            if (!duplicate) {
                result.add(newPosition);
            }
        }
        if (x4 < level[y].length && x4 >= 0 && y4 < level.length && y4 >= 0) {
            Pair<Integer, Integer> newPosition = new Pair<Integer, Integer>(x4, y4);
            boolean duplicate = false;

            if(!duplicate){
                for(int i = 0; i < next.size(); i++){
                    if(next.get(i).first.intValue() == newPosition.first.intValue() && next.get(i).second.intValue() == newPosition.second.intValue()){
                        duplicate = true;
                        break;
                    }
                }
            }

            if(!duplicate){
                for(int i = 0; i < result.size(); i++){
                    if(result.get(i).first.intValue() == newPosition.first.intValue() && result.get(i).second.intValue() == newPosition.second.intValue()){
                        duplicate = true;
                        break;
                    }
                }
            }


            if (!duplicate) {
                result.add(newPosition);
            }
        }

        return result;
    }
}
