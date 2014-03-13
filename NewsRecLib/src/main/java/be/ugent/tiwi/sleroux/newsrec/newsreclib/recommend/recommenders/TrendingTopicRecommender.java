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

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
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
    private final IViewsDao viewsDao;
    private static final Logger logger = Logger.getLogger(TrendingTopicRecommender.class);

    public TrendingTopicRecommender(ITrendsDao trendsDao, IViewsDao viewsDao, SearcherManager manager) {
        super(manager);
        this.trendsDao = trendsDao;
        this.viewsDao = viewsDao;
    }

    
    

    @Override
    public List<NewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        IndexSearcher searcher = null;
        try {
            String[] trends = trendsDao.getTrends();
            Query query = buildQuery(trends);
            int hitsPerPage = count;

            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            Filter filter = new SeenArticlesFilter(viewsDao, userid);
            searcher = manager.acquire();
            manager.maybeRefresh();
            searcher.search(query, filter, collector);

            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            int stop = (start + count < hits.length ? start + count : hits.length);
            List<NewsItem> results = new ArrayList<>(stop - start);

            for (int i = start; i < stop; i++) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                results.add(toNewsitem(d, docId, searcher));
            }

            return results;

        } catch (TrendsDaoException | IOException ex) {
            logger.error(ex);
            throw new RecommendationException(ex);
        } finally {
            try {
                manager.release(searcher);
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
        return new RecencyBoostQuery(q);
    }

}
