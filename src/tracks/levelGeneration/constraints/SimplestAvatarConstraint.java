package tracks.levelGeneration.constraints;

import core.game.GameDescription;
import tools.GameAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;

public class SimplestAvatarConstraint extends AbstractConstraint {

    public ArrayList<String> avatarSpritesIn;
    public ArrayList<GameDescription.SpriteData> allSprites;
    public GameAnalyzer gameAnalyzer;
    public HashMap<String, Integer> spriteOccurrences;



    @Override
    public double checkConstraint() {
        double numAvatar = avatarSpritesIn.size();
        double correctAvatar = 0;
        double wrongAvatar = 0;

        for (String avatar:avatarSpritesIn) {
            if(avatar.equals(gameAnalyzer.getAvatarSprites().get(0))){
                correctAvatar = spriteOccurrences.get(avatar);
            } else {
                wrongAvatar += spriteOccurrences.get(avatar);
            }
        }

        if(correctAvatar == 1 && wrongAvatar == 0){
            return 1;
        } else {
            return 0;
        }
    }
}
