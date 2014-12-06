package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/**
 * Class to calculate the Bhattacharya coefficient for query similairty.
 * PLease note that the queries.tsv should be included in the 'data/index' folder path 
 *
 */
public class Bhattacharya {
  protected Options _options = null;
  
  public Bhattacharya(Options options) {
    _options = options;
  }

    public void calculateBhattacharyaCoefficient(String pathToPRFOutput, String pathToOutput) throws FileNotFoundException, IOException{

        File folder = new File(pathToPRFOutput);
        File[]fileList1 = folder.listFiles();
        ArrayList<String> fileQueries = new ArrayList<String>();
        BufferedReader br1 = null;
        BufferedReader br2 = null;
        FileOutputStream outputDir = new FileOutputStream(pathToOutput);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputDir));
        ArrayList<Double> result = new ArrayList<Double>();
        fileQueries =  getQueries();

        for(int i=0; i<fileList1.length;i++){
            double resultValue = 0;
            for(int j=0; j<fileList1.length;j++){
                File f1 = new File(pathToPRFOutput+"/prf-"+(i+1)+".tsv");
                File f2 = new File(pathToPRFOutput+"/prf-"+(j+1)+".tsv");
                if((!f1.exists()) || (!f2.exists())){
                    break;
                }
                br1 = new BufferedReader(new InputStreamReader(new FileInputStream(pathToPRFOutput+"/prf-"+(i+1)+".tsv")));
                br2 = new BufferedReader(new InputStreamReader(new FileInputStream(pathToPRFOutput+"/prf-"+(j+1)+".tsv")));

                String line = null;
                HashMap<String, Double> map = new HashMap<String, Double>();
                while((line = br1.readLine())!=null){
                    Scanner s = new Scanner(line);
                    map.put(s.next().toString(), Double.parseDouble(s.next()));
                }
                String term = null;
                double value = 0;
                while((line = br2.readLine())!=null){
                    Scanner s = new Scanner(line);
                    term = s.next().toString();
                    value = Double.parseDouble(s.next());
                    if(map.containsKey(term)){
                         resultValue = resultValue + Math.sqrt(map.get(term) * value);
                    }
                }
                    bw.write(fileQueries.get(i));
                    bw.write("\t");
                    bw.write(fileQueries.get(j));
                    bw.write("\t");
                    bw.write(Double.toString(resultValue));
                    bw.write("\n");
                resultValue = 0.0;
            }
            result.add(resultValue);
        }
        bw.close();
        br1.close();
        br2.close();

    }

    private ArrayList<String> getQueries() throws  IOException {
        BufferedReader br =  new BufferedReader(new InputStreamReader(new FileInputStream(_options._indexPrefix+"/queries.tsv")));
        String line= null;
        ArrayList<String> fileQueries = new ArrayList<String>();
        while((line = br.readLine()) != null){
            fileQueries.add(line);
        }
        br.close();
        return fileQueries;
    }

    public static void main(String[] arg) throws IOException {
        Bhattacharya b = new Bhattacharya(new Options("conf/engine.conf"));
        b.calculateBhattacharyaCoefficient(arg[0],arg[1]);        
    }

}