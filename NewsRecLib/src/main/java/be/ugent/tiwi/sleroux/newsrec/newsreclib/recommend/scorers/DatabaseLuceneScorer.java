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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DatabaseLuceneScorer implements IScorer {

    private final IRatingsDao ratingsDao;
    private final IndexReader reader;
    private static final Logger logger = Logger.getLogger(DatabaseLuceneScorer.class);

    public DatabaseLuceneScorer(String lucineIndexLocation, IRatingsDao dao) throws IOException {
        ratingsDao = dao;
        Directory dir = FSDirectory.open(new File(lucineIndexLocation));
        reader = DirectoryReader.open(dir, 1);
    }

    @Override
    public void score(long user, int item, double rating) {
        view(user, item);
    }

    @Override
    public void view(long user, int item) {
        Map<String, Double> termMap = new HashMap<>(250);
        updateTermMap(termMap, item, "text", 1);
        updateTermMap(termMap, item, "title", 1.5);

        PriorityQueue<TermScorePair> pq = new PriorityQueue<>(termMap.size());
        for (String term : termMap.keySet()) {
            TermScorePair p = new TermScorePair(term, termMap.get(term));
            pq.add(p);
        }
        int n = (pq.size() < 10 ? pq.size() : 10);
        int i = 0;
        TermScorePair tsp = pq.poll();
        Map<String, Double> termsToStore = new HashMap<>();
        try {
            while (i < n && tsp != null) {
                termsToStore.put(tsp.getTerm(), tsp.getScore());
                tsp = pq.poll();
                i++;
            }
            ratingsDao.giveRating(user, termsToStore);
        } catch (RatingsDaoException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    private void updateTermMap(Map<String, Double> termMap, int item, String field, double weight) {
        try {
            Terms vector = reader.getTermVector(item, field);
            TermsEnum termsEnum;
            termsEnum = vector.iterator(TermsEnum.EMPTY);
            BytesRef text;
            while ((text = termsEnum.next()) != null) {
                String term = text.utf8ToString();
                int tf = (int) termsEnum.totalTermFreq();
                double idf = 1.0 / reader.docFreq(new Term("text", text));
                if (!Double.isInfinite(idf)) {
                    if (!termMap.containsKey(term)) {
                        termMap.put(term, tf * idf * weight);
                    } else {
                        termMap.put(term, termMap.get(term) + tf * idf * weight);
                    }
                }
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private class TermScorePair implements Comparable<Object> {

        private String term;
        private double score;

        public TermScorePair(String term, double score) {
            this.term = term;
            this.score = score;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof TermScorePair) {
                return Double.compare(((TermScorePair) o).score, score);
            }
            return -1;
        }

    }

}
