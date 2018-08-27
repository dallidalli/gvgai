package dallidalli.commonClasses;

import core.competition.CompetitionParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Class used to export CSV-file
 */
public class CSV {

    /**
     * Creates a CSV file from the information passed as parameters
     *
     * @param name
     * @param setting
     * @param time
     * @param evaluated
     * @param value
     * @param avgValue
     */
    public static void writeCSV(String name, String setting, ArrayList<String> time, ArrayList<String> evaluated,ArrayList<String> value, ArrayList<String> avgValue){
        int duration = (int)(CompetitionParameters.LEVEL_ACTION_TIME / 60000);

        String path = "experiments/"+SharedData.gameName+"_"+duration+"min/";

        File directory = new File(path);
        if (! directory.exists()){
            directory.mkdir();
        }

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(path+name + "_" + setting + "_" + System.currentTimeMillis() + ".csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("time");
        sb.append(',');
        sb.append("evaluated");
        sb.append(',');
        sb.append("value");
        sb.append(',');
        sb.append("avgValue");
        sb.append('\n');

        for(int i = 0; i < time.size(); i++){
            sb.append(time.get(i));
            sb.append(',');
            sb.append(evaluated.get(i));
            sb.append(',');
            sb.append(value.get(i));
            sb.append(',');
            sb.append(avgValue.get(i));
            sb.append('\n');
        }

        pw.write(sb.toString());
        pw.close();
    }
}
