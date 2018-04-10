package dallidalli.commonClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class CSV {

    public static void writeCSV(String name, String setting, ArrayList<String> time, ArrayList<String> evaluated,ArrayList<String> value, ArrayList<String> avgValue){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("experiments/"+name + "_" + setting + "_" + System.currentTimeMillis() + ".csv"));
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
