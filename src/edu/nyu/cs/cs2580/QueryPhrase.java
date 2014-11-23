package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
    public Vector< Vector<String>> _phraseTokens = new Vector<Vector<String>>();

    public QueryPhrase(String query) {
        super(query);
    }

    @Override
    public void processQuery() {
        if (_query == null) {
            return;
        }
        String token;
        Scanner s1 = new Scanner(_query);
        Pattern pattern = Pattern.compile("\"[^\"]*\"");
        while ((token = s1.findInLine(pattern)) != null) {
            _query = _query.replace(token,"");
            Vector<String> temp = new Vector<String>();
            int end = token.length() -1;
            token = token.substring(1, end);
            Scanner s2 = new Scanner(token);
            while (s2.hasNext()) {
                String word = HtmlParser.cleanString(s2.next()).toLowerCase();
                temp.add(PorterStemming.getStemmedWord(word));
            }
            _phraseTokens.add(temp);

            s2.close();
        }
        s1.close();

        Scanner s3 = new Scanner(_query);
        while (s3.hasNext()) {
            String word = HtmlParser.cleanString(s3.next()).toLowerCase();
            _tokens.add(PorterStemming.getStemmedWord(word));
        }
        s3.close();
    }
}