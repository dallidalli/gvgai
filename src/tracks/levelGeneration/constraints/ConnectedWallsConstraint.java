package tracks.levelGeneration.constraints;

import java.util.ArrayList;

public class ConnectedWallsConstraint extends AbstractConstraint {

    public ArrayList<String>[][] level;

    public String solidSprite;

    @Override
    public double checkConstraint() {
        if(solidSprite.equals("non-existent")){
            return 1;
        }


        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                if(level[y][x].size() > 0){
                    if(level[y][x].get(0).equals(solidSprite)){
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
                                if(level[y1][x1].get(0).equals(solidSprite)){
                                    counter++;
                                }}
                        }
                        if(x2 < level[y].length && x2 >= 0 && y2 < level.length && y2 >= 0){
                            if(level[y2][x2].size() > 0){
                                if(level[y2][x2].get(0).equals(solidSprite)){
                                    counter++;
                                }}
                        }
                        if(x3 < level[y].length && x3 >= 0 && y3 < level.length && y3 >= 0){
                            if(level[y3][x3].size() > 0){
                                if(level[y3][x3].get(0).equals(solidSprite)){
                                    counter++;
                                }}
                        }
                        if(x4 < level[y].length && x4 >= 0 && y4 < level.length && y4 >= 0){
                            if(level[y4][x4].size() > 0){
                                if(level[y4][x4].get(0).equals(solidSprite)){
                                    counter++;
                                }}
                        }

                        if(counter < 1){
                            return 0;
                        }
                    }
                }
            }
        }

        return 1;
    }
}
