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
package be.ugent.tiwi.sleroux.newsrec.webnewsrecommender;

import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.IClusterer;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.clustering.LingPipeHierarchicalClustering;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.DaoException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.ITrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.IViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.cachingProxyImpl.CachingTrendsDaoProxy;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCRatingsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCTrendsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.dao.mysqlImpl.JDBCViewsDao;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.RecommenderBuildException;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.RecommenderBuilder;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.recommenders.IRecommender;
import be.ugent.tiwi.sleroux.newsrec.newsreclib.recommend.scorers.IScorer;
import java.util.ResourceBundle;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Sam Leroux <sam.leroux@ugent.be>
 */
public class NewsRecContextListener implements ServletContextListener {

    private IViewsDao viewsDao;
    private IRatingsDao ratingsDao;
    private ITrendsDao trendsDao;
    private IRecommender recommender;
    private IScorer scorer;
    private IClusterer clusterer;
    private RecommenderBuilder builder;
    private static final Logger logger = Logger.getLogger(NewsRecContextListener.class);
    private static final ResourceBundle bundle = ResourceBundle.getBundle("WebNewsrecommender");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {

            viewsDao = new JDBCViewsDao();
            sce.getServletContext().setAttribute("viewsDao", viewsDao);

            ratingsDao = new JDBCRatingsDao();
            trendsDao = new CachingTrendsDaoProxy(new JDBCTrendsDao());

            String luceneLocation = bundle.getString("luceneIndexLocation");

            builder = new RecommenderBuilder(ratingsDao, trendsDao, viewsDao, luceneLocation);

            recommender = builder.getRecommender();
            sce.getServletContext().setAttribute("recommender", recommender);

            scorer = builder.getScorer();
            sce.getServletContext().setAttribute("scorer", scorer);

            clusterer = new LingPipeHierarchicalClustering();
            sce.getServletContext().setAttribute("clusterer", clusterer);

        } catch (DaoException ex) {
            logger.error(ex);
        } catch (RecommenderBuildException ex) {
            logger.error(ex);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (builder != null){
                builder.close();
            }
        } catch (RecommenderBuildException ex) {
            logger.error(ex);
        }
    }

}
