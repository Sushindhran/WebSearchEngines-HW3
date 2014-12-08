package edu.nyu.cs.cs2580;

/**
 * Document with score.
 *
 * @author fdiaz
 * @author congyu
 */
class ScoredDocument implements Comparable<ScoredDocument> {
    private Document _doc;
    private double _score;
    private double _pagerank;
    private int _numdocs;

    public ScoredDocument(Document doc, double score) {
        _doc = doc;
        _score = score;
    }

    public String asTextResult() {
        StringBuffer buf = new StringBuffer();
        buf.append(_doc._docid).append("\t");
        buf.append(_doc.getTitle()).append("\t");
        buf.append(_score).append("\t");
        buf.append(_pagerank).append("\t");
        buf.append(_numdocs);
        return buf.toString();
    }

    /**
     * @CS2580: Student should implement {@code asHtmlResult} for final project.
     */
    public String asHtmlResult() {
        String template="<p><a href=URL>TITLE</a>RANK</p>";
        Integer docId = _doc._docid;
        String title = _doc.getTitle();
        Double score = _score;
        Double pageRank = _pagerank;
        Integer numdocs = _numdocs;
        StringBuffer buf = new StringBuffer();
        String url="url?"+"&did="+Integer.toHexString(docId);
        String line;
        line=template.replace("URL", url);
        line = line.replace("TITLE", title);
        line = line.replace("RANK", "\t" + Double.toString(score));
        buf.append(line);
        return buf.toString();
    }

    @Override
    public int compareTo(ScoredDocument o) {
        if (this._score == o._score) {
            return 0;
        }
        return (this._score > o._score) ? 1 : -1;
    }


    public int getDocId() {
        return _doc._docid;
    }

    public String getDocURL() {
        return _doc.getUrl();
    }

    public void setPageRank(double _pagerank) {
        this._pagerank=_pagerank;
    }

    public void setNumDocs(int _numdocs) {
        this._numdocs=_numdocs;
    }
}