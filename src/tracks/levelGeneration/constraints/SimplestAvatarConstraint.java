package tracks.levelGeneration.constraints;

import core.game.GameDescription;

import java.util.ArrayList;
import java.util.HashMap;

public class SimplestAvatarConstraint extends AbstractConstraint {

    public ArrayList<String> avatarSpritesIn;
    public ArrayList<GameDescription.SpriteData> allSprites;
    public GameDescription description;
    public HashMap<String, Integer> spriteOccurrences;



    @Override
    public double checkConstraint() {
        ArrayList<String> avatarSprites = (ArrayList<String>) avatarSpritesIn.clone();
        double numAvatar = avatarSprites.size();

        if(numAvatar > 1){
            double numTransformInteraction = -1;
            ArrayList<String> obsoleteAvatars = new ArrayList<String>();
            String basicAvatar = avatarSprites.get(0);


            for (int i = 0; i < allSprites.size(); i++){
                String currentAvatar = avatarSprites.get(0);
                for (int n = 0; n < allSprites.size(); n++){
                    if (i != n){
                        String otherSprite = allSprites.get(n).name;
                        for (int z = 0; z < description.getInteraction(currentAvatar, otherSprite).size(); z++){
                            GameDescription.InteractionData possibleInteractions = description.getInteraction(currentAvatar, otherSprite).get(z);
                            if(possibleInteractions.type.equals("TransformTo")){
                                if(avatarSprites.contains(possibleInteractions.sprites.get(0))){
                                    basicAvatar = currentAvatar;
                                    obsoleteAvatars.add(possibleInteractions.sprites.get(0));
                                }
                            }
                        }
                    }
                }
            }

            avatarSprites.removeAll(obsoleteAvatars);

            if(avatarSprites.size() == 0){
                avatarSprites.add(basicAvatar);
            }
        }

        for (String avatar:avatarSpritesIn) {
            if(spriteOccurrences.get(avatar) > 0 && !avatar.equals(avatarSprites.get(0))){
                return 0;
            }
            
        }

        return 1;
    }
}
