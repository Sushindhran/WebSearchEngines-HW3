package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;

/**
 * Representation of a user query.
 *
 * In HW1: instructors provide this simple implementation.
 *
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 *
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
    public String _query = null;
    public String location = null;
    public Vector<String> _tokens = new Vector<String>();
    public Vector<String> _tokens2 = new Vector<String>();

    public Query(String query) {
        _query = query;
    }
    public Query(String query, String location) {
        _query = query;
        this.location = location;
    }

/*    public void appendLocation() {
        _query = _query + " " + location;
    }*/

    public void processQuery() {
        if (_query == null) {
            return;
        }
        Scanner s = new Scanner(_query);
        while (s.hasNext()) {
            String word = HtmlParser.cleanString(s.next()).toLowerCase();
            Vector<String> stopWords = new StopWords().getStopWords();
            if(!stopWords.contains(word)) {
                _tokens.add(PorterStemming.getStemmedWord(word));
                _tokens2.add(PorterStemming.getStemmedWord(word));
            }
        }
        s.close();
    }
}