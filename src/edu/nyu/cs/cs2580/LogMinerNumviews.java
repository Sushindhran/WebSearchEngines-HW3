package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {
    private File corpusFolder = new File(_options._corpusPrefix);
    private File logFolder = new File(_options._logPrefix);
    private HashMap<String, Integer> numViews = new HashMap<String, Integer>();

    public LogMinerNumviews(Options options) {
        super(options);
    }

    public HashMap<String, Integer> getNumViews() {
        return numViews;
    }

    /**
     * This function processes the logs within the log directory as specified by
     * the {@link _options}. The logs are obtained from Wikipedia dumps and have
     * the following format per line: [language]<space>[article]<space>[#views].
     * Those view information are to be extracted for documents in our corpus and
     * stored somewhere to be used during indexing.
     *
     * Note that the log contains view information for all articles in Wikipedia
     * and it is necessary to locate the information about articles within our
     * corpus.
     *
     * @throws IOException
     */
    @Override
    public void compute() throws IOException {
        System.out.println("Computing using " + this.getClass().getName());
        getDocuments();
        readLogs();
        writeToDisk();
    }

    /**
     * During indexing mode, this function loads the NumViews values computed
     * during mining mode to be used by the indexer.
     *
     * @throws IOException
     */
    @Override
    public Object load() throws IOException {
        System.out.println("Loading using " + this.getClass().getName());
        String fileName =  _options._indexPrefix + "/numViews.tsv";
        return loadFromFile(fileName);
    }

    /**
     * Puts the doc names in the numviews map and initializes all values
     * in it to 0.
     * @throws IOException
     */
    private void getDocuments() throws IOException {
        for (final File file : corpusFolder.listFiles()) {
            if (!file.isDirectory()) {
                numViews.put(file.getName(), 0);
            }
        }
        System.out.println(numViews.size());
    }

    /**
     * Read all the logs and store numviews for each document in a map
     * @throws IOException
     */
    private void readLogs() throws IOException {
        FileReader filereader;
        BufferedReader bufferedreader;
        String line;
        for (File file : logFolder.listFiles()) {
            if (!file.isDirectory()) {
                filereader = new FileReader(file);
                bufferedreader = new BufferedReader(filereader);
                while ((line = bufferedreader.readLine())!=null) {
                    String[] lineList = line.split(" ");
                    //System.out.println(lineList[1]);
                    String docName = URIParser.normalizeURL(lineList[1]);
                    Integer oldVal = numViews.get(docName);
                    if (oldVal != null && lineList.length == 3 && isInteger(lineList[2])) {
                        numViews.put(docName, oldVal + Integer.parseInt(lineList[2]));
                    }
                }
                bufferedreader.close();
            }
        }
        System.out.println(numViews.size());
    }

    /**
     * Write the numviews data into file
     * @throws IOException
     */
    private void writeToDisk() throws IOException {
        String outputPath =  _options._indexPrefix + "/numViews.tsv";
        FileWriter f = new FileWriter(outputPath);
        BufferedWriter bw = new BufferedWriter(f);
        Iterator iter = numViews.entrySet().iterator();
        String fileName;
        int score;
        while (iter.hasNext()) {
            Map.Entry pairs = (Map.Entry) iter.next();
            fileName = (String)pairs.getKey();
            score = (Integer)pairs.getValue();
            bw.write(fileName + "\t" +Integer.toString(score));
            bw.newLine();
        }
        bw.close();
    }

    /**
     * Load the numviews from the file into numviews map.
     * @throws IOException
     */
    public Object loadFromFile(String fileName) throws IOException {
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line;
        String lineList[];
        while ((line = br.readLine()) != null) {
            lineList = line.split("\t");
            numViews.put(lineList[0], Integer.parseInt(lineList[1]));
        }
        return numViews;
    }

    /**
     * Utility to check if a string is and Integer
     * @param s - String
     * @return - returns a boolean
     */
    private boolean isInteger(String s) {
        try {
            Integer i = Integer.parseInt(s);
            return true;
        }
        catch(NumberFormatException er) {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        LogMinerNumviews l = new LogMinerNumviews(new Options("conf/engine.conf"));
        l.compute();
        l.load();
        System.out.println(l.getNumViews().size());
    }
}
