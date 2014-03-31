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
package be.ugent.tiwi.sleroux.newsrec.recommendationstester;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.TermScorePair;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LuceneTopTermExtract implements Serializable {

    private static final Logger logger = Logger.getLogger(LuceneTopTermExtract.class);
    private static final int DEFAULT_NUMBER_TERMS = 10;
    private final Analyzer analyzer;

    public LuceneTopTermExtract(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Map<String, Double> getTopTerms(String id, IndexReader reader) {
        return getTopTerms(id, reader, DEFAULT_NUMBER_TERMS);
    }

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

    public Map<String, Double> getTopTerms(int docNr, IndexReader reader) {
        return getTopTerms(docNr, reader, DEFAULT_NUMBER_TERMS);
    }

    public Map<String, Double> getTopTerms(int docNr, IndexReader reader, int numberOfTerms) {
        try {
            Map<String, Double> termFreq = new HashMap<>(200);
            Map<String, Integer> docFreqs = new HashMap<>(200);

            Document doc = reader.document(docNr);

            updateFrequenciesMaps(termFreq, docFreqs, "title", doc.get("title"), reader, 2);
            updateFrequenciesMaps(termFreq, docFreqs, "description", doc.get("description"), reader, 1.5);
            updateFrequenciesMaps(termFreq, docFreqs, "text", doc.get("text"), reader, 1);

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

    public void addTopTerms(NewsItem item, IndexReader reader) {
        addTopTerms(item, DEFAULT_NUMBER_TERMS, reader);
    }

    public void addTopTerms(NewsItem item, int numberOfTerms, IndexReader reader) {
        try {
            Map<String, Double> termFreq = new HashMap<>(200);
            Map<String, Integer> docFreqs = new HashMap<>(200);

            updateFrequenciesMaps(termFreq, docFreqs, "title", item.getTitle(), reader, 2);
            updateFrequenciesMaps(termFreq, docFreqs, "description", item.getDescription(), reader, 1.5);
            updateFrequenciesMaps(termFreq, docFreqs, "text", item.getFulltext(), reader, 1);

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

    private void updateFrequenciesMaps(Map<String, Double> freqMap, Map<String, Integer> docFreqMap, String field, String content, IndexReader reader, double weight) throws IOException {
        if (content != null && !"".equals(content)) {
            try (TokenStream stream = analyzer.tokenStream(field, content)) {
                stream.reset();

                while (stream.incrementToken()) {
                    String term = stream.getAttribute(CharTermAttribute.class).toString();
                    term = term.trim();
                    if (freqMap.containsKey(term)) {
                        freqMap.put(term, freqMap.get(term) + weight);
                    } else {
                        freqMap.put(term, weight);
                    }

                    int docFreq = reader.docFreq(new Term(field, term));
                    if (docFreqMap.containsKey(content)) {
                        docFreqMap.put(term, docFreqMap.get(content) + docFreq);
                    } else {
                        docFreqMap.put(term, docFreq);
                    }
                }
            }
        }
    }

    private PriorityQueue<TermScorePair> getTermScores(Map<String, Double> termFreqMap, Map<String, Integer> docFreqMap, IndexReader reader) {
        int numDocs = reader.numDocs();
        PriorityQueue<TermScorePair> pq = new PriorityQueue<>(termFreqMap.size());

        double max = 0;
        double avg = 0;
        for (String term: termFreqMap.keySet()){
            max = (max > termFreqMap.get(term)? max: termFreqMap.get(term));
            avg += termFreqMap.get(term);
        }
        for (String term : termFreqMap.keySet()) {
            System.out.println(term);
            double tf = Math.log(termFreqMap.size()/termFreqMap.get(term));
            int docFreq = docFreqMap.get(term);
            if (docFreq > 0) {
                double idf = 1 + Math.log((double) numDocs / docFreqMap.get(term));
                double score = tf * idf;
                pq.add(new TermScorePair(term, score));
            }
        }
        return pq;
    }

}
