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
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Updates the user model stored in a database with the information of the
 * viewed item stored in the Lucene index.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DatabaseLuceneScorer implements IScorer {

    private final IRatingsDao ratingsDao;
    private final SearcherManager manager;
    private final Directory dir;
    private static final Logger logger = Logger.getLogger(DatabaseLuceneScorer.class);

    /**
     *
     * @param luceneIndexLocation Location where the index is stored
     * @param dao The RatingsDao to use
     * @throws IOException when there was an error opening the Lucene index.
     */
    public DatabaseLuceneScorer(String luceneIndexLocation, IRatingsDao dao) throws IOException {
        ratingsDao = dao;
        dir = FSDirectory.open(new File(luceneIndexLocation));
        manager = new SearcherManager(dir, null);
    }

    @Override
    public void score(long user, long item, double rating) {
        try {
            Map<String, Double> termsToStore = getTopTerms(item);
            if (!termsToStore.isEmpty()) {
                for (String term : termsToStore.keySet()) {
                    termsToStore.put(term, rating * termsToStore.get(term));
                }
                ratingsDao.giveRating(user, termsToStore);
                logger.info("Success giving score to item: " + item + " for user: " + user);
            }
        } catch (RatingsDaoException | IOException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    @Override
    public void view(long user, long item) {
        score(user, item, 0.75);
    }

    private Map<String, Double> getTopTerms(long item) throws IOException {
        Query q = new TermQuery(new Term("id", Long.toString(item)));
        TopScoreDocCollector col = TopScoreDocCollector.create(1, true);

        manager.maybeRefreshBlocking();
        IndexSearcher searcher = manager.acquire();
        IndexReader reader = searcher.getIndexReader();

        searcher.search(q, col);
        ScoreDoc[] hits = col.topDocs().scoreDocs;

        Map<String, Double> terms = new HashMap<>(10);
        if (hits.length > 0) {
            if (hits.length > 1) {
                logger.warn("hits.length should be 1 for id=" + item + " it is " + hits.length);
            }
            int docnr = hits[0].doc;
            Document doc = reader.document(docnr);
            for (String term : doc.getValues("term")) {
                terms.put(term, 1.0);
            }

        } else {
            logger.error("Could not find document with id=" + item);
        }
        manager.release(searcher);
        return terms;
    }

}
