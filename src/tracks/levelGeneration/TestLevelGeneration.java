package tracks.levelGeneration;

import core.competition.CompetitionParameters;
import dallidalli.commonClasses.SharedData;

import java.util.Random;

public class TestLevelGeneration {


    public static void main(String[] args) {

		// Available Level Generators
		String randomLevelGenerator = "tracks.levelGeneration.randomLevelGenerator.LevelGenerator";
		String geneticGenerator = "tracks.levelGeneration.geneticLevelGenerator.LevelGenerator";
		String constructiveLevelGenerator = "tracks.levelGeneration.constructiveLevelGenerator.LevelGenerator";
		String improvedConstructiveLevelGenerator = "dallidalli.improvedConstructiveLevelGenerator.LevelGenerator";
		String mctsLevelGenerator = "dallidalli.mctsLevelGenerator.LevelGenerator";
		String nmcsLevelGenerator = "dallidalli.nmcsLevelGenerator.LevelGenerator";
		String nrpaLevelGenerator = "dallidalli.nrpaLevelGenerator.LevelGenerator2";

		String generator = improvedConstructiveLevelGenerator;

		String gamesPath = "examples/gridphysics/";
		String physicsGamesPath = "examples/contphysics/";
		String generateLevelPath = "experiments/generatedLevels/";


		String games[] = new String[] { "aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", // 0-4
				"beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman", // 5-9
				"boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky", // 10-14
				"camelRace", "catapults", "chainreaction", "chase", "chipschallenge", // 15-19
				"clusters", "colourescape", "chopper", "cookmepasta", "cops", // 20-24
				"crossfire", "defem", "defender", "digdug", "dungeon", // 25-29
				"eighthpassenger", "eggomania", "enemycitadel", "escape", "factorymanager", // 30-34
				"firecaster", "fireman", "firestorms", "freeway", "frogs", // 35-39
				"garbagecollector", "gymkhana", "hungrybirds", "iceandfire", "ikaruga", // 40-44
				"infection", "intersection", "islands", "jaws", "killBillVol1", // 45-49
				"labyrinth", "labyrinthdual", "lasers", "lasers2", "lemmings", // 50-54
				"missilecommand", "modality", "overload", "pacman", "painter", // 55-59
				"pokemon", "plants", "plaqueattack", "portals", "raceBet", // 60-64
				"raceBet2", "realportals", "realsokoban", "rivers", "roadfighter", // 65-69
				"roguelike", "run", "seaquest", "sheriff", "shipwreck", // 70-74
				"sokoban", "solarfox", "superman", "surround", "survivezombies", // 75-79
				"tercio", "thecitadel", "thesnowman", "waitforbreakfast", "watergame", // 80-84
				"waves", "whackamole", "wildgunman", "witnessprotection", "wrapsokoban", // 85-89
				"zelda", "zenpuzzle" }; // 90, 91


		String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
										// + levelIdx + "_" + seed + ".txt";
										// where to record the actions
										// executed. null if not to save.

		// Other settings
		int seed = new Random().nextInt();
		int gameIdx = 0; // 0 39 75 90


		SharedData.eval_weights.add(3.0); //AccessibilityConstraint
		SharedData.eval_weights.add(4.0); //AvatarNumberConstraint
		SharedData.eval_weights.add(1.0); //ConnectedWallsConstraint
		SharedData.eval_weights.add(2.0); //CoverPercentageConstraint
		SharedData.eval_weights.add(4.0); //EndsInitiallyConstraint
		SharedData.eval_weights.add(1.0); //GoalDistanceConstraint
		SharedData.eval_weights.add(1.0); //NeutralHarmfulRatioConstraint
		SharedData.eval_weights.add(4.0); //SimplestAvatarConstraint
		SharedData.eval_weights.add(2.0); //SpriteNumberConstraint
		SharedData.eval_weights.add(3.0); //SpaceAroundAvatarConstraint
		SharedData.eval_weights.add(1.0); //SymmetryConstraint


		// 1. This starts a game, in a generated level created by a specific level generator
		//if(LevelGenMachine.generateOneLevel(game, mctsLevelGenerator, recordLevelFile)){
		//    LevelGenMachine.playOneGeneratedLevel(game, recordActionsFile, recordLevelFile, seed);
		//}

		int n = 1;

		if(args.length > 0){

			switch(args[1]){
				case "mcts":
					core.competition.CompetitionParameters.LEVEL_ACTION_TIME = 60000 * Integer.parseInt(args[0]);
					generator = mctsLevelGenerator;
					gameIdx = Integer.parseInt(args[2]);
					n = Integer.parseInt(args[3]);
					SharedData.MAX_SIZE = Double.parseDouble(args[4]);
					SharedData.MIN_SIZE = SharedData.MAX_SIZE;
					SharedData.MCTS_Cvalue = Double.parseDouble(args[5]);
					SharedData.MCTS_restart = Double.parseDouble(args[6]);
					break;
				case "nmcs":
					core.competition.CompetitionParameters.LEVEL_ACTION_TIME = 60000 * Integer.parseInt(args[0]);
					generator = nmcsLevelGenerator;
					gameIdx = Integer.parseInt(args[2]);
					n = Integer.parseInt(args[3]);
					SharedData.MAX_SIZE = Double.parseDouble(args[4]);
					SharedData.MIN_SIZE = SharedData.MAX_SIZE;
					SharedData.NMCS_level = Integer.parseInt(args[5]);
					SharedData.NMCS_injected = Boolean.parseBoolean(args[6]);
					break;
				case "nrpa":
					core.competition.CompetitionParameters.LEVEL_ACTION_TIME = 60000 * Integer.parseInt(args[0]);
					generator = nrpaLevelGenerator;
					gameIdx = Integer.parseInt(args[2]);
					n = Integer.parseInt(args[3]);
					SharedData.MAX_SIZE = Double.parseDouble(args[4]);
					SharedData.MIN_SIZE = SharedData.MAX_SIZE;
					SharedData.NRPA_level = Integer.parseInt(args[5]);
					SharedData.NRPA_cutoff = Integer.parseInt(args[6]);
					SharedData.NRPA_alpha = Double.parseDouble(args[7]);
					SharedData.NRPA_numIterations = Integer.parseInt(args[8]);
					break;
				default:
					break;
			}

		}

		int duration = (int)(CompetitionParameters.LEVEL_ACTION_TIME/60000);


		String recordLevelFile = generateLevelPath +games[gameIdx] + "_"+ generator+"_"+duration+"min"+System.currentTimeMillis()+"_glvl.txt";
		String game = gamesPath + games[gameIdx] + ".txt";

		SharedData.gameName = games[gameIdx];


		if (generator == improvedConstructiveLevelGenerator){
			for(int i = 0; i < 30; i++){
				gameIdx = 0;
				game = gamesPath + games[gameIdx] + ".txt";
				recordLevelFile = generateLevelPath +games[gameIdx] + "_"+ generator+"_"+duration+"min"+System.currentTimeMillis()+"_glvl.txt";
				SharedData.desiredCoverage = SharedData.MIN_COVER_PERCENTAGE + Math.random()*(SharedData.MAX_COVER_PERCENTAGE - SharedData.MIN_COVER_PERCENTAGE);
				System.out.println(SharedData.desiredCoverage);
				LevelGenMachine.generateOneLevel(game, generator, recordLevelFile);


				gameIdx = 39;
				game = gamesPath + games[gameIdx] + ".txt";
				recordLevelFile = generateLevelPath +games[gameIdx] + "_"+ generator+"_"+duration+"min"+System.currentTimeMillis()+"_glvl.txt";
				SharedData.desiredCoverage = SharedData.MIN_COVER_PERCENTAGE + Math.random()*(SharedData.MAX_COVER_PERCENTAGE - SharedData.MIN_COVER_PERCENTAGE);
				System.out.println(SharedData.desiredCoverage);
				LevelGenMachine.generateOneLevel(game, generator, recordLevelFile);


				gameIdx = 75;
				game = gamesPath + games[gameIdx] + ".txt";
				recordLevelFile = generateLevelPath +games[gameIdx] + "_"+ generator+"_"+duration+"min"+System.currentTimeMillis()+"_glvl.txt";
				SharedData.desiredCoverage = SharedData.MIN_COVER_PERCENTAGE + Math.random()*(SharedData.MAX_COVER_PERCENTAGE - SharedData.MIN_COVER_PERCENTAGE);
				System.out.println(SharedData.desiredCoverage);
				LevelGenMachine.generateOneLevel(game, generator, recordLevelFile);


				gameIdx = 90;
				game = gamesPath + games[gameIdx] + ".txt";
				recordLevelFile = generateLevelPath +games[gameIdx] + "_"+ generator+"_"+duration+"min"+System.currentTimeMillis()+"_glvl.txt";
				SharedData.desiredCoverage = SharedData.MIN_COVER_PERCENTAGE + Math.random()*(SharedData.MAX_COVER_PERCENTAGE - SharedData.MIN_COVER_PERCENTAGE);
				System.out.println(SharedData.desiredCoverage);
				LevelGenMachine.generateOneLevel(game, generator, recordLevelFile);
			}
		}else{
			for (int i = 0; i < n; i++) {
				recordLevelFile = generateLevelPath +games[gameIdx] + "_"+ generator+"_"+duration+"min"+System.currentTimeMillis()+"_glvl.txt";
				SharedData.desiredCoverage = SharedData.MIN_COVER_PERCENTAGE + Math.random()*(SharedData.MAX_COVER_PERCENTAGE - SharedData.MIN_COVER_PERCENTAGE);
				System.out.println(SharedData.desiredCoverage);
				LevelGenMachine.generateOneLevel(game, generator, recordLevelFile);
				if(n == 1){
					//LevelGenMachine.playOneGeneratedLevel(game, recordActionsFile, recordLevelFile, seed);
				}
			}
		}



		// 2. This generates numberOfLevels levels.
		// String levelGenerator = "tracks.levelGeneration." + args[0] + ".LevelGenerator";
		// int numberOfLevels = 5;
		// tracks.levelGeneration.randomLevelGenerator.LevelGenerator.includeBorders = true;

		// String[] folderName = levelGenerator.split("\\.");
		// generateLevelPath = "examples/generatedLevels/" + folderName[1] + "/";

		// game = gamesPath + args[1] + ".txt";
		// for (int i = 0; i < numberOfLevels; i++) {
		// 	recordLevelFile = generateLevelPath + args[1] + "_lvl" + i + ".txt";
		// 	LevelGenMachine.generateOneLevel(game, levelGenerator, recordLevelFile);
		//}


    }
}
