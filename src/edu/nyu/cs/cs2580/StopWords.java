package edu.nyu.cs.cs2580;
import java.util.Vector;

/**
 * Created by sharikri on 10/19/14.
 */
public class StopWords {
    private Vector<String> stopWords = new Vector<String>();

    public Vector<String> getStopWords() {
        stopWords.add("the");
        stopWords.add("for");
        stopWords.add("on");
        stopWords.add("to");
        stopWords.add("and");
        stopWords.add("or");
        stopWords.add("an");
        stopWords.add("if");
        stopWords.add("but");
        stopWords.add("the");
        stopWords.add("is");
        stopWords.add("of");
        stopWords.add("at");
        stopWords.add("he");
        stopWords.add("she");
        stopWords.add("be");
        stopWords.add("because");
        stopWords.add("me");
        stopWords.add("has");
        stopWords.add("http");
        stopWords.add("a");
        stopWords.add("able");
        stopWords.add("about");
        stopWords.add("after");
        stopWords.add("ago");
        stopWords.add("also");
        stopWords.add("are");
        stopWords.add("by");
        stopWords.add("eg");
        stopWords.add("etc");
        stopWords.add("him");
        stopWords.add("his");
        stopWords.add("in");
        stopWords.add("shall");
        stopWords.add("you");
        stopWords.add("that");
        stopWords.add("it");
        stopWords.add("as");
        stopWords.add("my");
        stopWords.add("until");
        stopWords.add("[edit]");
        stopWords.add("from");
        for(int i=0; i<100;i++) {
            stopWords.add((Integer.toString(i)));
        }
        return stopWords;
    }
}
