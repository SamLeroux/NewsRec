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
package be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.IRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.TrendingTopicRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.DatabaseLuceneScorer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class RecommenderBuilder {

    private boolean closed = false;
    
    private final IRatingsDao ratingsDao;
    private final ITrendsDao trendsDao;
    private final IViewsDao viewsDao;
    private final String luceneIndexLocation;
    private SearcherManager searcherManager;

    private static final Logger logger = Logger.getLogger(RecommenderBuilder.class);

    public RecommenderBuilder(IRatingsDao ratingsDao, ITrendsDao trendsDao, IViewsDao viewsDao, String luceneIndexLocation) throws RecommenderBuildException {
        this.ratingsDao = ratingsDao;
        this.trendsDao = trendsDao;
        this.viewsDao = viewsDao;
        this.luceneIndexLocation = luceneIndexLocation;
        build();
    }

    public IRecommender getRecommender() throws RecommenderBuildException {
        if (closed){
            throw new RecommenderBuildException("builder is closed");
        }
        return new TrendingTopicRecommender(trendsDao, viewsDao, searcherManager);
    }

    public IScorer getScorer() throws RecommenderBuildException {
        if (closed){
            throw new RecommenderBuildException("builder is closed");
        }
        return new DatabaseLuceneScorer(searcherManager, ratingsDao);
    }

    public void close() throws RecommenderBuildException {
        Exception ex = null;
        if (ratingsDao != null) {
            try {
                ratingsDao.close();
            } catch (DaoException ex1) {
                logger.error(ex1);
                ex = ex1;
            }
        }
        if (viewsDao != null) {
            try {
                viewsDao.close();
            } catch (DaoException ex1) {
                logger.error(ex1);
                ex = ex1;
            }
        }
        if (trendsDao != null) {
            try {
                trendsDao.close();
            } catch (DaoException ex1) {
                logger.error(ex1);
                ex = ex1;
            }
        }
        if (searcherManager != null) {
            try {
                searcherManager.close();
            } catch (IOException ex1) {
                logger.error(ex1);
                ex = ex1;
            }
        }
        closed = true;
        if (ex != null){
            throw new RecommenderBuildException(ex);
        }
    }

    private void build() throws RecommenderBuildException {
        try {
            Directory dir = FSDirectory.open(new File(luceneIndexLocation));
            searcherManager = new SearcherManager(dir, null);
        } catch (IOException ex) {
            throw new RecommenderBuildException(ex);
        }
    }
}
