package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.*;
import java.util.*;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
    private Map<String, Integer> documents = new HashMap<String, Integer>();
    private ArrayList<ArrayList<Integer>> docLinks = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Integer> docLinkCount = new ArrayList<Integer>();
    private Map<Integer, Integer> redirectMap = new HashMap<Integer, Integer>();
    private ArrayList<Float> _R = new ArrayList<Float> ();
    private float lambda = 0.9f;
    private int iterations = 2;

    final File corpusFolder = new File(_options._corpusPrefix);

    public CorpusAnalyzerPagerank(Options options) {
        super(options);
    }

    public ArrayList<Float> getPageRanks() {
        return _R;
    }

    /**
     * This function processes the corpus as specified inside {@link _options}
     * and extracts the "internal" graph structure from the pages inside the
     * corpus. Internal means we only store links between two pages that are both
     * inside the corpus.
     *
     * Note that you will not be implementing a real crawler. Instead, the corpus
     * you are processing can be simply read from the disk. All you need to do is
     * reading the files one by one, parsing them, extracting the links for them,
     * and computing the graph composed of all and only links that connect two
     * pages that are both in the corpus.
     *
     * Note that you will need to design the data structure for storing the
     * resulting graph, which will be used by the {@link compute} function. Since
     * the graph may be large, it may be necessary to store partial graphs to
     * disk before producing the final graph.
     *
     * @throws IOException
     */
    @Override
    public void prepare() throws IOException {
        System.out.println("Preparing " + this.getClass().getName());
        getDocuments();
        for (final File fileEntry : corpusFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                findLinks(fileEntry.getName());
            }
        }
        resolveRedirects();
    }

    /**
     * This function computes the PageRank based on the internal graph generated
     * by the {@link prepare} function, and stores the PageRank to be used for
     * ranking.
     *
     * Note that you will have to store the computed PageRank with each document
     * the same way you do the indexing for HW2. I.e., the PageRank information
     * becomes part of the index and can be used for ranking in serve mode. Thus,
     * you should store the whatever is needed inside the same directory as
     * specified by _indexPrefix inside {@link _options}.
     *
     * @throws IOException
     */
    @Override
    public void compute() throws IOException {
        ArrayList<Float> _I = new ArrayList<Float> ();
        System.out.println("Computing using " + this.getClass().getName());
        int corpusSize = docLinkCount.size();

        //Initialize all pageRanks with score 1.0/|P|
        for(int i = 0; i < corpusSize; i++) {
            _I.add(1.0f/corpusSize);

        }

        for(int i = 0; i<iterations; i++) {
            for (int j = 0; j < corpusSize; j++) {
                _R.add(lambda/corpusSize);
            }

            for (Map.Entry<String, Integer> entry : documents.entrySet()) {
                ArrayList<Integer> x = docLinks.get(entry.getValue());
                if(x.size() > 0) {
                    for(int d = 0; d < x.size(); d++) {
                        Integer docId = x.get(d);
                        Float val = _R.get(docId);
                        val += (1.0f - lambda) * _I.get(entry.getValue())/x.size();
                        _R.set(docId, val);
                    }
                } else {
                    for(int d = 0; d < corpusSize; d++) {
                        Float val = _R.get(d);
                        val += (1.0f - lambda) * _I.get(entry.getValue())/corpusSize;
                        _R.set(d, val);
                    }
                }
                _I = _R;
            }
        }
        outputPageRank();
    }

    /**
     * During indexing mode, this function loads the PageRank values computed
     * during mining mode to be used by the indexer.
     *
     * @throws IOException
     */
    @Override
    public Object load() throws IOException {
        System.out.println("Loading using " + this.getClass().getName());
        String fileName =  _options._indexPrefix + "/pageRank.tsv";
        return loadFromFile(fileName);
    }

    public void getDocuments() {
        int docCount = 0;
        for (File file : corpusFolder.listFiles()) {
            if (!file.isDirectory()) {
                documents.put(file.getName(), docCount);
                docLinkCount.add(0);
                docLinks.add(new ArrayList<Integer>());
                docCount++;
            }
        }
    }

    public void findLinks(String fileName) throws IOException {
        int destId, sourceId = documents.get(fileName);
        //System.out.println("FileName "+fileName);
        String wholePath = _options._corpusPrefix + "/" + fileName;
        File file = new File(wholePath);
        HeuristicLinkExtractor hle = new HeuristicLinkExtractor(file);
        String link;
        while ((link = hle.getNextInCorpusLinkTarget())!=null) {
            if (documents.get(link) != null) {
                //Check if redirect
                destId = documents.get(link);
                if(HtmlParser.checkIfRedirect( _options._corpusPrefix+"/"+link)) {
                    redirectMap.put(sourceId, destId);
                    break;
                } else {

                    if (!docLinks.get(sourceId).contains(destId)) {
                        docLinkCount.set(sourceId, docLinkCount.get(sourceId) + 1);
                        docLinks.get(sourceId).add(destId);
                    }
                }
            }
        }
    }

    private void resolveRedirects() throws IOException{
        System.out.println("Resolving ");

        //Resolve redirects
        Set<Integer> redirectSources = redirectMap.keySet();
        Iterator<ArrayList<Integer>> dit = docLinks.iterator();
        while(dit.hasNext()) {
            ArrayList<Integer> arrayList = dit.next();
            ArrayList<Integer> removeList = new ArrayList<Integer>();
            ArrayList<Integer> addList = new ArrayList<Integer>();
            Iterator<Integer> sIt = redirectSources.iterator();
            while(sIt.hasNext()) {
                Integer s = sIt.next();
                if(arrayList.contains(s)) {
                    removeList.add(s);
                    if(!arrayList.contains(redirectMap.get(s))) {
                        addList.add(redirectMap.get(s));
                    }
                }
            }

            //Add or remove
            arrayList.removeAll(removeList);
            arrayList.addAll(addList);

        }
    }

    private void outputPageRank() throws IOException{
        String outputPath =  _options._indexPrefix + "/pageRank.tsv";
        FileWriter fstream = new FileWriter(outputPath);
        BufferedWriter out = new BufferedWriter(fstream);
        Iterator it = documents.entrySet().iterator();
        String fileName;
        int index;
        double score;
        double sum = 0;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            fileName = (String)pairs.getKey();
            index = (Integer) pairs.getValue();
            score = _R.get(index);
            sum  = sum + score;
            out.write(fileName + "\t" +Double.toString(score));
            out.newLine();
        }
        out.close();
    }

    public Object loadFromFile(String fileName) throws IOException {
        Map<String, Float> rankScores = new HashMap<String, Float>();
        FileReader filereader = new FileReader(fileName);
        BufferedReader bufferedreader = new BufferedReader(filereader);
        String line;
        String[] tmp;
        while ((line = bufferedreader.readLine()) != null) {
            tmp = line.split("\t");
            rankScores.put(tmp[0], Float.parseFloat(tmp[1]));
        }
        bufferedreader.close();
        return rankScores;
    }

    public static void main(String[] args) throws IOException {
        CorpusAnalyzerPagerank c = new CorpusAnalyzerPagerank(new Options("conf/engine.conf"));
        c.prepare();
        c.compute();
        c.load();
    }
}