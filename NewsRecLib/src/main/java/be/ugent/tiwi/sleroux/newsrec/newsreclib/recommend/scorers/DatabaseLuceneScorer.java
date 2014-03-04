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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.TermScorePair;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * Updates the user model stored in a database with the information of the
 * viewed item stored in the Lucene index.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DatabaseLuceneScorer implements IScorer {

    private final IRatingsDao ratingsDao;
    private final ReaderManager manager;
    private static final Logger logger = Logger.getLogger(DatabaseLuceneScorer.class);

    /**
     *
     * @param lucineIndexLocation Location where the index is stored
     * @param dao The RatingsDao to use
     * @throws IOException when there was an error opening the Lucene index.
     */
    public DatabaseLuceneScorer(String lucineIndexLocation, IRatingsDao dao) throws IOException {
        ratingsDao = dao;
        Directory dir = FSDirectory.open(new File(lucineIndexLocation));
        //reader = DirectoryReader.open(dir, 1);
        manager = new ReaderManager(dir);
    }

    @Override
    public void score(long user, int item, double rating) {
        // fetch the terms occuring in this document
        Map<String, Double> termMap = new HashMap<>(250);
        updateTermMap(termMap, item, "text", 1);
        // terms in the title adn description are more important than terms in the text.
        updateTermMap(termMap, item, "description", 1.5);
        updateTermMap(termMap, item, "title", 2);

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
            int n = (pq.size() < 10 ? pq.size() : 10);
            int i = 0;
            TermScorePair tsp = pq.poll();
            Map<String, Double> termsToStore = new HashMap<>();
            while (i < n && tsp != null) {
                termsToStore.put(tsp.getTerm(), tsp.getScore() * rating);
                tsp = pq.poll();
                i++;
            }

            // Store them in the database
            try {
                ratingsDao.giveRating(user, termsToStore);
            } catch (RatingsDaoException ex) {
                logger.error(ex.getMessage(), ex);
            }

        }
    }

    @Override
    public void view(long user, int item) {
        score(user, item, 0.75);

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
    private void updateTermMap(Map<String, Double> termMap, int item, String field, double weight) {
        DirectoryReader reader = null;
        try {
            reader = manager.acquire();
            manager.maybeRefresh();
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
        } catch (IOException | NullPointerException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try {
                    manager.release(reader);
                } catch (IOException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    

}
