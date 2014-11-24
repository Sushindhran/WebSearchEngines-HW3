/*package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;

*//**
 * Created by abhisheksanghvi on 10/17/14.
 *//*
public class HtmlParser {

    public static String parseFile (File file) {

        StringBuffer strippedOutput = new StringBuffer();

        try {
            Document html = Jsoup.parse(file, "UTF-8");

            String body = html.body().text();
            body = body.replaceAll("\"", " ").trim();
            body=body.replaceAll(",", "").trim();
            body=body.replaceAll("\t", " ").trim();
            body=body.replaceAll("'", "").trim();
            body=body.replaceAll(":", "").trim();
            body=body.replaceAll(";", "").trim();
            body=body.replaceAll("\\)", "").trim();
            body=body.replaceAll("\\(", "").trim();
            body=body.replaceAll("\t", "").trim();
            body=body.replaceAll("\\.", "").trim();

            String title = html.title();
            title=title.replaceAll(",", "").trim();
            title=title.replaceAll("\t", " ").trim();
            title=title.replaceAll("'", "").trim();
            title=title.replaceAll(":", "").trim();
            title=title.replaceAll(";", "").trim();
            title=title.replaceAll("\\)", "").trim();
            title=title.replaceAll("\\(", "").trim();
            title=title.replaceAll("\t", "").trim();
            title=title.replaceAll("\\.", "").trim();


            strippedOutput.append(title + "\t");

            strippedOutput.append(body);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strippedOutput.toString();
    }
}*/

package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.Scanner;
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
        String word =  null;
        Scanner s = new Scanner(sentence);
        while(s.hasNext()){
            word = s.next();
            //System.out.println(word);
            if(word.contains("Rasika") && word.contains("[")){
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+ word);
                break;
            }
        }

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
        File f =  new File("./data/wiki/Alief_Elsik_High_School");
        updateIndex(parseFile(f));
        //System.out.println(parseFile(f));
    }
}