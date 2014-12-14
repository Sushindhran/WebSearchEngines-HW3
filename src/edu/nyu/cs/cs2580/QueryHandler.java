package edu.nyu.cs.cs2580;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.nyu.cs.cs2580.SearchEngine.Options;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Handles each incoming query, students do not need to change this class except
 * to provide more query time CGI arguments and the HTML output.
 *
 * N.B. This class is not thread-safe. 
 *
 * @author congyu
 * @author fdiaz
 */
class QueryHandler implements HttpHandler {

    /**
     * CGI arguments provided by the user through the URL. This will determine
     * which Ranker to use and what output format to adopt. For simplicity, all
     * arguments are publicly accessible.
     */
    public static class CgiArguments {
        // The raw user query
        public String _query = "";
        // How many results to return
        private int _numResults = 10;

        public int numterms;

        public int numdocs;

        public int did;

        public String prefix;

        public String location = null;

        // The type of the ranker we will be using.
        public enum RankerType {
            NONE,
            FULLSCAN,
            CONJUNCTIVE,
            FAVORITE,
            COSINE,
            PHRASE,
            QL,
            LINEAR,
            COMPREHENSIVE,
        }
        public RankerType _rankerType = RankerType.NONE;

        // The output format.
        public enum OutputFormat {
            TEXT,
            HTML,
            JSON
        }
        public OutputFormat _outputFormat = OutputFormat.TEXT;

        public CgiArguments(String uriQuery) {
            String[] params = uriQuery.split("&");
            for (String param : params) {
                String[] keyval = param.split("=", 2);
                if (keyval.length < 2) {
                    continue;
                }
                String key = keyval[0].toLowerCase();
                String val = keyval[1];
                if (key.equals("query")) {
                    _query = val;
                } else if (key.equals("num")) {
                    try {
                        _numResults = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        // Ignored, search engine should never fail upon invalid user input.
                    }
                } else if (key.equals("ranker")) {
                    try {
                        _rankerType = RankerType.valueOf(val.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Ignored, search engine should never fail upon invalid user input.
                    }
                } else if (key.equals("format")) {
                    try {
                        _outputFormat = OutputFormat.valueOf(val.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Ignored, search engine should never fail upon invalid user input.
                    }
                }
                else if (key.equals("numdocs")) {
                    numdocs=Integer.parseInt(val);
                }
                else if (key.equals("numterms")) {
                    numterms=Integer.parseInt(val);
                } else if (key.equals("did")) {
                    did = Integer.parseInt(val);
                } else if (key.equals("prefix")) {
                    prefix = val;
                } else if (key.equals("location")) {
                    location = val;
                }
            }  // End of iterating over params
        }
    }

    public String location = null;

    // For accessing the underlying documents to be used by the Ranker. Since
    // we are not worried about thread-safety here, the Indexer class must take
    // care of thread-safety.
    private Indexer _indexer;

    public QueryHandler(Options options, Indexer indexer) {
        _indexer = indexer;
    }

    private void respondWithMsg(HttpExchange exchange, final String message, CgiArguments.OutputFormat format)
            throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        if(format == CgiArguments.OutputFormat.HTML) {
            responseHeaders.set("Content-Type", "text/html");
        } else if(format == CgiArguments.OutputFormat.JSON) {
            responseHeaders.set("Content-Type", "application/json");
        } else {
            responseHeaders.set("Content-Type", "text/plain");
        }

        responseHeaders.add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(message.getBytes());
        responseBody.close();
    }

    private void constructTextOutput(final Vector<ScoredDocument> docs, StringBuffer response) {
        for (ScoredDocument doc : docs) {
            response.append(response.length() > 0 ? "\n" : "");
            response.append(doc.asTextResult());
        }
        response.append(response.length() > 0 ? "\n" : "");
    }

    private void constructHtmlOutput(final Vector<ScoredDocument> docs, StringBuffer response) {
        response.append("<html>\n<body>\n");
        for (ScoredDocument doc : docs) {
            response.append(response.length() > 0 ? "\n" : "");
            response.append(doc.asHtmlResult());
        }
        response.append("</body>\n</html>\n");
    }

    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (!requestMethod.equalsIgnoreCase("GET")) { // GET requests only.
            return;
        }

        // Print the user request header.
        Headers requestHeaders = exchange.getRequestHeaders();
        System.out.print("Incoming request: ");
        for (String key : requestHeaders.keySet()) {
            System.out.print(key + ":" + requestHeaders.get(key) + "; ");
        }
        System.out.println();

        // Validate the incoming request.
        String uriQuery = exchange.getRequestURI().getQuery();
        String uriPath = exchange.getRequestURI().getPath();

        if (uriPath == null || (!uriPath.equals("/favicon.ico") && uriQuery == null)) {
            respondWithMsg(exchange, " Something wrong with the URI!", null);
        }

        if (!uriPath.equals("/search") && !uriPath.equals("/location") && !uriPath.equals("/suggest") && !uriPath.equals("/prf") && !uriPath.equals("/url") && !uriPath.equals("/favicon.ico")) {
            respondWithMsg(exchange, " "+ uriPath + " is not handled!", null);
        }

        // Process the CGI arguments.
        CgiArguments cgiArgs = new CgiArguments(uriQuery);
        if (uriPath.equals("/search") && cgiArgs._query.isEmpty()) {
            respondWithMsg(exchange, "No query is given!", null);
        }

        Ranker ranker = null;
        Vector<ScoredDocument> scoredDocs = null;

        if(uriPath.equals("/search")) {
            // Create the ranker.
            ranker = Ranker.Factory.getRankerByArguments(
                    cgiArgs, SearchEngine.OPTIONS, _indexer);
            if (ranker == null) {
                respondWithMsg(exchange,
                        "Ranker " + cgiArgs._rankerType.toString() + " is not valid!", null);
            }
            QueryPhrase processedQuery = new QueryPhrase(cgiArgs._query);
            processedQuery.processQuery();

            // Ranking.
            scoredDocs = ranker.runQuery(processedQuery, cgiArgs._numResults);

            // Processing the query.

            StringBuffer response = new StringBuffer();
            switch (cgiArgs._outputFormat) {
                case TEXT:
                    constructTextOutput(scoredDocs, response);
                    break;
                case HTML:
                    constructHtmlOutput(scoredDocs, response);
                    break;
                default:
                    // nothing
            }
            respondWithMsg(exchange, response.toString(), cgiArgs._outputFormat);
            new QueryLogger(cgiArgs._query).writeToFile();
        } else if(uriPath.equals("/url")) {
            int docId = cgiArgs.did;
            Document d = _indexer.getDoc(docId);
            String filename = d.getTitle();
            String content = new String(Files.readAllBytes(Paths.get(_indexer._options._corpusPrefix+"/"+filename)));
            respondWithMsg(exchange, content, CgiArguments.OutputFormat.HTML);
        } else if(uriPath.equals("/prf")) {
            List<ScoredDocument> scoredDocuments;
            if (scoredDocs.size() < cgiArgs.numdocs) {
                scoredDocuments= scoredDocs.subList(0, scoredDocs.size());
            }
            else {
                scoredDocuments= scoredDocs.subList(0, cgiArgs.numdocs);
            }
            HashMap<String, Double> prf = ranker.pseudoRelevanceFeedback(scoredDocuments, cgiArgs.numterms);

            StringBuffer buf = new StringBuffer();
            for (String term : prf.keySet()) {
                buf.append(term).append("\t");
                buf.append(prf.get(term)).append("\n");
            }

            respondWithMsg(exchange, buf.toString(), null);
        } else if(uriPath.equals("/suggest")) {
            String suggestions[] = null;
            System.out.println(location);
            if(location != null) {
                suggestions = _indexer.getSuggestions(cgiArgs.prefix+" "+location);
            } else {
                suggestions = _indexer.getSuggestions(cgiArgs.prefix);
            }
            respondWithMsg(exchange, Arrays.toString(suggestions), CgiArguments.OutputFormat.JSON);
        } else if(uriPath.equals("/location")) {
            location = cgiArgs.location;
            respondWithMsg(exchange, "Location set successfully", CgiArguments.OutputFormat.JSON);
        }
    }
}

