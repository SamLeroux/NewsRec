/*
 * Copyright 2014 Sam Leroux <sam.leroux@ugent.be>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ugent.tiwi.sleroux.newsrec.newsreclib.termExtract;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.lucene.analyzers.NewsRecLuceneAnalyzer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.TermScorePair;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 * Extracts important terms from a document.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LuceneTopTermExtract implements Serializable {

    private static final Logger logger = Logger.getLogger(LuceneTopTermExtract.class);
    private static final int DEFAULT_NUMBER_TERMS = 10;
    private static final Pattern PATTERN = Pattern.compile("([A-Z][a-z]+( [A-Z][a-z]+)+)");
    private NewsRecLuceneAnalyzer analyzer;

    /**
     *
     * @param analyzer
     */
    public LuceneTopTermExtract(NewsRecLuceneAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void setAnalyzer(NewsRecLuceneAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Returns the 10 most important terms in the document with the specified
     * id.
     *
     * @param id
     * @param reader
     * @return A Map containing the terms and the scores.
     */
    public Map<String, Double> getTopTerms(String id, IndexReader reader) {
        return getTopTerms(id, reader, DEFAULT_NUMBER_TERMS);
    }

    /**
     * Returns the 10 most important terms in the document with the specified
     * docNr. WARNING: document numbers may change, only use this when you are
     * sure you are using the correct document number.
     *
     * @param docNr
     * @param reader
     * @return A Map containing the terms and the scores.
     */
    public Map<String, Double> getTopTerms(int docNr, IndexReader reader) {
        return getTopTerms(docNr, reader, DEFAULT_NUMBER_TERMS);
    }

    /**
     * Returns the 10 most important terms in the document with the specified
     * id.
     *
     * @param id
     * @param reader
     * @param numberOfTerms
     * @return
     */
    public Map<String, Double> getTopTerms(String id, IndexReader reader, int numberOfTerms) {
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
            Query q = new TermQuery(new Term("id", id));
            searcher.search(q, collector);
            if (collector.getTotalHits() > 0) {
                int docNr = collector.topDocs().scoreDocs[0].doc;
                return getTopTerms(docNr, reader, numberOfTerms);
            } else {
                logger.warn("No document found with id=" + id);
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
        return new HashMap<>(0);
    }

    /**
     * Returns the 10 most important terms in the document with the specified
     * id.
     *
     * @param docNr
     * @param reader
     * @param numberOfTerms
     * @return
     */
    public Map<String, Double> getTopTerms(int docNr, IndexReader reader, int numberOfTerms) {
        try {
            Map<String, Double> termFreq = new HashMap<>(200);
            Map<String, Integer> docFreqs = new HashMap<>(200);

            Document doc = reader.document(docNr);

            updateFrequenciesMapsForReader(termFreq, docFreqs, "title", doc.get("title"), reader, 2);
            updateFrequenciesMapsForReader(termFreq, docFreqs, "description", doc.get("description"), reader, 1.5);
            updateFrequenciesMapsForReader(termFreq, docFreqs, "text", doc.get("text"), reader, 1);

            updateFrequenciesMapsForRegex(termFreq, docFreqs, "title", doc.get("title"), reader, 2);
            updateFrequenciesMapsForRegex(termFreq, docFreqs, "description", doc.get("description"), reader, 1.5);
            updateFrequenciesMapsForRegex(termFreq, docFreqs, "text", doc.get("text"), reader, 1);

            PriorityQueue<TermScorePair> pq = getTermScores(termFreq, docFreqs, reader);

            int n = (pq.size() < numberOfTerms ? pq.size() : numberOfTerms);
            int i = 0;
            TermScorePair tsp = pq.poll();
            Map<String, Double> returnTerms = new HashMap<>(n);
            while (i < n && tsp != null) {
                returnTerms.put(tsp.getTerm(), tsp.getScore());
                tsp = pq.poll();
                i++;
            }
            return returnTerms;
        } catch (IOException ex) {
            logger.error(ex);
            return new HashMap<>(0);
        }
    }

    /**
     * Adds the 10 most important terms to the document. The document does not
     * need to be stored in the index.
     *
     * @param item
     * @param reader
     */
    public void addTopTerms(NewsItem item, IndexReader reader) {
        addTopTerms(item, DEFAULT_NUMBER_TERMS, reader);
    }

    /**
     * Adds the most important terms to the document. The document does not need
     * to be stored in the index.
     *
     * @param item
     * @param numberOfTerms
     * @param reader
     */
    public void addTopTerms(NewsItem item, int numberOfTerms, IndexReader reader) {
        try {
            Map<String, Double> termFreq = new HashMap<>(200);
            Map<String, Integer> docFreqs = new HashMap<>(200);

            updateFrequenciesMapsForReader(termFreq, docFreqs, "title", item.getTitle(), reader, 2);
            updateFrequenciesMapsForReader(termFreq, docFreqs, "description", item.getDescription(), reader, 1.5);
            updateFrequenciesMapsForReader(termFreq, docFreqs, "text", item.getFulltext(), reader, 1);

            updateFrequenciesMapsForRegex(termFreq, docFreqs, "title", item.getTitle(), reader, 2);
            updateFrequenciesMapsForRegex(termFreq, docFreqs, "description", item.getDescription(), reader, 1.5);
            updateFrequenciesMapsForRegex(termFreq, docFreqs, "text", item.getFulltext(), reader, 1);

            PriorityQueue<TermScorePair> pq = getTermScores(termFreq, docFreqs, reader);

            int n = (pq.size() < numberOfTerms ? pq.size() : numberOfTerms);
            int i = 0;
            TermScorePair tsp = pq.poll();
            while (i < n && tsp != null) {
                item.addTerm(tsp.getTerm(), tsp.getScore());
                tsp = pq.poll();
                i++;
            }

        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    private void updateFrequenciesMapsForReader(Map<String, Double> freqMap, Map<String, Integer> docFreqMap, String field, String content, IndexReader reader, double weight) throws IOException {
        if (content != null && !"".equals(content)) {
            try (TokenStream stream = analyzer.tokenStream(field, content)) {
                stream.reset();

                while (stream.incrementToken()) {
                    String term = stream.getAttribute(CharTermAttribute.class).toString();
                    term = term.trim();
                    term = term.replaceAll("  ", " ");
                    updateFrequenciesMapsForTerm(freqMap, docFreqMap, field, term, reader, weight);
                }
            }
        }
    }

    private void updateFrequenciesMapsForTerm(Map<String, Double> freqMap, Map<String, Integer> docFreqMap, String field, String term, IndexReader reader, double weight) throws IOException {
        if (freqMap.containsKey(term)) {
            freqMap.put(term, freqMap.get(term) + weight);
        } else {
            freqMap.put(term, weight);
        }

        int docFreq = reader.docFreq(new Term(field, term));
        if (docFreqMap.containsKey(term)) {
            docFreqMap.put(term, docFreqMap.get(term) + docFreq);
        } else {
            docFreqMap.put(term, docFreq);
        }
    }

    private void updateFrequenciesMapsForRegex(Map<String, Double> freqMap, Map<String, Integer> docFreqMap, String field, String content, IndexReader reader, double weight) throws IOException {
        Matcher m = PATTERN.matcher(content);
        CharArraySet stopwords = analyzer.getStopwords();
        while (m.find()) {
            String term = m.group(1).toLowerCase();
            boolean stop = false;
            int i = 0;
            String[] comp = term.split(" ");
            while (i < comp.length && !stop) {
                stop = stopwords.contains(comp[i]);
                i++;
            }
            if (!stopwords.contains(term) && !stop) {
                updateFrequenciesMapsForTerm(freqMap, docFreqMap, field, term, reader, weight);
            }
        }
    }

    private PriorityQueue<TermScorePair> getTermScores(Map<String, Double> termFreqMap, Map<String, Integer> docFreqMap, IndexReader reader) {
        int numDocs = reader.numDocs();
        PriorityQueue<TermScorePair> pq = new PriorityQueue<>(termFreqMap.size());

        for (String term : termFreqMap.keySet()) {
            double tf = 1 + Math.log(termFreqMap.get(term));
            int docFreq = docFreqMap.get(term);
            if (docFreq > 0) {
                double idf = 1 + Math.log((double) numDocs / docFreq);
                double score = tf * idf;
                pq.add(new TermScorePair(term, score));
            }
        }
        return pq;
    }

}
