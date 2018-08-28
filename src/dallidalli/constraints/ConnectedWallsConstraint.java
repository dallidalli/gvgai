package dallidalli.constraints;

import tools.Pair;

import java.util.ArrayList;

/**
 * Constraint to check if all walls are connected to each other
 */
public class ConnectedWallsConstraint extends AbstractConstraint {

    public ArrayList<String>[][] level;

    public String solidSprite;

    @Override
    public double checkConstraint() {
        if(solidSprite.equals("non-existent")){
            return 1;
        }

        double total = 0;
        double achieved = 0;

        ArrayList<Pair<Integer, Integer>> points = new ArrayList<>();

        for(int y = 1; y < level.length -1; y++) {
            for (int x = 1; x < level[y].length - 1; x++) {
                if (level[y][x].size() > 0) {
                    if (level[y][x].get(0).equals(solidSprite)) {

                        ArrayList<Pair<Integer, Integer>> subpoints = new ArrayList<>();
                        subpoints.add(new Pair<>(x,y-1));
                        subpoints.add(new Pair<>(x,y+1));
                        subpoints.add(new Pair<>(x-1,y));
                        subpoints.add(new Pair<>(x+1,y));
                        subpoints.add(new Pair<>(x-1,y-1));
                        subpoints.add(new Pair<>(x+1,y+1));
                        subpoints.add(new Pair<>(x-1,y+1));
                        subpoints.add(new Pair<>(x+1,y-1));

                        int counter = 0;
                        int counter2 = 0;


                        for (int i = 0; i < subpoints.size(); i++) {
                            Pair<Integer, Integer> point = subpoints.get(i);

                            if(level[point.second][point.first].size() > 0){
                                if(level[point.second][point.first].get(0).equals(solidSprite)){
                                    if(i < 4){
                                        counter++;
                                    } else {
                                        counter2++;
                                    }
                                }
                            }
                        }

                        if(counter == 1){
                            total++;
                            points.add(new Pair<>(x,y));
                        }

                        if(counter == 0 || (counter == 2 && (counter2 == 3 || counter2 == 2))){
                            total++;
                        }
                    }
                }
            }
        }

        if(points.size() == 0){
            return 0;
        }

        for (Pair<Integer, Integer> point:points) {
            int lastX = point.first;
            int lastY = point.second;

            int curX = point.first;
            int curY = point.second;

            boolean possible = false;

            ArrayList<Pair<Integer, Integer>> last = new ArrayList<>();
            last.add(new Pair<>(lastX, lastY));

            while (!isBorder(curX, curY)){
                possible = false;

                ArrayList<Pair<Integer, Integer>> subpoints = new ArrayList<>();
                subpoints.add(new Pair<>(curX,curY-1));
                subpoints.add(new Pair<>(curX,curY+1));
                subpoints.add(new Pair<>(curX-1,curY));
                subpoints.add(new Pair<>(curX+1,curY));

                for (int i = 0; i < subpoints.size(); i++) {
                    Pair<Integer, Integer> next = subpoints.get(i);


                    if(level[next.second][next.first].size() > 0 && !last.contains(next)){
                        if(level[next.second][next.first].get(0).equals(solidSprite)){
                            if(points.contains(next)){
                                possible = false;
                                break;
                            }

                            last.add(new Pair<>(curX, curY));

                            curX = next.first;
                            curY = next.second;

                            possible = true;
                            break;
                        }
                    }
                }

                if(!possible){
                    break;
                }
            }

            if(possible){
                achieved++;
            }
        }

        return achieved/total;
    }

    private boolean isBorder(int x, int y){
        return (x == 0 || x == level[0].length -1) || (y == 0 || y == level.length -1);
    }
}
