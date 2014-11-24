package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.IOException;
import java.util.*;

/**
 * Instructors' code for illustration purpose. Non-tested code.
 * 
 * @author congyu
 */
public class RankerConjunctive extends Ranker {

  public RankerConjunctive(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
    Document doc = null;
    int docid = -1;
      try {
          while ((doc = _indexer.nextDoc(query, docid)) != null) {
            rankQueue.add(new ScoredDocument(doc, 1.0));
            if (rankQueue.size() > numResults) {
              rankQueue.poll();
            }
            docid = doc._docid;
          }
      } catch (IOException e) {
          e.printStackTrace();
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
      }

      Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    ScoredDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
      results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    return results;
  }

    @Override
    public HashMap<String, Double> pseudoRelevanceFeedback(List<ScoredDocument> scoredDocuments, int m) {
        return null;
    }
}
