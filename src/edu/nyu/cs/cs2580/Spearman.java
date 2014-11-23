package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.*;


public class Spearman {
    private static Vector<DocumentInfo> docInfos = new Vector<DocumentInfo>();

    private static void buildM(Map<String,Double> pageRanks,Map<String,Integer> numViews) {
        Map<String, DocumentInfo> infosM = new HashMap<String, DocumentInfo>();

        Iterator it = pageRanks.entrySet().iterator();
        String name;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            name = (String) pairs.getKey();
            if (infosM.get(name)==null) {
                DocumentInfo di = new DocumentInfo(name);
                di.pr = (Double) pairs.getValue();
                infosM.put(name,di);
            }
        }
        it = numViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            name = (String) pairs.getKey();
            if (infosM.get(name)==null) {
                DocumentInfo di = new DocumentInfo(name);
                di.nv = (Integer) pairs.getValue();
                infosM.put(name,di);
            } else {
                DocumentInfo di = infosM.get(name);
                di.nv = (Integer) pairs.getValue();
            }
        }
        it = infosM.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            name = (String) pairs.getKey();
            DocumentInfo di = (DocumentInfo) pairs.getValue();
            docInfos.add(di);
        }
    }

    private static void load(String pageRankPath, String numViewPath) throws IOException {
        SearchEngine.Options opt = new SearchEngine.Options("conf/engine.conf");
        CorpusAnalyzer analyzer = CorpusAnalyzer.Factory.getCorpusAnalyzerByOption(opt);
        LogMiner miner = LogMiner.Factory.getLogMinerByOption(opt);
        Map<String,Double> rankScores = (HashMap<String,Double>) ((CorpusAnalyzerPagerank)analyzer).loadFromFile(pageRankPath);
        Map<String,Integer> numViews = (HashMap<String,Integer>) ((LogMinerNumviews)miner).loadFromFile(numViewPath);
        buildM(rankScores, numViews);
    }

    private static void compute() {
        calculateP();
        calculateN();
        double up = 0;
        double tmp;
        for (int i = 0; i<docInfos.size();i++) {
            tmp = docInfos.get(i).p - docInfos.get(i).n;
            up = up + tmp * tmp;
        }
        tmp = docInfos.size();
        double down = tmp * (tmp * tmp-1);
        double tao = 1.0 - 6.0 * up/down;
        System.out.println(tao);
    }

    private static void calculateP() {
        Collections.sort(docInfos, new pageRankComparator());

        for (int i = 0; i < docInfos.size(); i++) {
            docInfos.get(i).p = (double) i + 1;
        }
    }

    private static void calculateN() {
        Collections.sort(docInfos, new numViewComparator());
        for (int i = 0; i < docInfos.size(); i++)
            docInfos.get(i).n = (double) i + 1;
    }

    public static class pageRankComparator implements Comparator<DocumentInfo> {
        public int compare(DocumentInfo doc1, DocumentInfo doc2) {
            if (doc1.pr > doc2.pr) {
                return -1;
            } else if (doc1.pr < doc2.pr) {
                return 1;
            }
            return doc1.name.compareTo(doc2.name);
        }
    }

    public static class numViewComparator implements Comparator<DocumentInfo> {
        public int compare(DocumentInfo doc1, DocumentInfo doc2) {
            if (doc1.nv > doc2.nv) {
                return -1;
            } else if (doc1.nv < doc2.nv) {
                return 1;
            }
            return doc1.name.compareTo(doc2.name);
        }
    }

    public static void main (String [] args) throws IOException{
        Spearman.load("data/index/pageRank.tsv", "data/index/numViews.tsv");
        Spearman.compute();
    }
}
