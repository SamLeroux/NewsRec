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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.queries.RecencyBoostQuery;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.filters.SeenArticlesFilter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ViewsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.ScoreDecay;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 * Return the n most viewed items as recommendations.
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TopNRecommender extends LuceneRecommender {

    private IViewsDao viewsDao;
    private final ScoreDecay decay;
    private static final Logger logger = Logger.getLogger(TopNRecommender.class);

    public TopNRecommender(IViewsDao viewsDao, SearcherManager manager) {
        super(manager);
        this.viewsDao = viewsDao;
        decay = new ScoreDecay();
        decay.setM(9e-9);
    }

    public IViewsDao getViewsDao() {
        return viewsDao;
    }

    public void setViewsDao(IViewsDao viewsDao) {
        this.viewsDao = viewsDao;
    }

    @Override
    public List<NewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        IndexSearcher searcher = null;
        try {
            List<Long> ids = viewsDao.getNMostSeenArticles(start, start + count);
            Query query = buildQuery(ids);
            int hitsPerPage = count;

            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            Filter filter = new SeenArticlesFilter(viewsDao, userid);
            searcher = manager.acquire();
            searcher.search(query, filter, collector);

            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            int stop = (start + count < hits.length ? start + count : hits.length);
            List<NewsItem> results = new ArrayList<>(stop - start);

            for (int i = start; i < stop; i++) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                results.add(toNewsitem(d, docId));
            }

            return results;

        } catch (ViewsDaoException | IOException ex) {

            throw new RecommendationException(ex);
        } finally {
            if (searcher != null) {
                try {
                    manager.release(searcher);
                } catch (IOException ex) {
                    logger.error(ex);
                }
                searcher = null;
            }
        }
    }

    protected Query buildQuery(List<Long> ids) {
        BooleanQuery q = new BooleanQuery();
        float boost = 1.0F;
        float d = 0.5F / ids.size();
        for (long id : ids) {
            Query query = NumericRangeQuery.newLongRange("id", 1, id, id, true, true);
            query.setBoost(boost);
            boost -= d;
            q.add(query, BooleanClause.Occur.SHOULD);
        }

        return new RecencyBoostQuery(q, decay);
    }

}
