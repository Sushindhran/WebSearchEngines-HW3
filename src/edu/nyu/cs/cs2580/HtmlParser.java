package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.Vector;

/**
 * Created by abhisheksanghvi on 10/17/14.
 */
public class HtmlParser {

    public static String parseFile (File file) {

        StringBuffer strippedOutput = new StringBuffer();

        try {
            Document html = Jsoup.parse(file, "UTF-8");

            String body = html.body().text();
            body = cleanString(body);


            String title = html.title();
            title=cleanString(title);


            strippedOutput.append(title + "\t");

            strippedOutput.append(body);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strippedOutput.toString();
    }

    private static void updateIndex(String document) {
        String[] words = document.split(" ");
        for (int i = 0; i < words.length; i++) {
            String lower = words[i].trim().toLowerCase();
            lower=lower.replaceAll(" ", "");
            Vector<String> stopWords = new StopWords().getStopWords();
            String term = PorterStemming.getStemmedWord(lower);
            if (!stopWords.contains(term) && term != " " && term.length() > 1) {
                System.out.println("Word: "+term);
            }
        }
    }

    public static String cleanString(String sentence) {
        sentence=sentence.replaceAll("\\s+", " ").trim();
        sentence = sentence.replaceAll("\"", " ").trim();
        sentence=sentence.replaceAll("\n", " ").trim();
        sentence=sentence.replaceAll(",", "").trim();
        sentence=sentence.replaceAll("\t", " ").trim();
        sentence=sentence.replaceAll("'", "").trim();
        sentence=sentence.replaceAll(":", "").trim();
        sentence=sentence.replaceAll(";", "").trim();
        sentence=sentence.replaceAll("\\?", "").trim();
        sentence=sentence.replaceAll("\\)", "").trim();
        sentence=sentence.replaceAll("\\(", "").trim();
        sentence=sentence.replaceAll(".\\[", " \\[").trim();
        sentence=sentence.replaceAll("\\[(.*?)\\]", "").trim();
        sentence=sentence.replaceAll("\\. ", " ").trim();
        sentence=sentence.replaceAll("<", "").trim();
        sentence=sentence.replaceAll(">", "").trim();
        sentence=sentence.replaceAll("\\{", "").trim();
        sentence=sentence.replaceAll("\\}", "").trim();
        sentence=sentence.replaceAll("\\*", "").trim();
        sentence=sentence.replaceAll("\\s+", " ").trim();
        return sentence;
    }

    public static boolean checkIfRedirect(String filename) {
        File f = new File(filename);
        try {
            FileReader filereader = new FileReader(f);
            BufferedReader bufferedreader = new BufferedReader(filereader);
            int count = 0;
            while(bufferedreader.readLine() != null) {
                count++;
            }

            if(count<20) {
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String args[]) {
        File f =  new File("./data/wiki/.DS_Store");
        updateIndex(parseFile(f));
        //System.out.println(parseFile(f));
    }
}