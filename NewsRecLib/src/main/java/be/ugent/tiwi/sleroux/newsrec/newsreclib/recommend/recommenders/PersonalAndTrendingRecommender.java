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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.RecommendedNewsItem;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.filters.RecentFilter;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.filters.UniqueResultsFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
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
public class PersonalAndTrendingRecommender extends TrendingTopicRecommender {

    private final IRatingsDao ratingsDao;
    private static final Logger logger = Logger.getLogger(PersonalAndTrendingRecommender.class);

    public PersonalAndTrendingRecommender(ITrendsDao trendsDao, IViewsDao viewsDao, IRatingsDao ratingsDao, SearcherManager manager) {
        super(trendsDao, viewsDao, manager);
        this.ratingsDao = ratingsDao;
    }

    @Override
    public List<RecommendedNewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        count = count / 2;

        List<RecommendedNewsItem> results = super.recommend(userid, start, count);

        IndexSearcher searcher = null;
        try {
            Map<String, Double> terms = ratingsDao.getRatings(userid);
            Query query = buildQuery(terms);
            int hitsPerPage = count;

            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);

            Filter f1 = new UniqueResultsFilter(results);
            Filter f2 = new RecentFilter("timestamp", 1000 * 60 * 60 * 24);
            Filter f = new ChainedFilter(new Filter[]{f1, f2}, ChainedFilter.AND);

            searcher = manager.acquire();
            manager.maybeRefresh();
            searcher.search(query, f, collector);

            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            int stop = (start + count < hits.length ? start + count : hits.length);

            for (int i = start; i < stop; i++) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                RecommendedNewsItem item = toNewsitem(d, docId, hits[i].score, "personal");
                results.add(item);
            }
            //Collections.sort(results);
        } catch (RatingsDaoException | IOException ex) {
            logger.error(ex);
            throw new RecommendationException(ex);
        }
        return results;
    }

    protected Query buildQuery(Map<String, Double> terms) {
        BooleanQuery q = new BooleanQuery();
        for (String term : terms.keySet()) {
            Query query = new TermQuery(new Term("description", term));
            query.setBoost(terms.get(term).floatValue());
            q.add(query, BooleanClause.Occur.SHOULD);
            Query query2 = new TermQuery(new Term("title", term));
            query2.setBoost(terms.get(term).floatValue() * 2);
            q.add(query2, BooleanClause.Occur.SHOULD);
        }
        //return q;
        return new RecencyBoostQuery(q);
    }

}
