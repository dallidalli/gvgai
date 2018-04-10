package dallidalli.constraints;

import java.util.ArrayList;

public class SymmetryConstraint extends AbstractConstraint {

    public ArrayList<String>[][] level;

    @Override
    public double checkConstraint() {
        int width = level[0].length;
        int height = level.length;
        double sum = 0;
        double counter = 0;

        for(int x = 0; x < width/2; x++){
            for(int y = 0; y < height/2; y++){
                sum += checkComplements(x,y, height, width);
                counter++;
            }
        }

        return sum/counter;
    }

    private double checkComplements(int x, int y, int height, int width) {
        String sprite1 = "";
        String sprite2 = "";
        String sprite3 = "";
        String sprite4 = "";
        double sum = 0;

        if(level[y][x].size() > 0){
            sprite1 = level[y][x].get(0);
        }

        if(level[(height-1-y)][x].size() > 0){
           sprite2 = level[(height-1-y)][x].get(0);
        }

        if(level[(height-1-y)][(width-1-x)].size() > 0){
            sprite3 = level[(height-1-y)][(width-1-x)].get(0);
        }

        if(level[y][(width-1-x)].size() > 0){
            sprite4 = level[y][(width-1-x)].get(0);
        }

        ArrayList<String> sprites = new ArrayList<>();
        sprites.add(sprite1);
        sprites.add(sprite2);
        sprites.add(sprite3);
        sprites.add(sprite4);

        double counter = 0;
        for(int i = 0; i < sprites.size(); i++){
            for(int j = 0; j < sprites.size(); j++){
                if(i != j){
                    if(sprites.get(i).equals(sprites.get(j))){
                        sum++;
                    }
                    counter++;
                }
            }
        }

        return sum/counter;

    }
}
