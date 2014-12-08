package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.IOException;
import java.util.*;

/**
 * @CS2580: Implement this class for HW3 based on your {@code RankerFavorite}
 * from HW2. The new Ranker should now combine both term features and the
 * document-level features including the PageRank and the NumViews.
 */
public class RankerComprehensive extends Ranker {

    public RankerComprehensive(Options options,
                               CgiArguments arguments, Indexer indexer) {
        super(options, arguments, indexer);
        System.out.println("Using Ranker: " + this.getClass().getSimpleName());
    }

    @Override
    public Vector<ScoredDocument> runQuery(Query query, int numResults) {
        Document doc = null;
        ScoredDocument scoredDoc = null;
        int docId = -1;
        Vector<ScoredDocument> results = new Vector<ScoredDocument>();
        Queue<ScoredDocument> retrieval_results = new PriorityQueue<ScoredDocument>(numResults);
        try {
            while ((doc = _indexer.nextDoc(query, docId)) != null) {
                retrieval_results.add(runqueryQL(query, doc._docid));
                if (numResults < retrieval_results.size()) {
                    retrieval_results.poll();
                }
                docId = doc._docid;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        while ((scoredDoc = retrieval_results.poll()) != null) {

            results.add(scoredDoc);
        }
        Collections.sort(results, Collections.reverseOrder());
        return results;
    }

    /**
     * Method for scoring documents based on QL
     *
     * @param query the query words
     * @param did   the document id
     * @return the scored document
     */
    public ScoredDocument runqueryQL(Query query, int did) {
        double lambda = 0.5d, score = 0.0d;
        DocumentIndexed d = (DocumentIndexed) _indexer.getDoc(did);
        Vector<String> qv = new Vector<String>();
        float pageRank = d.getPageRank();
        int numviews = d.getNumViews();
        for (String str : query._tokens) {
            //Check the token for spaces and handle them accordingly
            String[] temp = str.split(" ");
            if (temp.length > 1) {
                for (String term : temp) {
                    qv.add(term);
                }
            } else {
                qv.add(str);
            }
        }

        for (String q : qv) {
            int docTerFreq, corpusTerFreq;
            long totWordsInDoc, totWordsInCorp;
            double cumulativeVal = 0.0d;
            docTerFreq = _indexer.documentTermFrequency(q, (d.getUrl()));
            corpusTerFreq = _indexer.corpusTermFrequency(q);
            totWordsInDoc = d.getNumberOfWords();
            totWordsInCorp = _indexer.totalTermFrequency();

            if (totWordsInDoc != 0) {
                cumulativeVal += ((1 - lambda) * ((double) docTerFreq / totWordsInDoc));
            }

            if (totWordsInCorp != 0) {
                cumulativeVal += ((lambda) * ((double) corpusTerFreq / totWordsInCorp));
            }
            score += (Math.log(cumulativeVal) / Math.log(2));
        }

        score = score * 0.65;
        score += 0.39*(Math.log(pageRank) / Math.log(2));
        score += 0.0001*(Math.log(numviews) / Math.log(2));

        ScoredDocument scoredDocument = new ScoredDocument(d, Math.pow(2, score));
        scoredDocument.setPageRank(pageRank);
        scoredDocument.setNumDocs(numviews);
        return scoredDocument;
    }

    public HashMap<String, Double> pseudoRelevanceFeedback(List<ScoredDocument> results, int numTerms) {
        Map<Integer, Double> finalAns = new HashMap<Integer, Double>();
        Map<Integer, Integer> temp = new HashMap<Integer, Integer>();


        Map<Integer, List<Integer>> topTerms = new HashMap<Integer, List<Integer>>();

        for (ScoredDocument document : results) {
            int docid = document.getDocId();
            topTerms.put(docid, _indexer.getTopTerms(docid, numTerms));
        }

        int denominator = 0;

        for (int docId : topTerms.keySet()) {
            List<Integer> listOfTerms = topTerms.get(docId);
            for (int i = 0; i < listOfTerms.size(); i = i + 2) {
                if (!temp.containsKey(listOfTerms.get(i))) {
                    temp.put(listOfTerms.get(i), listOfTerms.get(i + 1));
                } else {
                    temp.put(listOfTerms.get(i), temp.get(listOfTerms.get(i)) + listOfTerms.get(i + 1));
                }
            }
        }


        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(temp.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        List<Map.Entry<Integer, Integer>> sublist = list.subList(0, numTerms);
        temp.clear();

        for (Map.Entry<Integer, Integer> term : sublist) {
            int numerator = 0;
            for (ScoredDocument document : results) {
                numerator = numerator + _indexer.documentTermFrequency(_indexer.getTermName(term.getKey()), document.getDocURL());
            }
            temp.put(term.getKey(), numerator);
        }

        for (int term : temp.keySet()) {
            denominator = denominator + temp.get(term);
        }

        for (int term : temp.keySet()) {
            finalAns.put(term, (temp.get(term) * 1.0) / denominator);
        }

        HashMap<String, Double> returnMap = new HashMap<String, Double>();
        for (int term : finalAns.keySet()) {
            returnMap.put(_indexer.getTermName(term), finalAns.get(term));
        }

        return returnMap;
    }
}
