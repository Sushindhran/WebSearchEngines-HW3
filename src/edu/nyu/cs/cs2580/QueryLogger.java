package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.*;


public class QueryLogger {
    private Options options;
    private String logPath;
    private String query;

    public QueryLogger(String _query) {
        try {
            query = _query.toLowerCase();
            options = new Options("conf/engine.conf");
            logPath = options._logPrefix + "/";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int checkGreater(String current) {
        return current.compareTo(query);
    }

    public int[] checkIfQueryExists() throws IOException {
        int lineCount = -1, start = -1, results[] = {-1, -1};
        String line;
        boolean found = false;
        File file = new File(logPath + query.charAt(0) +".tsv");
        if(!file.exists()) {
            return results;
        }
        FileReader f = new FileReader(logPath + query.charAt(0) +".tsv");
        BufferedReader br = new BufferedReader(f);
        while((line = br.readLine()) != null) {
            lineCount++;
            String lineArr[] = line.split("\t");
            //First line is count
            if(lineCount != 0) {
                int val = checkGreater(lineArr[0]);
                if(val == 0) {
                    found = true;
                    break;
                } else if(val < 0) {
                    //Insert point.
                    start = lineCount;
                } else {
                    break;
                }
            }
        }
        br.close();
        if(found) {
            results[0] = 1;
            results[1] = lineCount;
        } else {
            results[0] = 0;
            results[1] = start;
        }
        return results;
    }

    public void writeToFile() {
        try {
            int results[] = checkIfQueryExists();
            int lineCount = 0;
            String line;
            StringBuilder builder = new StringBuilder(logPath).append(query.charAt(0) + "temp.tsv");
            BufferedWriter writer = new BufferedWriter(new FileWriter(builder.toString(), true));
            System.out.println(results[0] + " " + results[1]);
            if(results[0] == -1 && results[1] == -1) {
                writer.write(1 + "\n");
                writer.write(query + "\t" + 1 + "\n");
            } else {
                FileReader fr = new FileReader(logPath + query.charAt(0)+".tsv");
                BufferedReader br = new BufferedReader(fr);

                while ((line = br.readLine()) != null) {
                    if (results[0] == 0) {
                        if (lineCount == 0) {
                            Integer x = Integer.parseInt(line) + 1;
                            line = x.toString();
                        } else if (lineCount == results[1]) {
                            writer.write(query + "\t" + 1 + "\n");
                            break;
                        }
                    } else {
                         if (lineCount == results[1]) {
                            String lineArr[] = line.split("\t");
                            Integer x = Integer.parseInt(lineArr[1]) + 1;
                            line = lineArr[0] + "\t" + x.toString();
                            writer.write(line + "\n");
                            break;
                         }
                    }
                    writer.write(line + "\n");
                    lineCount++;
                }

                while ((line = br.readLine()) != null) {
                    writer.write(line + "\n");
                }
                br.close();
            }
            writer.close();
            renameFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renameFiles() {
        File oldFile = new File(logPath + query.charAt(0) + "temp.tsv");
        File newFile = new File(logPath + query.charAt(0) + ".tsv");
        if(newFile.exists()) {
            newFile.delete();
        }
        oldFile.renameTo(newFile);
        oldFile.delete();
    }
}
