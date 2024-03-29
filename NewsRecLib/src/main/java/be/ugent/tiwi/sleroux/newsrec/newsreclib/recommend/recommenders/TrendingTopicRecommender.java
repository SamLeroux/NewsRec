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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.RecommendedNewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.filters.RecentFilter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.utils.ScoreDecay;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TrendingTopicRecommender extends LuceneRecommender implements IRecommender {

    private final ITrendsDao trendsDao;
    private final ScoreDecay decay;
    private static final Logger logger = Logger.getLogger(TrendingTopicRecommender.class);

    public TrendingTopicRecommender(ITrendsDao trendsDao, IViewsDao viewsDao, SearcherManager manager) {
        super(manager);
        this.trendsDao = trendsDao;
        this.decay = new ScoreDecay();
        decay.setM(10e-8);
    }

    @Override
    public List<RecommendedNewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        IndexSearcher searcher = null;
        try {
            String[] trends = trendsDao.getTrends(250);
            Query query = buildQuery(trends);
            int hitsPerPage = start + count;

            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            //Filter filter = new SeenArticlesFilter(viewsDao, userid);
            Filter f = new RecentFilter("timestamp", 1000 * 60 * 60 * 24);

            manager.maybeRefresh();
            searcher = manager.acquire();

            searcher.search(query, f, collector);

            ScoreDoc[] hits = collector.topDocs(start, count).scoreDocs;

            List<RecommendedNewsItem> results = new ArrayList<>(hits.length);

            for (ScoreDoc hit : hits) {
                int docId = hit.doc;
                Document d = searcher.doc(docId);
                RecommendedNewsItem item = toNewsitem(d, docId, hit.score, "trending");
                results.add(item);
            }

            return results;

        } catch (TrendsDaoException | IOException ex) {
            logger.error(ex);
            throw new RecommendationException(ex);
        } finally {
            try {
                if (searcher != null) {
                    manager.release(searcher);
                }
            } catch (IOException ex) {
                logger.error(ex);
            }
            searcher = null;
        }
    }

    protected Query buildQuery(String[] trends) {
        BooleanQuery q = new BooleanQuery();
        for (String term : trends) {
            Query query2 = new TermQuery(new Term("title", term));
            q.add(query2, BooleanClause.Occur.SHOULD);
        }

        RecencyBoostQuery rq = new RecencyBoostQuery(q, decay);

        return rq;
    }

}
