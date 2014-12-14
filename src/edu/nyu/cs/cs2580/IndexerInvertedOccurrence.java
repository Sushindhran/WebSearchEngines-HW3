package edu.nyu.cs.cs2580;

import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * @CS2580: Implement this class for HW2.
 */

public class IndexerInvertedOccurrence extends Indexer  implements Serializable {

    private static final long serialVersionUID = 109213905740085030L;
    //Contains all documents
    private Map<Integer, DocumentIndexed> _documents = new HashMap<Integer, DocumentIndexed>();
    //Dictionary that contains all the terms and a termId
    private Map<String, Integer> dictionary = new HashMap<String, Integer>();
    //The index uses the termId as key and an array list of the occurences as value
    private Map<Integer, ArrayList<Integer>> index = new HashMap<Integer, ArrayList<Integer>>();
    //Tracker list that holds the latest position of occurrence inserted in the map.
    private ArrayList<Integer> trackerList = new ArrayList<Integer>();

    private Map<Integer, ArrayList<Integer>> cache = new HashMap<Integer, ArrayList<Integer>>();

  /*
   * This is the code for pseudo-random feedback
   */

    private Map<Integer, ArrayList<Integer>> documentTermMap = new HashMap<Integer, ArrayList<Integer>>();
    private Map<Integer, String> reverseDictionary = new HashMap<Integer, String>();


    private int partialFileCount = 0;
    int uniqueTermNum = 0;
    private boolean loadCache = false;
    private int indexLoadCount = 1;
    private boolean dictionaryLoad = true;

    public IndexerInvertedOccurrence(Options options) {
        super(options);
        System.out.println("Using Indexer: " + this.getClass().getSimpleName());
    }

    @Override
    public void constructIndex() throws IOException {
        int fileCount=0, indexCount=1;
        File corpusFolder = new File(_options._corpusPrefix);
        File[] listOfFiles = corpusFolder.listFiles();
        _numDocs = listOfFiles.length;
        //Ensuring that the map is clear
        index.clear();
        //System.out.print(listOfFiles.length);
        for (File file : listOfFiles) {

            if(fileCount==500) {
                System.out.println("Constructing Partial Index" + (int) Math.ceil(indexCount / 500));
                persist((int) Math.ceil(indexCount / 500));
                persist_DocTermMap((int) Math.ceil(indexCount / 500));
                index.clear();
                fileCount = 0;
            }
            try {
                analyse(file, indexCount);
            }catch (Exception e) {
                e.printStackTrace();
            }
            indexCount++;
            fileCount++;
        }
        persist((int)Math.floor(indexCount/500)+1);
        persist_DocTermMap((int) Math.floor(indexCount / 500)+1);
        System.out.println("Constructing Partial Index " + (int)(Math.floor(indexCount/500)+1.0));
        index.clear();

        System.out.println("Merging all partial Indexes");

        //Merging all files into one index
        mergeIndexFiles();

        writeDocumentsandDictionary();

        deleteTempFiles();

        splitFiles();
    }

    private void analyse(File file, int indexCount) {
        DocumentIndexed documentIndexed = new DocumentIndexed(indexCount);
        if(file.isFile()) {
            documentIndexed.setTitle(file.getName());
            documentIndexed.setUrl(file.getPath());
            String content = HtmlParser.parseFile(file);

            Scanner s = new Scanner(content).useDelimiter("\t");

            String title = "";
            if (s.hasNext()) {
                title = s.next();
            }

            String body="";
            if(s.hasNext()) {
                body = s.next();
            }

            //Sring buffer that contains the document content
            StringBuffer sb = new StringBuffer();
            sb.append(title);
            sb.append(" ");
            sb.append(body);
            int numWords = updateIndex(sb.toString().trim(), indexCount);
            documentIndexed.setNumberOfWords(numWords);
            _documents.put(indexCount, documentIndexed);
            createDocumentTermMap(sb.toString().trim(), indexCount);
        }
    }


  /* This function persists three data structures whenever called and clears them.
   * 1) Document map in tsv format
   * 2) Dictionary
   * 3) Index is tsv format
   */

    private void persist(int fileCount) throws IOException {
        partialFileCount++;
        try {
            //Sort the index before making a partial index
            StringBuilder indexBuilder = new StringBuilder(_options._indexPrefix).append("/").append(fileCount + "tempIndex.tsv");
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexBuilder.toString(), true));

      /* The Index is saved as follows in tsv format
       * Col1 : TermId
       * Col2 : List of Documents and corresponding values-separated by a space.
       */

            Set<Integer> indexKeys = index.keySet();

            //Convert to list to sort
            List<Integer> indexKeysList = new ArrayList<Integer>();
            indexKeysList.addAll(indexKeys);
            Collections.sort(indexKeysList);

            //Iterate over the sorted keyList
            Iterator<Integer> indexIt = indexKeysList.iterator();

            while(indexIt.hasNext()) {


                Integer key = indexIt.next();
                indexWriter.write(key.toString());

                //Value for key
                ArrayList<Integer> indexVal = index.get(key);

                //Iterate over the document details
                // 1) First Value is the number of occurences
                // 2) Subsequent Values are the locations of each occurence in the document.
                int x = 0;
                int skip = 0;

                while (x <= indexVal.size() + 1) {
                    if (x == indexVal.size()) {
                        break;
                    }

                    if (x == skip) {
                        indexWriter.write("\n");
                        //This is a docId
                        skip += indexVal.get(x + 1) + 2;
                    }

                    indexWriter.write(indexVal.get(x).toString() + "\t");
                    x++;
                }
                indexWriter.write("\n");
            }
            indexWriter.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        //Clear index
        index.clear();
    }

    private void persist_DocTermMap(int fileCount) throws IOException {
        try {
            //Sort the index before making a partial index
            StringBuilder indexBuilder = new StringBuilder(_options._indexPrefix).append("/").append(fileCount + "DocTermMap.tsv");
            BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexBuilder.toString(), true));

      /* The Index is saved as follows in tsv format
       * Col1 : TermId
       * Col2 : List of Documents and corresponding values-separated by a space.
       */

            Set<Integer> indexKeys = documentTermMap.keySet();

            //Convert to list to sort
            List<Integer> indexKeysList = new ArrayList<Integer>();
            indexKeysList.addAll(indexKeys);
            Collections.sort(indexKeysList);

            //Iterate over the sorted keyList
            Iterator<Integer> indexIt = indexKeysList.iterator();

            while(indexIt.hasNext()) {


                Integer key = indexIt.next();
                indexWriter.write(key.toString() + "-->");

                //Value for key
                ArrayList<Integer> indexVal = documentTermMap.get(key);

                //Iterate over the document details
                // 1) First Value is the number of occurences
                // 2) Subsequent Values are the locations of each occurence in the document.
                int x = 0;
                int skip = 0;

                while (x != indexVal.size()) {
                    indexWriter.write(indexVal.get(x).toString() + "\t");
                    x++;
                }
                indexWriter.write("\n");
            }
            indexWriter.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        //Clear index
        documentTermMap.clear();
    }



    private void writeDocumentsandDictionary() throws IOException {
        String docFile = "/documentsAndDict.tsv";
        StringBuilder mergebuilder = new StringBuilder(_options._indexPrefix).append(docFile);
        BufferedWriter mergeWriter = new BufferedWriter(new FileWriter(mergebuilder.toString(), true));

        for(int document : _documents.keySet()) {
            DocumentIndexed docIndexed = _documents.get(document);
            mergeWriter.write((document + "\t"));
            mergeWriter.write(docIndexed.getTitle() + "\t" + docIndexed.getUrl() + "\t");
            mergeWriter.write(docIndexed.getNumberOfWords()+"");
            mergeWriter.newLine();
        }
        //Clear all documents from the map after writing.
        _documents.clear();

        mergeWriter.write("#####\n");

        for(String term: dictionary.keySet()) {
            int termId = dictionary.get(term);
            mergeWriter.write(termId+"\t");
            mergeWriter.write(term);
            mergeWriter.newLine();
        }

        mergeWriter.close();
        //Clear the dictionary
        dictionary.clear();

        mergeWriter.close();
    }


    private void deleteTempFiles() throws  IOException {
        String finalIndexFile = "invertedIndexOccurrence.tsv";
        String dictFile = "documentsAndDict.tsv";
        File indexFolder = new File(_options._indexPrefix);
        File[] listOfFiles = indexFolder.listFiles();

        //Delete ever file except invertedIndexOccurence.tsv

        for(File eachFile : listOfFiles) {
            if(!eachFile.getName().equals(finalIndexFile) && !eachFile.getName().equals(dictFile) && !eachFile.getName().contains("DocTermMap")
                    && !eachFile.getName().contains("pageRank")&& !eachFile.getName().contains("numViews")) {
                eachFile.delete();
            }
        }
    }

    private void mergeIndexFiles() throws IOException {
        String indexFile = "/invertedIndexOccurrence.tsv";
        mergeTwoFiles("/1tempIndex.tsv", "/2tempIndex.tsv");
        File f1 = new File(_options._indexPrefix+"/1tempIndex.tsv");
        File f2 = new File(_options._indexPrefix+"/2tempIndex.tsv");
        f1.delete();
        f2.delete();
        for(int i=3; i<=partialFileCount; i++) {
            File oldFile = new File(_options._indexPrefix+"/temp.tsv");
            System.out.println("Deleted temp "+i);
            File newFile = new File(_options._indexPrefix+"/first.tsv");
            oldFile.renameTo(newFile);
            try {
                File temp = new File(_options._indexPrefix+"/temp.tsv");
                temp.delete();
                mergeTwoFiles("first.tsv", i + "tempIndex.tsv");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        File oldFile = new File(_options._indexPrefix+"/temp.tsv");
        File newFile = new File(_options._indexPrefix+indexFile);
        oldFile.renameTo(newFile);
    }

    private void mergeTwoFiles(String firstFile, String secondFile) throws IOException {

        try{
            StringBuilder mergebuilder = new StringBuilder(_options._indexPrefix).append("/temp.tsv");
            BufferedWriter mergeWriter = new BufferedWriter(new FileWriter(mergebuilder.toString(), true));

            StringBuilder firstbuilder = new StringBuilder(_options._indexPrefix).append("/"+firstFile);
            BufferedReader firstReader = new BufferedReader(new FileReader(firstbuilder.toString()));

            StringBuilder secondbuilder = new StringBuilder(_options._indexPrefix).append("/"+secondFile);
            BufferedReader secondReader = new BufferedReader(new FileReader(secondbuilder.toString()));

            if(firstFile ==null || firstFile ==".DS_Store" || firstFile == "DocMap.tsv" || firstFile == "Dictionary.tsv") {
                File oldFile = new File(_options._indexPrefix + "/" + secondFile);
                File newFile = new File(_options._indexPrefix+"/temp.tsv");
                oldFile.renameTo(newFile);
                mergeWriter.close();
                firstReader.close();
                secondReader.close();
                return;
            } else if(secondFile == null || secondFile ==".DS_Store" || firstFile == "DocMap.tsv" || firstFile == "Dictionary.tsv") {
                File oldFile = new File(_options._indexPrefix + "/" + firstFile);
                File newFile = new File(_options._indexPrefix+"/temp.tsv");
                oldFile.renameTo(newFile);
                mergeWriter.close();
                firstReader.close();
                secondReader.close();
                return;
            }

            String firstline = firstReader.readLine(), secondline = secondReader.readLine();

            int prevTermId = -1;
            while((secondline != null) && (firstline != null)) {
                List<String> secondlist = null, firstlist=null;
                if(firstline != null) {
                    firstlist = stringTokenizer(firstline);
                } else {
                    mergeWriter.write(secondline);
                    secondline=secondReader.readLine();
                    continue;
                }

                if(secondline != null) {
                    secondlist = stringTokenizer(secondline);
                } else {
                    mergeWriter.write(firstline);
                    firstline=firstReader.readLine();
                    continue;
                }

                if(firstlist.size() == 1 || secondlist.size() ==1 ) {
                    if(Integer.parseInt(firstlist.get(0))>Integer.parseInt(secondlist.get(0))
                            && (Integer.parseInt(secondlist.get(0))>prevTermId)) {

                        mergeWriter.write(secondlist.get(0)+"\n");
                        secondline = secondReader.readLine();
                        secondlist = stringTokenizer(secondline);
                        while(secondlist.size()>1) {
                            mergeWriter.write(secondline+"\n");
                            secondline = secondReader.readLine();
                            secondlist = stringTokenizer(secondline);
                        }
                    } else if (Integer.parseInt(firstlist.get(0)) < Integer.parseInt(secondlist.get(0))) {
                        prevTermId = Integer.parseInt(firstlist.get(0));
                        mergeWriter.write(firstlist.get(0)+"\n");
                        firstline = firstReader.readLine();
                        firstlist = stringTokenizer(firstline);
                        String check = firstline;
                        while(firstlist.size()>1 && (firstline = firstReader.readLine())!=null) {
                            mergeWriter.write(check+"\n");
                            check = firstline;
                            firstlist = stringTokenizer(firstline);
                        }

                    } else {
                        mergeWriter.write(firstlist.get(0)+"\n");
                        firstline = firstReader.readLine();
                        firstlist = stringTokenizer(firstline);
                        while(firstlist.size()>1) {
                            mergeWriter.write(firstline+"\n");
                            firstline = firstReader.readLine();
                            if(firstline==null) {
                                break;
                            }
                            firstlist = stringTokenizer(firstline);
                        }

                        secondline = secondReader.readLine();
                        secondlist = stringTokenizer(secondline);
                        while(secondlist.size()>1) {
                            mergeWriter.write(secondline+"\n");
                            secondline = secondReader.readLine();
                            if(secondline==null) {
                                break;
                            }
                            secondlist = stringTokenizer(secondline);
                        }
                    }
                }
            }

            if(firstline!=null) {
                while(firstline!=null) {
                    mergeWriter.write(firstline+"\n");
                    firstline=firstReader.readLine();
                }
            }

            if(secondline!=null) {
                while(secondline!=null) {
                    mergeWriter.write(secondline+"\n");
                    secondline=secondReader.readLine();
                }
            }

            mergeWriter.close();
            firstReader.close();
            secondReader.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void splitFiles() throws FileNotFoundException, IOException {
        String indexFile = "invertedIndexOccurrence.tsv";
        StringBuilder firstbuilder = new StringBuilder(_options._indexPrefix).append("/"+indexFile);
        BufferedReader firstReader = new BufferedReader(new FileReader(firstbuilder.toString()));

        StringBuilder splitbuilder = new StringBuilder(_options._indexPrefix).append("/index1.tsv");
        BufferedWriter splitWriter = new BufferedWriter(new FileWriter(splitbuilder.toString(), true));

        int count = 0, indexCount = 1;
        String line;
        while((line = firstReader.readLine()) != null) {
            List<String> list = stringTokenizer(line);

            if(list.size() == 1) {
                count++;
                if(count == 500 ) {
                    count = 0;
                    indexCount++;
                    splitWriter.close();
                    splitbuilder = new StringBuilder(_options._indexPrefix).append("/index"+indexCount+".tsv");
                    splitWriter = new BufferedWriter(new FileWriter(splitbuilder.toString(), true));
                    splitWriter.flush();
                }
            }
            splitWriter.write(line);
            splitWriter.newLine();
        }
        splitWriter.close();
    }

    private List<String> stringTokenizer(String str) {
        List<String> tokenList = new ArrayList<String>();
        try {
            StringTokenizer st = new StringTokenizer(str, "\t");
            while (st.hasMoreElements()) {
                tokenList.add(st.nextElement().toString());
            }
        }catch (Exception e) {
            //IT is null
        }
        return tokenList;
    }

    private int updateIndex(String document, int indexCount) {
        String[] words = document.split(" ");
        for (int i=0; i<words.length; i++) {
            String lower = words[i].toLowerCase().trim();
            //lower.replace("\""," ").trim();
            lower = lower.replaceAll(" ","");
            Vector<String> stopWords = new StopWords().getStopWords();
            String term = PorterStemming.getStemmedWord(lower);
            if(!stopWords.contains(term) && term != " " && term.length()>1) {
                //System.out.print("In updateIndex: Dictionary size "+dictionary.size());

                if (!dictionary.containsKey(term)) {
                    dictionary.put(term, uniqueTermNum);
                    //reverseDictionary.put(uniqueTermNum, term);

                    ArrayList<Integer> occurrence = new ArrayList<Integer>();
                    occurrence.add(indexCount);
                    occurrence.add(1);
                    occurrence.add(i);
                    index.put(uniqueTermNum, occurrence);

                    trackerList.add(0);
                    uniqueTermNum++;
                } else {
                    //System.out.print("In updateIndex: Dictionary size "+dictionary.size());
                    int termId = dictionary.get(term);
                    int latestPosition = trackerList.get(termId);

                    if(!index.containsKey(termId)) {
                        ArrayList<Integer> occurrence = new ArrayList<Integer>();
                        occurrence.add(indexCount);
                        occurrence.add(1);
                        occurrence.add(i);
                        index.put(termId, occurrence);
                        trackerList.set(termId, 0);
                    } else {
                        ArrayList<Integer> occurrence = index.get(termId);
                        if (occurrence.get(latestPosition) == indexCount) {
                            occurrence.set(latestPosition + 1, occurrence.get(latestPosition + 1) + 1);
                            occurrence.add(i);
                        } else {
                            occurrence.add(indexCount);
                            latestPosition = occurrence.size() - 1;
                            trackerList.set(termId, latestPosition);
                            occurrence.add(1);
                            occurrence.add(i);
                        }
                    }
                }
                _totalTermFrequency++;
            }
        }
        return words.length;
    }

    private void createDocumentTermMap(String document, int indexCount) {
        Map<Integer, Integer> docWordCount = new HashMap<Integer, Integer>();
        String[] words = document.split(" ");
        for (int i=0; i<words.length; i++) {
            String lower = words[i].toLowerCase().trim();
            lower = lower.replaceAll(" ","");
            Vector<String> stopWords = new StopWords().getStopWords();
            String term = PorterStemming.getStemmedWord(lower);
            if(!stopWords.contains(term) && term != " " && term.length()>1) {
                int termId = dictionary.get(term);
                if (!docWordCount.containsKey(termId)) {
                    docWordCount.put(termId,1);
                }
                else {
                    docWordCount.put(termId, docWordCount.get(termId) + 1);
                }
            }
        }

        List<Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>(docWordCount.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<Integer, Integer>>()
        {
            @Override
            public int compare( Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2 )
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        } );

        ArrayList<Integer> wordAndCount = new ArrayList<Integer>();
        for (Entry<Integer, Integer> term: list) {
            wordAndCount.add(term.getKey());
            wordAndCount.add(term.getValue());
        }

        documentTermMap.put(indexCount, wordAndCount);
    }

    @Override
    public void loadIndex() throws IOException, ClassNotFoundException {
        if(dictionaryLoad) {
            loadDictionaryAndDocuments();
            dictionaryLoad = false;
        }
        StringBuilder builder = new StringBuilder(_options._indexPrefix).append("/").append("index"+indexLoadCount+".tsv");
        FileInputStream in = new FileInputStream(builder.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        int count = 0;
        Integer key = 0;

        ArrayList<Integer> value = null;
        while((line = br.readLine()) != null) {
            List<String> lineList = stringTokenizer(line);
            if(lineList.size() == 1) { // term id read from file
                // initializing value list for next line
                if(count != 0) {
                    index.put(key, value);
                }
                value = new ArrayList<Integer>();
                count++;
                key = Integer.parseInt(lineList.get(0));
            } else {
                for(String s : lineList) {
                    value.add(Integer.parseInt(s));
                }
            }
        }
        br.close();
    }

    public void loadDictionaryAndDocuments() throws IOException {
        CorpusAnalyzerPagerank c = new CorpusAnalyzerPagerank(new Options("conf/engine.conf"));
        LogMinerNumviews l = new LogMinerNumviews(new Options("conf/engine.conf"));
        HashMap<String, Float> _R = (HashMap<String, Float>) c.loadFromFile(_options._indexPrefix + "/pageRank.tsv");
        HashMap<String, Integer> _N = (HashMap<String, Integer>)l.loadFromFile(_options._indexPrefix + "/numViews.tsv");
        System.out.println(_R.size());
        System.out.println(_N.size());
        StringBuilder builder = new StringBuilder(_options._indexPrefix).append("/").append("documentsAndDict.tsv");
        FileInputStream in = new FileInputStream(builder.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        Boolean dict = false;
        while((line = br.readLine()) != null) {
            //System.out.println("Line is "+line);
            List<String> lineList = stringTokenizer(line);
            if(lineList.size()!=0 && lineList.get(0).equals("#####")) {
                dict = true;
                continue;
            }

            if(!dict) {
                Scanner scanner = new Scanner(line).useDelimiter("\t");
                while (scanner.hasNext()) {
                    int docid = Integer.parseInt(scanner.next());
                    DocumentIndexed documentIndexed = new DocumentIndexed(docid);
                    String title = scanner.next();
                    documentIndexed.setTitle(title);
                    documentIndexed.setUrl(scanner.next());
                    documentIndexed.setNumberOfWords(Long.parseLong(scanner.next()));
                    documentIndexed.setPageRank(_R.get(title));
                    documentIndexed.setNumViews(_N.get(title));
                    _documents.put(docid, documentIndexed);
                }
            } else if(lineList.size() > 1 && lineList.size() <=2){
                dictionary.put(lineList.get(1), Integer.parseInt(lineList.get(0)));
                reverseDictionary.put(Integer.parseInt(lineList.get(0)),lineList.get(1));
            }
        }

        for(int docid : _documents.keySet()) {
            DocumentIndexed document = _documents.get(docid);
            _totalTermFrequency = _totalTermFrequency + document.getNumberOfWords();
        }
    }

    public void loadToCache(int loadIndex) throws IOException, ClassNotFoundException {
        //Clear before loading into cache again.
        index.clear();
        indexLoadCount = loadIndex + 1;
        System.out.println(indexLoadCount+" IndexLoadCount");
        loadIndex();
    }

    public boolean checkIndexForTerm(int termId) throws IOException, ClassNotFoundException {
        File indexFolder = new File(_options._indexPrefix);
        File[] listOfFiles = indexFolder.listFiles();
        int noOfFiles = listOfFiles.length-22;
        if(termId <= noOfFiles * 500) {
            //System.out.println((int) Math.floor(termId / 5000));
            if(index.containsKey(termId)) {
                return true;
            } else {
                loadToCache((int) Math.floor(termId / 500));
            }
            //System.out.println(index);
            if(index.containsKey(termId)) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getTerm(int termId) throws ClassNotFoundException, IOException{
        if (cache.containsKey(termId)) {
            System.out.println("Cache accessed");
            return cache.get(termId);
        }
        else{
            if(checkIndexForTerm(termId)) {
                if(cache.size() == 100) {
                    cache.clear();
                }
                System.out.println("Not found in cache");
                cache.put(termId, index.get(termId));
                return cache.get(termId);
            } else {
                return null;
            }
        }
    }

    @Override
    public Document getDoc(int docid) {
        if (_documents.containsKey(docid)) {
            return _documents.get(docid);
        }
        return null;
    }

    /**
     * In HW2, you should be using {@link DocumentIndexed}.
     */
    @Override
    public Document nextDoc(Query query, int docid) {
        if(query instanceof QueryPhrase && ((QueryPhrase) query)._phraseTokens.size() != 0){
            System.out.println("nextDocForPhrase!!!");
            try {
                return nextDocForPhrase(query, docid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            // System.out.println("nextDocForSimple!!!");
            try {
                return nextDocForSimple(query, docid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Document nextDocForSimple(Query query, int docid) throws IOException, ClassNotFoundException {
        int maxDocId = -1;
        //int start = 1, end = 1;
        boolean flag = true;
        for(String q: query._tokens){
            int nextDocId = next(q,docid);

            if(nextDocId == -1){
                return null;
            }

            if(maxDocId < 0) {
                maxDocId = nextDocId;
            }

            System.out.println("nextDocId("+docid+")= "+nextDocId);

            if(maxDocId != nextDocId) {
                flag = false;
                if (maxDocId < nextDocId) {
                    maxDocId = nextDocId;
                }
            }
        }

        if(flag){
            return _documents.get(maxDocId);
        }
        return nextDocForSimple(query, maxDocId - 1);
    }

    public int next(String word, int docId) throws IOException, ClassNotFoundException {
        if( (word == null) || (word.trim().length() == 0) ){
            return -1;
        }
        int termId = -1;
        if(dictionary.containsKey(word)){
            termId = dictionary.get(word);
        }
        if(termId < 0){
            return -1;
        }
        // valueList is the list of docIDS for the word.
        List<Integer> docOccLocList = getTerm(termId);
      /*if(indexLoadCount!=1){
      	docId = -1;
      }*/
        if(docId == -1) {
            docId = docOccLocList.get(0);
            return docId;
        }

        int i;
        for(i=0; i < docOccLocList.size(); ){
            if(docOccLocList.get(i)>(docId)) {
                return docOccLocList.get(i);
            }
            else{
                i = i + docOccLocList.get(i+1) + 2;
            }
        }
        if(i>=docOccLocList.size()) {
            return -1;
        }
        return -1;
    }

    private DocumentIndexed nextDocForPhrase(Query query, int docid) throws IOException, ClassNotFoundException {

        int maxDocId = -1;
        boolean phrasePresent = true;
        for (Vector<String> phrase : ((QueryPhrase) query)._phraseTokens) {
            StringBuffer sb = new StringBuffer();
            for (String word : phrase) {
                sb.append(word+" ");
            }
            Query q = new Query(sb.toString());
            q.processQuery();

            int docId = nextDocForPhraseHelper(q, docid);

            if(docId == -1){
                return null;
            }

            if (maxDocId < 0) {
                maxDocId = docId;
            }

            if (maxDocId != docId) {
                phrasePresent = false;
                if (maxDocId < docId) {
                    maxDocId = docId;
                }
            }
        }

        if (query._tokens.size() != 0) {
            StringBuffer sb = new StringBuffer();
            for (String term : ((QueryPhrase) query)._tokens) {
                sb.append(term+" ");
            }
            Query q = new Query(sb.toString());
            q.processQuery();
            DocumentIndexed simpleDoc = (DocumentIndexed)nextDocForSimple(q, docid);
            if(simpleDoc == null){
                return null;
            }

            int docId = simpleDoc._docid;

            if (maxDocId < 0) {
                maxDocId = docId;
            }

            if (maxDocId != docId) {
                phrasePresent = false;
                if (maxDocId < docId) {
                    maxDocId = docId;
                }
            }
        }

        if(phrasePresent) {
            return _documents.get(maxDocId);
        }
        return nextDocForPhrase(query, maxDocId - 1);
    }


    private int nextDocForPhraseHelper(Query q, int docid) throws IOException, ClassNotFoundException {

        boolean phrasePresent = true;
        DocumentIndexed phraseDoc = (DocumentIndexed)nextDocForSimple(q, docid);
        if(phraseDoc == null){
            return -1;
        }

        int docId = phraseDoc._docid;

        HashSet<Integer> firstTermLocation = new HashSet<Integer>();

        Iterator iterator = q._tokens.iterator();
        int termId = dictionary.get(iterator.next().toString());
        List<Integer> docOccLocList = getTerm(termId);
        int location = 0;
        for (int i = 0; i < docOccLocList.size(); ) {
            if (docOccLocList.get(i) == docId) {
                location = i;
                break;
            } else {
                i = i + docOccLocList.get(i + 1) + 2;
            }
        }


        for (int i = 0; i < docOccLocList.get(location + 1); i++) {
            firstTermLocation.add(docOccLocList.get(location + 2 + i));
        }

        int counter = 1;
        boolean tokenpresent = false;

        while (iterator.hasNext()) {
            //counter++;
            int termId2 = dictionary.get(iterator.next().toString());
            List<Integer> docOccLocList2 = getTerm(termId2);
            int location2 = -1;
            for (int i = 0; i < docOccLocList2.size(); ) {
                if (docOccLocList2.get(i) == docId) {
                    location2 = i;
                    break;
                } else if (docOccLocList2.get(i) > docId) {
                    break;
                }
                else {
                    i = i + docOccLocList2.get(i + 1) + 2;
                }
            }
            if (location2 == -1) {
                return -1;
            }

            for (int i = 0; i < docOccLocList2.get(location2 + 1); i++) {
                if (firstTermLocation.contains(docOccLocList2.get(location2 + 2 + i) - counter)) {
                    tokenpresent=true;
                    break;
                }
            }

            counter++;
            if (tokenpresent) {
                tokenpresent = false;
            } else {
                phrasePresent = false;
                break;
            }
        }

        if (phrasePresent) {
            return docId;
        }
        else {
            return nextDocForPhraseHelper(q, docId);
        }

    }

    @Override
    public int corpusDocFrequencyByTerm(String term) {

        if (dictionary.containsKey(term)) {
            int termid = dictionary.get(term);
            try {
                if (cache.containsKey(termid) || checkIndexForTerm(termid)) {
                    List<Integer> occurrence = getTerm(termid);
                    int documentCount = 0;
                    int i=0;
                    while (i < occurrence.size()) {
                        i=i+occurrence.get(i+1)+1;
                        documentCount = documentCount + 1;
                    }
                    return documentCount;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 0;
    }


    @Override
    public int corpusTermFrequency(String term) {
        if (dictionary.containsKey(term)) {
            int termid = dictionary.get(term);
            try {
                if (cache.containsKey(termid) || checkIndexForTerm(termid)) {
                    List<Integer> occurrence = getTerm(termid);
                    int corpusTermFreq = 0;
                    int i=0;
                    while (i < occurrence.size()) {
                        corpusTermFreq = corpusTermFreq + occurrence.get(i+1);
                        i=i+occurrence.get(i+1)+2;
                    }
                    return corpusTermFreq;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 0;
    }

    @Override
    public int documentTermFrequency(String term, String url) {
        int documentId = -1;
        for (int docid : _documents.keySet()) {
            if (_documents.get(docid).getUrl().equals(url)) {
                documentId = docid;
                //System.out.println("Document ID: "+ documentId);
            }
        }
        if (documentId == -1) {
            return 0;
        }

        if (dictionary.containsKey(term)) {
            int termid = dictionary.get(term);
            try {
                if (cache.containsKey(termid) || checkIndexForTerm(termid)) {
                    List<Integer> occurrence = getTerm(termid);
                    int docTermFreq = 0;
                    int i = 0;
                    while (i < occurrence.size()) {
                        if (documentId == occurrence.get(i)) {
                            docTermFreq = occurrence.get(i + 1);
                            return docTermFreq;
                        }
                        i = i + occurrence.get(i + 1) + 2;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 0;
    }


    public List<Integer> getTopTerms(int doc, int m) {
        if (documentTermMap.containsKey(doc)) {
            return documentTermMap.get(doc).subList(0,m*2);
        }
        else if (doc <= _documents.size()){
            documentTermMap.clear();
            int i=0;
            if (doc % 500 == 0) {
                i = (doc / 500);
            }
            else {
                i = (doc / 500) + 1;
            }

            StringBuilder builder = new StringBuilder(_options._indexPrefix).append("/").append(i+"DocTermMap.tsv");
            FileInputStream in = null;
            try {
                in = new FileInputStream(builder.toString());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;

                while ((line = br.readLine()) != null) {
                    ArrayList<Integer> valueList = new ArrayList<Integer>();
                    Scanner keyValue = new Scanner(line).useDelimiter("-->");//line.split("-->");
                    int docId = Integer.parseInt(keyValue.next());
                    if(keyValue.hasNext()) {
                        String[] values = keyValue.next().split("\t");
                        for (String value : values) {
                            valueList.add(Integer.parseInt(value));
                        }
                    }
                    documentTermMap.put(docId, valueList);
                }

                return documentTermMap.get(doc).subList(0,m*2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getTermName(int termId) {
        return reverseDictionary.get(termId);
    }

    public static void main(String args[]) {
        try {
            IndexerInvertedOccurrence ind = new IndexerInvertedOccurrence(new Options("conf/engine.conf"));
            ind.loadIndex();
            Set<Entry<String, Integer>> dictSet = ind.dictionary.entrySet();
            for(Entry e : dictSet) {
                if(Integer.parseInt(e.getValue().toString()) <= 5000 ) {
                    System.out.println(e.getValue()+ " " + e.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}