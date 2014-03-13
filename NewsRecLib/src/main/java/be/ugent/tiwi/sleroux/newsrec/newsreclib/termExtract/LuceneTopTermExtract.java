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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.TermScorePair;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class LuceneTopTermExtract implements Serializable{
    private static final Logger logger = Logger.getLogger(LuceneTopTermExtract.class);
    private static final int DEFAULT_NUMBER_TERMS = 10;

    public Map<String, Double> getTopTerms(String id, IndexReader reader) throws IOException{
        return getTopTerms(id, reader, DEFAULT_NUMBER_TERMS);
    }
    
    public Map<String, Double> getTopTerms(int docNr, IndexReader reader) throws IOException{
        return getTopTerms(docNr, reader, DEFAULT_NUMBER_TERMS);
    }
    
    public Map<String, Double> getTopTerms(String id, IndexReader reader, int numberOfTerms) throws IOException{
        IndexSearcher searcher = new IndexSearcher(reader);
        Query q = new TermQuery(new Term("id",id));
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        searcher.search(q,collector);
        if (collector.getTotalHits() > 0){
            int docNr = collector.topDocs().scoreDocs[0].doc;
            return getTopTerms(docNr, reader,numberOfTerms);
        }
        else{
            logger.error("Could not find document with id="+id);
        }
        return new HashMap<>();
        
    }
    
    public Map<String, Double> getTopTerms(int docNr, IndexReader reader, int numberOfTerms) throws IOException {
        // fetch the terms occuring in this document
        Map<String, Double> termMap = new HashMap<>(250);
        updateTermMap(termMap, docNr, "text", 1, reader);
        // terms in the title adn description are more important than terms in the text.
        updateTermMap(termMap, docNr, "description", 1.5, reader);
        updateTermMap(termMap, docNr, "title", 2, reader);

        if (termMap.size() > 0) {
            // Only store the n most important terms.
            PriorityQueue<TermScorePair> pq = new PriorityQueue<>(termMap.size());
            double avg = 0;
            for (double d : termMap.values()) {
                avg += d;
            }
            avg /= termMap.size();

            for (String term : termMap.keySet()) {
                if (termMap.get(term) > avg) {
                    TermScorePair p = new TermScorePair(term, termMap.get(term));
                    pq.add(p);
                }

            }
            int n = (pq.size() < numberOfTerms ? pq.size() : numberOfTerms);
            int i = 0;
            TermScorePair tsp = pq.poll();
            Map<String, Double> termsToStore = new HashMap<>();
            while (i < n && tsp != null) {
                termsToStore.put(tsp.getTerm(), tsp.getScore());
                tsp = pq.poll();
                i++;
            }
            return termsToStore;
        }
        return new HashMap<>();
    }

    /**
     * Fetch the document and copy all terms and term frequencies to the map.
     * Multiply the weight of each term by the provided weight factor.
     *
     * @param termMap
     * @param item
     * @param field
     * @param weight
     */
    private void updateTermMap(Map<String, Double> termMap, int item, String field, double weight, IndexReader reader) throws IOException {

        try {

            Terms vector = reader.getTermVector(item, field);
            if (vector != null) {
                TermsEnum termsEnum;
                termsEnum = vector.iterator(TermsEnum.EMPTY);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    String term = text.utf8ToString();
                    int docFreq = reader.docFreq(new Term(field, text));
                    // ignore really rare terms and really common terms
                    double minFreq = reader.numDocs() * 0.0001;
                    double maxFreq = reader.numDocs() / 3;
                    if (docFreq > minFreq && docFreq < maxFreq) {
                        double tf = 1 + ((double) termsEnum.totalTermFreq()) / reader.getSumTotalTermFreq(field);
                        double idf = Math.log((double) reader.numDocs() / docFreq);
                        if (!Double.isInfinite(idf)) {
                            if (!termMap.containsKey(term)) {
                                termMap.put(term, tf * idf * weight);
                            } else {
                                termMap.put(term, termMap.get(term) + tf * idf * weight);
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

}
