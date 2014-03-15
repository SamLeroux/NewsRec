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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.NewsItemLuceneDocConverter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 * Updates the user model stored in a database with the information of the
 * viewed item stored in the Lucene index.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class DatabaseLuceneScorer implements IScorer {

    private final IRatingsDao ratingsDao;
    private final SearcherManager manager;
    private static final Logger logger = Logger.getLogger(DatabaseLuceneScorer.class);

    /**
     *
     * @param manager
     * @param dao
     * @param analyzer
     */
    public DatabaseLuceneScorer(SearcherManager manager, IRatingsDao dao) {
        ratingsDao = dao;
        this.manager = manager;
    }

    @Override
    public void score(long user, String item, double rating) {
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
    public void view(long user, String item) {
        score(user, item, 0.75);
    }

    private Map<String, Double> getTopTerms(String item) throws IOException {
        manager.maybeRefreshBlocking();
        IndexSearcher searcher = manager.acquire();
        IndexReader reader = searcher.getIndexReader();
        TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
        Query q = new TermQuery(new Term("id",item));
        searcher.search(q,collector);
        if (collector.getTotalHits() > 0){
            int docNr = collector.topDocs().scoreDocs[0].doc;
            Document doc = reader.document(docNr);
            NewsItem nitem = NewsItemLuceneDocConverter.DocumentToNewsItem(doc);
            return nitem.getTerms();
        }else{
            logger.warn("Could not find document with id="+item);
        }
        reader.close();
        manager.release(searcher);
        return new HashMap<>();
    }

}
