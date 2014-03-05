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
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.RatingsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.TrendsDaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.model.NewsItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class TrendingTopicsRecommender extends LuceneRecommender{

    private ITrendsDao trendsDao;
    private IViewsDao viewsDao;
    
    public TrendingTopicsRecommender(ITrendsDao trendsDao, String luceneIndexLocation) throws IOException {
        super(luceneIndexLocation);
        this.trendsDao = trendsDao;
    }
    
    
    @Override
    public List<NewsItem> recommend(long userid, int start, int count) throws RecommendationException {
        IndexSearcher searcher = null;
        try {
            Query query = buildQuery(trendsDao.getTrends());
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
                results.add(toNewsitem(d, docId));
                //System.out.println(docId);
                //System.out.println(searcher.explain(query, docId).toString());
            }

            return results;

        } catch (TrendsDaoException | IOException ex) {
            logger.error(ex);
            throw new RecommendationException(ex);
        }  finally{
            try {
                manager.release(searcher);
            } catch (IOException ex) {
                logger.error(ex);
            }
            searcher = null;
        }
    }
    
    protected Query buildQuery(String[] terms) {
        BooleanQuery q = new BooleanQuery();
        for (String term : terms) {
            Query query = new TermQuery(new Term("text", term));
            q.add(query, BooleanClause.Occur.SHOULD);
            Query query2 = new TermQuery(new Term("title", term));
            q.add(query2, BooleanClause.Occur.SHOULD);
        }
        //return q;
        return new RecencyBoostQuery(q);
    }
    
}
