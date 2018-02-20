package tracks.levelGeneration.nrpaLevelGenerator;

import core.game.Event;
import core.game.GameDescription;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import sun.security.provider.SHA;
import tools.ElapsedCpuTimer;
import tools.LevelMapping;
import tools.Pair;
import tools.StepController;
import tracks.levelGeneration.constraints.CombinedConstraints;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneratedLevel implements Comparable<GeneratedLevel> {

    /**
     * current level
     */
    private ArrayList<String>[][] level;
    /**
     * current fitness if its a feasible
     */
    private ArrayList<Double> fitness;
    /**
     * current fitness if its an infeasible
     */
    private double constrainFitness;
    /**
     * if the fitness is calculated before (no need to recalculate)
     */
    private boolean calculated;
    /**
     * the best automated agent
     */
    private AbstractPlayer automatedAgent;
    /**
     * the naive automated agent
     */
    private AbstractPlayer naiveAgent;
    /**
     * the do nothing automated agent
     */
    private AbstractPlayer doNothingAgent;
    /**
     * The current stateObservation of the level
     */
    private StateObservation stateObs;

    /**
     * initialize the level with a certain length and width
     * @param width
     * @param height
     */
    @SuppressWarnings("unchecked")
    public GeneratedLevel(int width, int height){
        this.level = new ArrayList[height][width];
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                this.level[y][x] = new ArrayList<String>();
            }
        }
        this.fitness = new ArrayList<Double>();
        this.calculated = false;
        this.stateObs = null;

        System.out.println(SharedData.gameAnalyzer.getSolidSprites());
        System.out.println(SharedData.gameDescription.getAvatar());
    }


    /**
     * clone the level data
     */
    public GeneratedLevel clone(){
        GeneratedLevel c = new GeneratedLevel(level[0].length, level.length);

        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                c.level[y][x].addAll(level[y][x]);
            }
        }

        return c;
    }


    public void setPosition(SpritePointData position, String sprite){
        level[position.y][position.x].clear();
        level[position.y][position.x].add(sprite);
    }

    public void clearPosition(SpritePointData position){
        level[position.y][position.x].clear();
    }

    public void resetCalculated(){
        this.calculated = false;
        this.constrainFitness = 0;
    }

    /**
     * initialize the agents used during evaluating the level
     */
    @SuppressWarnings("unchecked")
    private void constructAgent(){
        try{
            Class agentClass = Class.forName(SharedData.AGENT_NAME);
            Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
            automatedAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try{
            Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
            Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
            naiveAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try{
            Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
            Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
            doNothingAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * initialize the empty level
     */
    public void InitializeEmpty(){
        ArrayList<String> solidSprites = SharedData.gameAnalyzer.getSolidSprites();
        if(solidSprites.size() > 0){
            //picking a random solid object to use
            String randomSolid = solidSprites.get(SharedData.random.nextInt(solidSprites.size()));

            //adding a borders around the level
            for(int y = 0; y < level.length; y++){
                for(int x = 0; x < level[y].length; x++){
                    level[y][x].clear();
                    if (y == 0 || y == (level.length -1) || x == 0 || x == (level[y].length -1)){
                        level[y][x].add(randomSolid);
                    }
                }
            }
        }
    }


    /**
     * initialize the level with random sprites
     */
    public void InitializeRandom(){
        ArrayList<GameDescription.SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
        ArrayList<String> allSpritesString = new ArrayList<String>();

        for (GameDescription.SpriteData sprite : allSprites) {
            allSpritesString.add(sprite.name);
        }

        int size = level.length * level[0].length;
        double coverage = SharedData.MIN_COVER_PERCENTAGE + (SharedData.MAX_COVER_PERCENTAGE - SharedData.MIN_COVER_PERCENTAGE) * SharedData.random.nextDouble();

        for(int i = 0; i < (size*coverage); i++){
            String spriteName = allSprites.get(SharedData.random.nextInt(allSprites.size())).name;
            ArrayList<SpritePointData> freePositions = getFreePositions(allSpritesString);
            int index = SharedData.random.nextInt(freePositions.size());
            level[freePositions.get(index).y][freePositions.get(index).x].add(spriteName);
        }

    }

    /**
     * initialize the level using the contructive level generator
     */
    public void InitializeConstructive(){
        String[] levelString = SharedData.constructiveGen.generateLevel(SharedData.gameDescription, null, level[0].length, level.length).split("\n");
        HashMap<Character, ArrayList<String>> charMap = SharedData.constructiveGen.getLevelMapping();

        for(int y=0; y<levelString.length; y++){
            for(int x=0; x<levelString[y].length(); x++){
                if(levelString[y].charAt(x) != ' '){
                    this.level[y][x].addAll(charMap.get(levelString[y].charAt(x)));
                }
            }
        }

        FixLevel();
    }


    /**
     * get the free positions in the current level (that doesnt contain solid or object from the input list)
     * @param sprites	list of sprites names to test them
     * @return			list of all free position points
     */
    public ArrayList<SpritePointData> getFreePositions(ArrayList<String> sprites){
        ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();

        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                ArrayList<String> tileSprites = level[y][x];
                boolean found = false;
                for(String stype:tileSprites){
                    found = found || sprites.contains(stype);
                    found = found || SharedData.gameAnalyzer.getSolidSprites().contains(stype);
                }

                if(!found){
                    positions.add(new SpritePointData("", x, y));
                }
            }
        }

        return positions;
    }


    /**
     * get all the positions of all the sprites found in the input list
     * @param sprites	list of sprites
     * @return			list of points that contains the sprites in the list
     */
    private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites){
        ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();

        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                ArrayList<String> tileSprites = level[y][x];
                for(String stype:tileSprites){
                    for(String s:sprites){
                        if(s.equals(stype)){
                            positions.add(new SpritePointData(stype, x, y));
                        }
                    }
                }
            }
        }

        return positions;
    }


    /**
     * Fix the player in the level (there must be only one player no more or less)
     */
    private void FixPlayer(){
        //get the list of all the avatar names
        ArrayList<GameDescription.SpriteData> avatar = SharedData.gameDescription.getAvatar();
        ArrayList<String> avatarNames = new ArrayList<String>();
        for(GameDescription.SpriteData a:avatar){
            avatarNames.add(a.name);
        }

        //get list of all the avatar positions in the level
        ArrayList<SpritePointData> avatarPositions = getPositions(avatarNames);

        // if not avatar insert a new one
        if(avatarPositions.size() == 0){
            ArrayList<SpritePointData> freePositions = getFreePositions(avatarNames);

            int index = SharedData.random.nextInt(freePositions.size());
            level[freePositions.get(index).y][freePositions.get(index).x].add(avatarNames.get(SharedData.random.nextInt(avatarNames.size())));
        }

        //if there is more than one avatar remove all of them except one
        else if(avatarPositions.size() > 1){
            int notDelete = SharedData.random.nextInt(avatarPositions.size());
            int index = 0;
            for(SpritePointData point:avatarPositions){
                if(index != notDelete){
                    level[point.y][point.x].remove(point.name);
                }
                index += 1;
            }
        }
    }


    /**
     * Fix the level by fixing the player number
     */
    private void FixLevel(){
        FixPlayer();
    }


    /**
     * get the current used level mapping to parse the level string
     * @return	Level mapping object that can help to construct the
     * 			level string and parse the level string
     */
    public LevelMapping getLevelMapping(){
        LevelMapping levelMapping = new LevelMapping(SharedData.gameDescription);
        levelMapping.clearLevelMapping();
        char c = 'a';
        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                if(levelMapping.getCharacter(level[y][x]) == null){
                    levelMapping.addCharacterMapping(c, level[y][x]);
                    c += 1;
                }
            }
        }

        return levelMapping;
    }


    /**
     * get the current level string
     * @param levelMapping	level mapping object to help constructing the string
     * @return				string of letters defined in the level mapping
     * 						that represent the level
     */
    public String getLevelString(LevelMapping levelMapping){
        String levelString = "";
        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                levelString += levelMapping.getCharacter(level[y][x]);
            }
            levelString += "\n";
        }

        levelString = levelString.substring(0, levelString.length() - 1);

        return levelString;
    }


    /**
     * get the percentage of the level covered by objects excluding the borders
     * @return	percentage with respect to the size of the level
     */
    public double getCoverPercentage(){
        int objects = 0;
        int borders = 0;
        if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
            borders = 1;
        }
        for (int y = borders; y < level.length - borders; y++) {
            for (int x = borders; x < level[y].length - borders; x++) {
                objects += Math.min(1, level[y][x].size());
            }
        }

        return 1.0 * objects / (level.length * level[0].length);
    }


    /**
     * get game state observation for the current level
     * @return	StateObservation for the current level
     */
    private StateObservation getStateObservation(){
        if(stateObs != null){
            return stateObs;
        }

        LevelMapping levelMapping = getLevelMapping();
        String levelString = getLevelString(levelMapping);
        stateObs = SharedData.gameDescription.testLevel(levelString, levelMapping.getCharMapping());
        return stateObs;
    }


    /**
     * calculate the number of objects in the level by sprite names
     * @return	a hashmap of the number of each object based on its name
     */
    private HashMap<String, Integer> calculateNumberOfObjects(){
        HashMap<String, Integer> objects = new HashMap<String, Integer>();
        ArrayList<GameDescription.SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();


        //initialize the hashmap with all the sprite names
        for(GameDescription.SpriteData sprite:allSprites){
            objects.put(sprite.name, 0);
        }


        //modify the hashmap to reflect the number of objects found in this level
        for(int y = 0; y < level.length; y++){
            for(int x = 0; x < level[y].length; x++){
                ArrayList<String> sprites = level[y][x];
                for(String stype:sprites){
                    if(objects.containsKey(stype)){
                        objects.put(stype, objects.get(stype) + 1);
                    }
                    else{
                        objects.put(stype, 1);
                    }
                }
            }
        }

        return objects;
    }


    /**
     * Get fitness value for the current score difference between
     * the best player and the naive player
     * @param scoreDiff	difference between the best player score and the naive player score
     * @param maxScore	maximum score required to approach it
     * @return			value between 0 to 1 which is almost 1 near the maxScore.
     */
    private double getGameScore(double scoreDiff, double maxScore){
        if(maxScore == 0){
            return 1;
        }
        if(scoreDiff <= 0){
            return 0;
        }
        double result = (3 * scoreDiff / maxScore);
        return 2 / (1 + Math.exp(-result)) - 1;
    }

    /**
     * check if the player death terminates the game
     * player ID used is 0, default for single player games.
     * @return	true if the player death terminates the game and false otherwise
     */
    private boolean isPlayerCauseDeath(){

        for(GameDescription.TerminationData t: SharedData.gameDescription.getTerminationConditions()){
            String[] winners = t.win.split(",");
            Boolean win = Boolean.parseBoolean(winners[0]);

            for(String s:t.sprites){
                if(!win & SharedData.gameDescription.getAvatar().contains(s)){
                    return true;
                }
            }

        }

        return false;
    }


    /**
     * get a fitness value for the number of unique rules satisfied during playing the game
     * @param gameState		the current level after playing using the best player
     * @param minUniqueRule		minimum amount of rules needed to reach 1
     * @return			near 1 when its near to minUniqueRule
     */
    private double getUniqueRuleScore(StateObservation gameState, double minUniqueRule){
        double unique = 0;
        HashMap<Integer, Boolean> uniqueEvents = new HashMap<Integer, Boolean>();
        for(Event e:gameState.getEventsHistory()){
            int code = e.activeTypeId + 10000 * e.passiveTypeId;
            if(!uniqueEvents.containsKey(code)){
                unique += 1;
                uniqueEvents.put(code, true);
            }
        }


        /**
         * Remove the player death from the unique rules
         */
        if(isPlayerCauseDeath() && gameState.getGameWinner() == Types.WINNER.PLAYER_LOSES){
            unique -= 1;
        }

        return 2 / (1 + Math.exp(-3 * unique / minUniqueRule)) - 1;
    }


    /**
     * Play the current level using the naive player
     * @param stateObs	the current stateObservation object that represent the level
     * @param steps		the maximum amount of steps that it shouldn't exceed it
     * @param agent		current agent to play the level
     * @return			the number of steps that the agent stops playing after (<= steps)
     */
    private int getNaivePlayerResult(StateObservation stateObs, int steps, AbstractPlayer agent){
        int i =0;
        for(i=0;i<steps;i++){
            if(stateObs.isGameOver()){
                break;
            }
            Types.ACTIONS bestAction = agent.act(stateObs, null);
            stateObs.advance(bestAction);
        }

        return i;
    }

    public void calculateSoftConstraints(boolean verbose){
        Pair<Integer, Integer> avatarPosition;

        ArrayList<String> tmp = new ArrayList<String>();
        for (GameDescription.SpriteData sprite:SharedData.gameDescription.getAvatar()) {
            tmp.add(sprite.name);
        }

        ArrayList<SpritePointData> tmpdata = getPositions(tmp);

        if(tmpdata.size() > 0){
            avatarPosition = new Pair<Integer, Integer>(tmpdata.get(0).x, tmpdata.get(0).y);
        } else {
            avatarPosition = new Pair<Integer, Integer>(-1,-1);
        }


        HashMap<String, Integer> spriteOccurrences = calculateNumberOfObjects();
        ArrayList<String> solidSprites = SharedData.gameAnalyzer.getSolidSprites();
        String solidSprite;

        if(solidSprites.size() > 0){
            solidSprite = solidSprites.get(0);
        } else {
            solidSprite = "non-existent";
        }

        ArrayList<GameDescription.TerminationData> terminationData = SharedData.gameDescription.getTerminationConditions();

        ArrayList<Pair<Integer, Integer>> listOfGoals = new ArrayList<Pair<Integer, Integer>>();

        ArrayList<SpritePointData> tmpPos = getPositions(SharedData.gameAnalyzer.getGoalSprites());
        for (SpritePointData pos:tmpPos) {
            listOfGoals.add(new Pair<Integer, Integer>(pos.x, pos.y));
        }

        ArrayList<String> avatarSprites = SharedData.gameAnalyzer.getAvatarSprites();


        double coverPercentage = getCoverPercentage();

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("avatarPosition", avatarPosition);
        parameters.put("level", level);
        parameters.put("spriteOccurrences", spriteOccurrences);
        parameters.put("solidSprites", solidSprites);
        parameters.put("solidSprite", solidSprite);
        parameters.put("coverPercentage", coverPercentage);
        parameters.put("minCoverPercentage", SharedData.MIN_COVER_PERCENTAGE);
        parameters.put("maxCoverPercentage", SharedData.MAX_COVER_PERCENTAGE);
        parameters.put("gameAnalyzer", SharedData.gameAnalyzer);
        parameters.put("terminationConditions", terminationData);
        parameters.put("width", level[0].length);
        parameters.put("height", level.length);
        parameters.put("listOfGoals", listOfGoals);
        parameters.put("avatarSpritesIn", avatarSprites);
        parameters.put("allSprites", SharedData.gameDescription.getAllSpriteData());


        CombinedConstraints constraint = new CombinedConstraints();
        constraint.addConstraints(new String[]{"AccessibilityConstraint", "AvatarNumberConstraint", "ConnectedWallsConstraint", "CoverPercentageConstraint", "EndsInitiallyConstraint", "GoalDistanceConstraint", "SimplestAvatarConstraint", "SpriteNumberConstraint", "SpaceAroundAvatarConstraint"});
        constraint.setParameters(parameters);

        if(verbose){
            constraint.listConstraints();
        }

        constrainFitness = constraint.checkConstraint();
    }

    /**
     * Calculate the current fitness of the level
     * @param time	amount of time to evaluate the level
     * @return		current fitness of the level
     */
    public ArrayList<Double> calculateFitness(long time){
        if(!calculated){
            constructAgent();

            calculated = true;
            StateObservation stateObs = getStateObservation();

            //Play the game using the best agent
            StepController stepAgent = new StepController(automatedAgent, SharedData.EVALUATION_STEP_TIME);
            ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
            elapsedTimer.setMaxTimeMillis(time);
            stepAgent.playGame(stateObs.copy(), elapsedTimer);

            StateObservation bestState = stepAgent.getFinalState();
            ArrayList<Types.ACTIONS> bestSol = stepAgent.getSolution();

            StateObservation doNothingState = null;
            int doNothingLength = Integer.MAX_VALUE;
            //playing the game using the donothing agent and naive agent
            for(int i = 0; i< SharedData.REPETITION_AMOUNT; i++){
                StateObservation tempState = stateObs.copy();
                int temp = getNaivePlayerResult(tempState, bestSol.size(), doNothingAgent);
                if(temp < doNothingLength){
                    doNothingLength = temp;
                    doNothingState = tempState;
                }
            }
            double coverPercentage = getCoverPercentage();

            //calculate the maxScore need to be satisfied based on the difference
            //between the score of different collectible objects
            double maxScore = 0;
            if(SharedData.gameAnalyzer.getMinScoreUnit() > 0){
                double numberOfUnits = SharedData.gameAnalyzer.getMaxScoreUnit() / (SharedData.MAX_SCORE_PERCENTAGE * SharedData.gameAnalyzer.getMinScoreUnit());
                maxScore = numberOfUnits * SharedData.gameAnalyzer.getMinScoreUnit();
            }


            //calculate the constrain fitness by applying all different constraints
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("solutionLength", bestSol.size());
            parameters.put("minSolutionLength", SharedData.MIN_SOLUTION_LENGTH);
            parameters.put("doNothingSteps", doNothingLength);
            parameters.put("doNothingState", doNothingState.getGameWinner());
            parameters.put("bestPlayer", bestState.getGameWinner());
            parameters.put("minDoNothingSteps", SharedData.MIN_DOTHING_STEPS);
            parameters.put("coverPercentage", coverPercentage);
            parameters.put("minCoverPercentage", SharedData.MIN_COVER_PERCENTAGE);
            parameters.put("maxCoverPercentage", SharedData.MAX_COVER_PERCENTAGE);
            parameters.put("spriteOccurrences", calculateNumberOfObjects());
            parameters.put("gameAnalyzer", SharedData.gameAnalyzer);
            parameters.put("gameDescription", SharedData.gameDescription);

            CombinedConstraints constraint = new CombinedConstraints();
            constraint.addConstraints(new String[]{"SolutionLengthConstraint", "DeathConstraint",

                    "CoverPercentageConstraint", "SpriteNumberConstraint", "GoalConstraint", "AvatarNumberConstraint", "WinConstraint"});
            constraint.setParameters(parameters);
            constrainFitness = constraint.checkConstraint();

            System.out.println("SolutionLength:" + bestSol.size() + " doNothingSteps:" + doNothingLength + " coverPercentage:" + coverPercentage + " bestPlayer:" + bestState.getGameWinner());


            //calculate the fitness if it satisfied all the constraints
            if(constrainFitness >= 1){
                StateObservation naiveState = null;
                for(int i = 0; i< SharedData.REPETITION_AMOUNT; i++){
                    StateObservation tempState = stateObs.copy();
                    getNaivePlayerResult(tempState, bestSol.size(), naiveAgent);
                    if(naiveState == null || tempState.getGameScore() > naiveState.getGameScore()){
                        naiveState = tempState;
                    }
                }

                double scoreDiffScore = getGameScore(bestState.getGameScore() - naiveState.getGameScore(), maxScore);
                double ruleScore = getUniqueRuleScore(bestState, SharedData.MIN_UNIQUE_RULE_NUMBER);

                fitness.add(scoreDiffScore);
                fitness.add(ruleScore);
            }

            this.automatedAgent = null;
            this.naiveAgent = null;
            this.stateObs = null;
        }

        return fitness;
    }

    /**
     * Get the current chromosome fitness
     * @return	array contains all fitness values
     */
    public ArrayList<Double> getFitness(){
        return fitness;
    }


    /**
     * Get the average value of the fitness
     * @return	average value of the fitness array
     */
    public double getCombinedFitness(){
        double result = 0;
        for(double v: this.fitness){
            result += v;
        }
        return result / this.fitness.size();
    }

    /**
     * Get constraint fitness for infeasible chromosome
     * @return	1 if its feasible and less than 1 if not
     */
    public double getConstrainFitness(){
        return constrainFitness;
    }

    /**
     * helpful data structure to hold information about certain points in the level
     * @author AhmedKhalifa
     */
    public class SpritePointData implements Comparable{
        public String name;
        public int x;
        public int y;

        public SpritePointData(String name, int x, int y){
            this.name = name;
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Object o) {
            try{
                SpritePointData p = (SpritePointData)o;

                //if(p.first == this.first && p.second == this.second)
                if(p.x == this.x && p.y == this.y && p.name.equals(this.name))
                    return 0;

            }catch(ClassCastException e)
            {
                //Whatever this is, this is not a Pair. So, not equal at all.
                return -1;
            }
            return -1; //Not sure what would the order be, so always -1.
        }

        @Override
        public boolean equals(Object obj) {
            return (compareTo(obj)==0);
        }

        public boolean sameCoordinate(SpritePointData other){
            return (other.x == this.x && other.y == this.y);
        }
    }


    /**
     * Compare two chromosome with each other based on their
     * constrained fitness and normal fitness
     */
    @Override
    public int compareTo(GeneratedLevel o) {
        if(this.constrainFitness < 1 || o.constrainFitness < 1){
            if(this.constrainFitness < o.constrainFitness){
                return 1;
            }
            if(this.constrainFitness > o.constrainFitness){
                return -1;
            }
            return 0;
        }

        double firstFitness = 0;
        double secondFitness = 0;
        for(int i=0; i<this.fitness.size(); i++){
            firstFitness += this.fitness.get(i);
            secondFitness += o.fitness.get(i);
        }

        if(firstFitness > secondFitness){
            return -1;
        }

        if(firstFitness < secondFitness){
            return 1;
        }

        return 0;
    }


}
