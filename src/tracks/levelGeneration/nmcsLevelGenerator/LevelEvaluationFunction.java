package tracks.levelGeneration.nmcsLevelGenerator;

import core.game.GameDescription;
import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class LevelEvaluationFunction {

    //public HashMap<String, Integer> listOfNeededSprites;
    //public HashMap<String, ArrayList<Integer>> listOfForbiddenSprites;
    //public HashMap<String, ArrayList<Integer>> listOfStopCounterSprites;
    public HashMap<String, Integer> spriteOccurances;


    public ArrayList<String> solidSprites;
    public ArrayList<String> avatarSprites;
    public boolean clustedSolid;
    public ArrayList<String> collectableSprites;
    public ArrayList<String> nonSpawnedHarmfulSprites;
    public ArrayList<String> spawnerHarmfulSprites;
    public ArrayList<String> otherSprites;
    public ArrayList<String> goalSprites;

    public boolean[] isSpriteCounterTermination = new boolean[2];
    public boolean[] isSpriteCounterMoreTermination = new boolean[2];
    public boolean[] isMultiSpriteCounterTermination = new boolean[2];
    public boolean[] isMultiSpriteCounterSubTypesTermination = new boolean[2];
    public boolean[] isStopCounterTermination = new boolean[2];
    public boolean[] isTimeOutTermination = new boolean[2];
    public ArrayList<GameDescription.TerminationData> terminationConditions;
    public ArrayList<GameDescription.TerminationData> winningConditions;
    public ArrayList<GameDescription.TerminationData> loosingConditions;

    public LevelEvaluationFunction(){

    }



    public double validateTerminationConditions(){
        double totalValue = 0;
        double numberOfConditions = terminationConditions.size();

        for (GameDescription.TerminationData td:terminationConditions) {
            double subTotal = 0;
            double numSubCondition = 0;

            if(td.type.equals("SpriteCounter")){
                for (String sprite:td.sprites) {
                    numSubCondition++;

                    if (spriteOccurances.get(sprite) > td.limit){
                        subTotal++;
                    }

                }
            }

            if(td.type.equals("SpriteCounterMore")){
                for (String sprite:td.sprites) {
                    numSubCondition++;

                    if (spriteOccurances.get(sprite) < td.limit){
                        subTotal++;
                    }
                }
            }

            if(td.type.equals("MultiSpriteCounter")){
                numSubCondition++;
                int sum = 0;
                for (String sprite:td.sprites) {
                    sum = sum + spriteOccurances.get(sprite);
                }
                if(sum != td.limit){
                    subTotal++;
                }
            }

            if(td.type.equals("MultiSpriteCounterSubTypes")){
                numSubCondition++;
                int sum = 0;
                for (String sprite:td.sprites) {
                    sum = sum + spriteOccurances.get(sprite);
                }
                if(sum != td.limit){
                    subTotal++;
                }
            }

            if(td.type.equals("StopCounter")){
                int sum = 0;
                numSubCondition++;
                for (String sprite:td.sprites) {
                    sum = sum + spriteOccurances.get(sprite);
                }
                if (sum == td.limit){
                    subTotal++;
                }
            }

            if(td.type.equals("TimeOut")){

            }

            totalValue = totalValue + (subTotal/numSubCondition);
        }

        return (totalValue/numberOfConditions);
    }

    public void generateEvaluationFunction(){
        //listOfNeededSprites = new HashMap<String, Integer>();
        //listOfForbiddenSprites = new HashMap<String, ArrayList<Integer>>();
        //listOfStopCounterSprites = new HashMap<String, ArrayList<Integer>>();
        spriteOccurances = new HashMap<String, Integer>();

        solidSprites = new ArrayList<String>();
        solidSprites = (ArrayList<String>) SharedData.gameAnalyzer.getSolidSprites().clone();
        double numSolid = solidSprites.size();
        clustedSolid = false;
        if(numSolid == 1){
            clustedSolid = true;
        }

        System.out.println(solidSprites);

        avatarSprites = new ArrayList<String>();
        avatarSprites = (ArrayList<String>) SharedData.gameAnalyzer.getAvatarSprites().clone();
        double numAvatar = avatarSprites.size();
        if(numAvatar > 1){
            double numTransformInteraction = -1;
            ArrayList<String> obsoleteAvatars = new ArrayList<String>();
            String basicAvatar = avatarSprites.get(0);


            for (int i = 0; i < SharedData.gameDescription.getAllSpriteData().size(); i++){
                String currentAvatar = avatarSprites.get(0);
                for (int n = 0; n < SharedData.gameDescription.getAllSpriteData().size(); n++){
                    if (i != n){
                        String otherSprite = SharedData.gameDescription.getAllSpriteData().get(n).name;
                        for (int z = 0; z < SharedData.gameDescription.getInteraction(currentAvatar, otherSprite).size(); z++){
                            GameDescription.InteractionData possibleInteractions = SharedData.gameDescription.getInteraction(currentAvatar, otherSprite).get(z);
                            if(possibleInteractions.type == "TransformTo"){
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

        collectableSprites = new ArrayList<String>();
        collectableSprites = (ArrayList<String>) SharedData.gameAnalyzer.getCollectableSprites().clone();
        double numCollectable = collectableSprites.size();

        nonSpawnedHarmfulSprites = new ArrayList<String>();
        nonSpawnedHarmfulSprites = SharedData.gameAnalyzer.getHarmfulSprites();

        spawnerHarmfulSprites = new ArrayList<String>();

        ArrayList<String> spawnedSprites = new ArrayList<String>();
        for (String sprite: nonSpawnedHarmfulSprites) {
            String currentType = lookupSprite(sprite).type;
            if(SharedData.gameAnalyzer.checkIfSpawned(sprite) == 0){
                spawnedSprites.add(sprite);
            } else if(SharedData.gameAnalyzer.spawnerTypes.contains(currentType)){
                spawnerHarmfulSprites.add(sprite);
            }
        }

        nonSpawnedHarmfulSprites.removeAll(spawnedSprites);
        nonSpawnedHarmfulSprites.removeAll(spawnedSprites);

        double numHarmful = nonSpawnedHarmfulSprites.size();
        double numSpawnerHarmful = nonSpawnedHarmfulSprites.size();

        otherSprites = new ArrayList<String>();
        otherSprites = SharedData.gameAnalyzer.getOtherSprites();
        spawnedSprites.clear();
        for (String sprite:otherSprites) {
            if (SharedData.gameAnalyzer.checkIfSpawned(sprite) == 0){
                spawnedSprites.add(sprite);
            }
        }
        otherSprites.removeAll(spawnedSprites);

        double numOther = otherSprites.size();

        terminationConditions = (ArrayList<GameDescription.TerminationData>) SharedData.gameDescription.getTerminationConditions().clone();

        winningConditions = new ArrayList<GameDescription.TerminationData>();
        loosingConditions = new ArrayList<GameDescription.TerminationData>();

        for (GameDescription.TerminationData td:terminationConditions) {
            if (td.win.split(",")[0] == "Win"){
                winningConditions.add(td);
                editConditions(0, td.type);
            } else if (td.win.split(",")[0] == "Lose"){
                loosingConditions.add(td);
                editConditions(1, td.type);
            }
        }

        /*

        for (GameDescription.TerminationData td:terminationConditions) {
            if(td.type.equals("SpriteCounter")){
                for (String sprite:td.sprites) {
                    if(listOfNeededSprites.containsKey(sprite)){
                        if (listOfNeededSprites.get(sprite) < td.limit+1){
                            listOfNeededSprites.put(sprite, td.limit+1);
                        }
                    } else {
                        listOfNeededSprites.put(sprite, td.limit+1);
                    }
                }
            }

            if(td.type.equals("SpriteCounterMore")){
                for (String sprite:td.sprites) {
                    if(listOfNeededSprites.containsKey(sprite)){
                        if (listOfNeededSprites.get(sprite) > td.limit-1){
                            listOfNeededSprites.put(sprite, td.limit-1);
                        }
                    } else {
                        listOfNeededSprites.put(sprite, td.limit-1);
                    }
                }
            }

            if(td.type.equals("MultiSpriteCounter") || td.type.equals("MultiSpriteCounterSubTypes")){
                for (String sprite:td.sprites) {
                    if(listOfForbiddenSprites.containsKey(sprite)){
                        if (listOfForbiddenSprites.get(sprite).contains(td.limit)){
                            ArrayList<Integer> tmp = listOfForbiddenSprites.get(sprite);
                            tmp.add(td.limit);
                            listOfForbiddenSprites.put(sprite, tmp);
                        }
                    } else {
                        ArrayList<Integer> tmp = new ArrayList<Integer>();
                        tmp.add(td.limit);
                        listOfForbiddenSprites.put(sprite, tmp);
                    }
                }
            }

            if(td.type.equals("StopCounter")){
                for (String sprite:td.sprites) {
                    if(listOfStopCounterSprites.containsKey(sprite)){
                        if (listOfStopCounterSprites.get(sprite).contains(td.limit)){
                            ArrayList<Integer> tmp = listOfStopCounterSprites.get(sprite);
                            tmp.add(td.limit);
                            listOfStopCounterSprites.put(sprite, tmp);
                        }
                    } else {
                        ArrayList<Integer> tmp = new ArrayList<Integer>();
                        tmp.add(td.limit);
                        listOfStopCounterSprites.put(sprite, tmp);
                    }
                }
            }

            if(td.type.equals("TimeOut")){

            }
        }

        */

        goalSprites = new ArrayList<String>();
        goalSprites = SharedData.gameAnalyzer.getGoalSprites();
        double numGoal = goalSprites.size();

        ArrayList<String> winningGoalSprites = new ArrayList<String>();
    }

    private void editConditions(int index, String type) {
        switch (type){
            case "SpriteCounter":
                isSpriteCounterTermination[index] = true;
                break;

            case "SpriteCounterMore":
                isSpriteCounterMoreTermination[index] = true;
                break;

            case "MultiSpriteCounter":
                isMultiSpriteCounterTermination[index] = true;
                break;

            case "MultiSpriteCounterSubTypes":
                isMultiSpriteCounterSubTypesTermination[index] = true;
                break;

            case "StopCounter":
                isStopCounterTermination[index] = true;
                break;
            case "TimeOut":
                isTimeOutTermination[index] = true;
                break;

            default:
                break;
        }
    }

    private GameDescription.SpriteData lookupSprite(String sprite){
        GameDescription.SpriteData found = null;

        for (GameDescription.SpriteData data:SharedData.gameDescription.getAllSpriteData()) {
            if(data.name == sprite){
                found = data;
                break;
            }
        }

        return found;
    }
}
